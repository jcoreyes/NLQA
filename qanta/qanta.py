from numpy import *
from util.gen_util import *
from util.math_util import *
from util.dtree_util import *
from rnn.adagrad import Adagrad
import rnn.propagation as prop
from classify.learn_classifiers import validate
import cPickle, time, argparse
from multiprocessing import Pool


# splits the training data into minibatches
# multi-core parallelization
def par_objective(num_proc, data, params, d, len_voc, rel_list, lambdas):
    pool = Pool(processes=num_proc) 

    # non-data params
    oparams = [params, d, len_voc, rel_list]
    
    # chunk size
    n = len(data) / num_proc
    split_data = [data[i:i+n] for i in range(0, len(data), n)]
    to_map = []
    for item in split_data:
        to_map.append( (oparams, item) )
        
    result = pool.map(objective_and_grad, to_map)
    pool.close()   # no more processes accepted by this pool    
    pool.join()    # wait until all processes are finished
    
    total_err = 0.0
    all_nodes = 0.0
    total_grad = None
    
    for (err, grad, num_nodes) in result:
        total_err += err

        if total_grad is None:
            total_grad = grad
        else:
            total_grad += grad

        all_nodes += num_nodes

    # add L2 regularization
    params = unroll_params(params, d, len_voc, rel_list)
    (rel_dict, Wv, b, L) = params    
    grads = unroll_params(total_grad, d, len_voc, rel_list)
    [lambda_W, lambda_L] = lambdas

    reg_cost = 0.0
    for key in rel_list:
        reg_cost += 0.5 * lambda_W * sum(rel_dict[key] ** 2)
        grads[0][key] = grads[0][key] / all_nodes
        grads[0][key] += lambda_W * rel_dict[key]

    reg_cost += 0.5 * lambda_W * sum(Wv ** 2)
    grads[1] = grads[1] / all_nodes
    grads[1] += lambda_W * Wv

    grads[2] = grads[2] / all_nodes

    reg_cost += 0.5 * lambda_L * sum(L ** 2)
    grads[3] = grads[3] / all_nodes
    grads[3] += lambda_L * L

    cost = total_err / all_nodes + reg_cost
    grad = roll_params(grads, rel_list)

    return cost, grad


# this function computes the objective / grad for each minibatch
def objective_and_grad(par_data):

    params, d, len_voc, rel_list = par_data[0]
    data = par_data[1]
    params = unroll_params(params, d, len_voc, rel_list)
    grads = init_dtrnn_grads(rel_list, d, len_voc)

    (rel_dict, Wv, b, L) = params

    error_sum = 0.0
    num_nodes = 0
    tree_size = 0

    # compute error and gradient for each tree in minibatch
    # also keep track of total number of nodes in minibatch
    for index, tree in enumerate(data):

        nodes = tree.get_nodes()
        for node in nodes:
            node.vec = L[:, node.ind].reshape( (d, 1))

        tree.ans_vec = L[:, tree.ans_ind].reshape( (d, 1))

        prop.forward_prop(params, tree, d)
        error_sum += tree.error()
        tree_size += len(nodes)

        prop.backprop(params[:-1], tree, d, len_voc, grads)

    grad = roll_params(grads, rel_list)
    return (error_sum, grad, tree_size)


# train qanta and save model
if __name__ == '__main__':

    # command line arguments
    parser = argparse.ArgumentParser(description='QANTA: a question answering neural network \
                                     with trans-sentential aggregation')
    parser.add_argument('-data', help='location of dataset', default='data/hist_split')
    parser.add_argument('-We', help='location of word embeddings', default='data/hist_We')
    parser.add_argument('-d', help='word embedding dimension', type=int, default=100)
    parser.add_argument('-np', '--num_proc', help='number of cores to parallelize over', type=int, \
                        default=6)
    parser.add_argument('-lW', '--lambda_W', help='regularization weight for composition matrices', \
                        type=float, default=0.)
    parser.add_argument('-lWe', '--lambda_We', help='regularization weight for word embeddings', \
                        type=float, default=0.)
    parser.add_argument('-b', '--batch_size', help='adagrad minibatch size (ideal: 25 minibatches \
                        per epoch). for provided datasets, 272 for history and 341 for lit', type=int,\
                        default=272)
    parser.add_argument('-ep', '--num_epochs', help='number of training epochs, can also determine \
                         dynamically via validate method', type=int, default=30)
    parser.add_argument('-agr', '--adagrad_reset', help='reset sum of squared gradients after this many\
                         epochs', type=int, default=3)
    parser.add_argument('-v', '--do_val', help='check performance on dev set after this many\
                         epochs', type=int, default=5)
    parser.add_argument('-o', '--output', help='desired location of output model', \
                         default='models/hist_params')

    args = vars(parser.parse_args())
    

    ## load data
    vocab, rel_list, ans_list, tree_dict = \
        cPickle.load(open(args['data'], 'rb'))

    # four total folds in this dataset: train, test, dev, and devtest
    train_trees = tree_dict['train'] 

    # - since the dataset that we were able to release is fairly small, the 
    #   test, dev, and devtest folds are tiny. feel free to validate over another
    #   combination of these folds if you wish. 
    val_trees = tree_dict['dev']

    ans_list = array([vocab.index(ans) for ans in ans_list])

    # NOTE: it significantly helps both accuracy and training time to initialize
    #       word embeddings using something like Word2Vec. we have provided word2vec
    #       embeddings for both datasets. for other data, we strongly recommend 
    #       using a similar smart initialization. you can also randomly initalize, although
    #       this generally results in slower convergence to a worse local minima
    orig_We = cPickle.load(open(args['We'], 'rb'))
    # orig_We = gen_rand_we(len(vocab), d)

    # regularization lambdas
    lambdas = [args['lambda_W'], args['lambda_We']]

    # output log and parameter file destinations
    param_file = args['output']
    log_file = param_file.split('_')[0] + '_log'

    print 'number of training sentences:', len(train_trees)
    print 'number of validation sentences:', len(val_trees)
    rel_list.remove('root')
    print 'number of dependency relations:', len(rel_list)

    ## remove incorrectly parsed sentences from data
    # print 'removing bad trees train...'
    bad_trees = []
    for ind, tree in enumerate(train_trees):

        if tree.get(0).is_word == 0:
            print tree.get_words(), ind
            bad_trees.append(ind)

    # pop bad trees, higher indices first
    # print 'removed ', len(bad_trees)
    for ind in bad_trees[::-1]:
        train_trees.pop(ind)

    # print 'removing bad trees val...'
    bad_trees = []
    for ind, tree in enumerate(val_trees):

        if tree.get(0).is_word == 0:
            # print tree.get_words(), ind
            bad_trees.append(ind)

    # pop bad trees, higher indices first
    # print 'removed ', len(bad_trees)
    for ind in bad_trees[::-1]:
        val_trees.pop(ind)
    
    # add vocab lookup to leaves / answer
    print 'adding lookup'
    for tree in train_trees:
        tree.ans_list = ans_list[ans_list != tree.ans_ind]

    # generate params / We
    params = gen_dtrnn_params(args['d'], rel_list)
    rel_list = params[0].keys()

    # add We matrix to params
    params += (orig_We, )
    r = roll_params(params, rel_list)

    dim = r.shape[0]
    print 'parameter vector dimensionality:', dim

    log = open(log_file, 'w')

    # minibatch adagrad training
    ag = Adagrad(r.shape)

    for tdata in [train_trees]:

        min_error = float('inf')

        for epoch in range(0, args['num_epochs']):

            lstring = ''

            # create mini-batches
            random.shuffle(tdata)
            batches = [tdata[x : x + args['batch_size']] for x in xrange(0, len(tdata), 
                       args['batch_size'])]

            epoch_error = 0.0
            for batch_ind, batch in enumerate(batches):
                now = time.time()
                err, grad = par_objective(args['num_proc'], batch, r, args['d'], len(vocab), \
                                          rel_list, lambdas)
                update = ag.rescale_update(grad)
                r = r - update
                lstring = 'epoch: ' + str(epoch) + ' batch_ind: ' + str(batch_ind) + \
                        ' error, ' + str(err) + ' time = '+ str(time.time()-now) + ' sec'
                print lstring
                log.write(lstring + '\n')
                log.flush()

                epoch_error += err

            # done with epoch
            print 'done with epoch ', epoch, ' epoch error = ', epoch_error, ' min error = ', min_error
            lstring = 'done with epoch ' + str(epoch) + ' epoch error = ' + str(epoch_error) \
                     + ' min error = ' + str(min_error) + '\n\n'
            log.write(lstring)
            log.flush()

            # save parameters if the current model is better than previous best model
            if epoch_error < min_error:
                min_error = epoch_error
                print 'saving model...'
                params = unroll_params(r, args['d'], len(vocab), rel_list)
                cPickle.dump( ( params, vocab, rel_list), open(param_file, 'wb'))

            # reset adagrad weights
            if epoch % args['adagrad_reset'] == 0 and epoch != 0:
                ag.reset_weights()

            # check accuracy on validation set
            if epoch % args['do_val'] == 0 and epoch != 0:
                print 'validating...'
                params = unroll_params(r, args['d'], len(vocab), rel_list)
                train_acc, val_acc = validate([train_trees, val_trees], params, args['d'])
                lstring = 'train acc = ' + str(train_acc) + ', val acc = ' + str(val_acc) + '\n\n\n'
                print lstring
                log.write(lstring)
                log.flush()

    log.close()




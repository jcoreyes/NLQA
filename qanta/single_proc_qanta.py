from numpy import *
from util.gen_util import *
from util.math_util import *
from util.dtree_util import *
from rnn.propagation import *
from rnn.gradient_check import *
import cPickle

## this class is mainly for developing and checking model correctness
## the parallelized version (qanta.py) should be used for large datasets

# does forward propagation and computes error, no backprop
# useful for gradient check
def objective(data, params, d, len_voc, rel_list, lambdas):

    params = unroll_params(params, d, len_voc, rel_list)

    (rel_dict, Wv, b, L) = params

    error_sum = 0.0
    num_nodes = 0
    tree_size = 0.0

    for tree in data:

        nodes = tree.get_nodes()
        for node in nodes:
            node.vec = L[:, node.ind].reshape( (d, 1))

        tree.ans_vec = L[:, tree.ans_ind].reshape( (d, 1))

        forward_prop(params, tree, d)
        error_sum += tree.error()
        tree_size += len(nodes)

    # regularize
    [lambda_W, lambda_L] = lambdas
    reg_cost = 0.0
    for key in rel_list:
        reg_cost += 0.5 * lambda_W * sum(rel_dict[key] ** 2)

    reg_cost += 0.5 * lambda_W * sum(Wv ** 2)
    reg_cost += 0.5 * lambda_L * sum(L ** 2)
    cost = error_sum / tree_size + reg_cost

    return cost


# does both forward and backprop
def objective_and_grad(data, params, d, len_voc, rel_list, lambdas):

    params = unroll_params(params, d, len_voc, rel_list)
    grads = init_dtrnn_grads(rel_list, d, len_voc)

    (rel_dict, Wv, b, L) = params

    error_sum = 0.0
    num_nodes = 0
    tree_size = 0

    for index, tree in enumerate(data):

        nodes = tree.get_nodes()
        for node in nodes:
            node.vec = L[:, node.ind].reshape( (d, 1))

        tree.ans_vec = L[:, tree.ans_ind].reshape( (d, 1))

        forward_prop(params, tree, d)
        error_sum += tree.error()
        tree_size += len(nodes)

        backprop(params[:-1], tree, d, len_voc, grads)

    # regularize
    [lambda_W, lambda_L] = lambdas
    reg_cost = 0.0
    for key in rel_list:
        reg_cost += 0.5 * lambda_W * sum(rel_dict[key] ** 2)
        grads[0][key] = grads[0][key] / tree_size
        grads[0][key] += lambda_W * rel_dict[key]

    reg_cost += 0.5 * lambda_W * sum(Wv ** 2)
    grads[1] = grads[1] / tree_size
    grads[1] += lambda_W * Wv

    grads[2] = grads[2] / tree_size

    reg_cost += 0.5 * lambda_L * sum(L ** 2)
    grads[3] = grads[3] / tree_size
    grads[3] += lambda_L * L

    cost = error_sum / tree_size + reg_cost
    grad = roll_params(grads, rel_list)

    return cost, grad


# loads a small dataset and checks the gradients
if __name__ == '__main__':

    # word embedding dimension
    d = 5

    # regularization lambdas: [lambda_W, lambda_L]
    lambdas = [1e-4, 1e-3]

    # load small dataset for developing
    trees = cPickle.load(open('data/toy_dtrees', 'rb'))

    # populate vocabulary, relation, and answer list
    vocab = []
    ans_list = []
    rel_list = []

    for tree in trees:

        for node in tree.get_nodes():
            word = node.word.lower()
            if word not in vocab:
                vocab.append(word)

            for ind, rel in node.kids:
                if rel not in rel_list:
                    rel_list.append(rel)

        if tree.ans.lower() not in vocab:
            vocab.append(tree.ans.lower())

        ans_ind = vocab.index(tree.ans.lower())
        if ans_ind not in ans_list:
            ans_list.append(ans_ind)

    ans_list = array(ans_list)

    # we don't need the "extra" root node
    rel_list.remove('root')
    print 'found', len(rel_list), 'dependency relations:'
    print rel_list

    # generate params / We
    params = gen_dtrnn_params(d, rel_list)
    rel_list = params[0].keys()
    orig_We = gen_rand_we(len(vocab), d)

    # add We matrix to params
    params += (orig_We, )

    r = roll_params(params, rel_list)
    dim = r.shape[0]
    
    # add vocab lookup to leaves / answer
    print 'adding lookup'
    for tree in trees:
        for node in tree.get_nodes():
            node.ind = vocab.index(node.word.lower())

        tree.ans_ind = vocab.index(tree.ans)
        tree.ans_list = ans_list[ans_list != tree.ans_ind]

    # check gradient
    f_args = [trees, r, d, len(vocab), rel_list, lambdas]
    gradient_check(objective_and_grad, objective, dim, f_args)
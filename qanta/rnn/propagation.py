from numpy import *
from util.math_util import *
import random

# - QANTA's forward propagation. the labels argument indicates whether
#   you want to compute errors and deltas at each node or not. for training,
#   you obviously want those computations to occur, but during testing they
#   unnecessarily slow down feature computation
def forward_prop(params, tree, d, labels=True):

    tree.reset_finished()

    to_do = tree.get_nodes()

    (rel_dict, Wv, b, We) = params

    # - wrong_ans is 100 randomly sampled wrong answers for the objective function
    # - only need wrong answers when computing error
    if labels:
        random.shuffle(tree.ans_list)
        wrong_ans = [We[:, ind] for ind in tree.ans_list[0:100]]

    # forward prop
    while to_do:
        curr = to_do.pop(0)

        # node is leaf
        if len(curr.kids) == 0:

            # activation function is the normalized tanh
            curr.p = tanh(Wv.dot(curr.vec) + b)
            curr.p_norm = curr.p / linalg.norm(curr.p)
            curr.ans_error = 0.0

        else:

            # - root isn't a part of this! 
            # - more specifically, the stanford dep. parser creates a superficial ROOT node
            #   associated with the word "root" that we don't want to consider during training
            if len(to_do) == 0:
                ind, rel = curr.kids[0]
                curr.p = tree.get(ind).p
                curr.p_norm = tree.get(ind).p_norm
                curr.ans_error = 0.
                continue

            # check if all kids are finished
            all_done = True
            for ind, rel in curr.kids:
                if tree.get(ind).finished == 0:
                    all_done = False
                    break

            # if not, push the node back onto the queue
            if not all_done:
                to_do.append(curr)
                continue

            # otherwise, compute p at node
            else:
                kid_sum = zeros( (d, 1) )
                for ind, rel in curr.kids:
                    curr_kid = tree.get(ind)

                    try:
                        kid_sum += rel_dict[rel].dot(curr_kid.p_norm)

                    # - this shouldn't happen unless the parser spit out a seriously 
                    #   malformed tree
                    except KeyError:
                        print 'forward propagation error'
                        print tree.get_words()
                        print curr.word, rel, tree.get(ind).word
                
                kid_sum += Wv.dot(curr.vec)
                curr.p = tanh(kid_sum + b)
                curr.p_norm = curr.p / linalg.norm(curr.p)


        # error and delta
        if labels:
            curr.ans_error = 0.0
            curr.ans_delta = zeros( (d, 1) )

            base = 1 - tree.ans_vec.T.dot(curr.p_norm)
            delta_base = -1 * tree.ans_vec.flatten()

            rank = 1.0
            for ans in wrong_ans:
                err = max(0.0, base + ans.T.dot(curr.p_norm))
                if err > 0.0:
                    # WARP approximation of rank
                    rank = (len(wrong_ans) - 1) / rank

                    # multiply curr.ans_error and curr.ans_delta by 1/rank for WARP effect 
                    curr.ans_error += err
                    delta = delta_base + ans
                    curr.ans_delta += delta.reshape( (d, 1))
                    rank = 0.0

                rank += 1

        curr.finished = 1


# computes gradients for the given tree and increments existing gradients
def backprop(params, tree, d, len_voc, grads):

    (rel_dict, Wv, b) = params

    # start with root's immediate kid (for same reason as forward prop)
    ind, rel = tree.get(0).kids[0]
    root = tree.get(ind)

    # operate on tuples of the form (node, parent delta)
    to_do = [ (root, zeros( (d, 1) ) ) ]

    while to_do:
        curr = to_do.pop()
        node = curr[0]
        pd = curr[1]

        # internal node
        if len(node.kids) > 0:

            act = pd + node.ans_delta
            df = dtanh(node.p)
            node.delta_i = df.dot(act)

            for ind, rel in node.kids:

                curr_kid = tree.get(ind)
                grads[0][rel] += node.delta_i.dot(curr_kid.p_norm.T)
                to_do.append( (curr_kid, rel_dict[rel].T.dot(node.delta_i) ) )

            grads[1] += node.delta_i.dot(node.vec.T)
            grads[2] += node.delta_i
            grads[3][:, node.ind] += Wv.T.dot(node.delta_i).ravel()

        # leaf
        else:
            act = pd + node.ans_delta
            df = dtanh(node.p)

            node.delta_i = df.dot(act)
            grads[1] += node.delta_i.dot(node.vec.T)
            grads[2] += node.delta_i
            grads[3][:, node.ind] += Wv.T.dot(node.delta_i).ravel()
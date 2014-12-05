from numpy import *

def gradient_check(obj_and_grad, obj, dim, f_args):

    # f_args looks like [data, params, vector dim, vocab size, 
    #                    list of dependency relations, regularization lambdas]

    params = f_args[1]
    print 'checking gradients given parameter vector of dimensionality', dim
    
    cost, actual_grad = obj_and_grad(*f_args)
    num_grad = zeros(actual_grad.shape)

    mean = 2e-6 * ( (1 + linalg.norm(params)) / dim)

    for i in range(0, dim):
        curr_param = zeros( (num_grad.shape))
        curr_param[i] = 1
        curr_param = curr_param * mean
        f_args[1] = params + curr_param
        part_cost = obj(*f_args)
        num_grad[i] = (part_cost - cost) / mean
        print i, ' actual: ', num_grad[i], ' mine: ', actual_grad[i]
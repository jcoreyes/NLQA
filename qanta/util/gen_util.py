from numpy import *

# - given a vector containing all parameters, return a list of unrolled parameters
# - specifically, these parameters, as described in section 3 of the paper, are:
#   - rel_dict, dictionary of {dependency relation r: composition matrix W_r}
#   - Wv, the matrix for lifting a word embedding to the hidden space
#   - b, bias term
#   - We, the word embedding matrix
def unroll_params(arr, d, len_voc, rel_list):

    mat_size = d * d
    rel_dict = {}
    ind = 0

    for r in rel_list:
        rel_dict[r] = arr[ind: ind + mat_size].reshape( (d, d) )
        ind += mat_size

    Wv = arr[ind : ind + mat_size].reshape( (d, d) )
    ind += mat_size

    b = arr[ind : ind + d].reshape( (d, 1) )
    ind += d

    We = arr[ind : ind + len_voc * d].reshape( (d, len_voc))

    return [rel_dict, Wv, b, We]


# roll all parameters into a single vector
def roll_params(params, rel_list):
    (rel_dict, Wv, b, We) = params

    rels = concatenate( [rel_dict[key].ravel() for key in rel_list] )
    return concatenate( (rels, Wv.ravel(), b.ravel(), We.ravel() ) )


# randomly initialize all parameters
def gen_dtrnn_params(d, rels):
	"""
	Returns (dict{rels:[mat]}, Wv, b)
	"""

	r = sqrt(6) / sqrt(201)
	rel_dict = {}
	for rel in rels:
		rel_dict[rel] = random.rand(d, d) * 2 * r - r

	return (
		rel_dict,
		random.rand(d, d) * 2 * r - r,
		zeros((d, 1))
	)


# returns list of zero gradients which backprop modifies
def init_dtrnn_grads(rel_list, d, len_voc):

	rel_grads = {}
	for rel in rel_list:
		rel_grads[rel] = zeros( (d, d) )

	return [
		rel_grads,
		zeros((d, d)),
		zeros((d, 1)),
		zeros((d, len_voc))
		]


# random embedding matrix for gradient checks
def gen_rand_we(len_voc, d):
	r = sqrt(6) / sqrt(51)
	we = random.rand(d, len_voc) * 2 * r - r
	return we

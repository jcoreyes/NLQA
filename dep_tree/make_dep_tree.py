import os
from node import node
# the first goal is to run the parser on sentences and get the implicit tree 
# structure (as a list of relations)
parser_path = "../../Dropbox/stanford-parser/lexparser.sh"
# q_string = "Who was the last president of the Soviet Union?"


def make_tree(q_string):
	# run bash script and imp tree remains in the q_string_ann.txt file
	parserCommand = "echo \" " + q_string + "\" > q_string.txt & " + parser_path + " q_string.txt > q_string_ann.txt"
	os.system(parserCommand)


	relations = [line.strip() for line in open('q_string_ann.txt')]
	relations = relations[relations.index('')+1:len(relations)-1]

	# create tree as dictionary
	# 	- words are the keys
	#   - values are nodes that store:
	#   	- word
	#    	- parent
	#       - relation to parent
	#       - children of word
	tree = {}
	tree['ROOT'] = node(None, None, None, [])

	for r in relations: 
		rel = r[0:r.index('(')]
		parent = r[r.index('(')+1:r.index('-')]
		word = r[r.index(' ')+1:r.rindex('-')]


		if word in tree.keys():
			tree[word].parent = parent
			tree[word].relation = rel
		else: 
			tree[word] = node(word, parent, rel, [])

		if parent in tree.keys():
			tree[parent].children.append(word)
		else:
			tree[parent] = node(parent, None, None, [word])

	return tree
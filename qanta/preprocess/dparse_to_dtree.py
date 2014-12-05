from dtree_util import *
import sys, cPickle, random, os

## CAUTION: you will most likely have to fiddle around with these functions to
##          get them to do what you want. they are meant to help you get your data
##          into the proper format for QANTA. send me an email if you have any questions
##          miyyer@umd.edu


# - given a text file where each line is a question sentence, use the
#   stanford dependency parser to create a dependency parse tree for each sentence
def dparse(question_file):

    out_file = open('../data/raw_parses', 'w')

    # change these paths to point to your stanford parser.
    # make sure to use the lexparser.sh file in this directory instead of the default!
    parser_out = os.popen("~/Downloads/stanford_parser/lexparserqanta.sh " + question_file).readlines()
    for line in parser_out:
        out_file.write(line)

    out_file.close()


# - function that parses the resulting stanford parses
#   e.g., "nsubj(finalized-5, john-1)"
def split_relation(text):
    rel_split = text.split('(')
    rel = rel_split[0]
    deps = rel_split[1][:-1]
    if len(rel_split) != 2:
        print 'error ', rel_split
        sys.exit(0)

    else:
        dep_split = deps.split(',')

        # more than one comma (e.g. 75,000-19)
        if len(dep_split) > 2:

            fixed = []
            half = ''
            for piece in dep_split:
                piece = piece.strip()
                if '-' not in piece:
                    half += piece

                else:
                    fixed.append(half + piece)
                    half = ''

            print 'fixed: ', fixed
            dep_split = fixed

        final_deps = []
        for dep in dep_split:
            words = dep.split('-')
            word = words[0]
            ind = int(words[len(words) - 1])

            if len(words) > 2:
                word = '-'.join([w for w in words[:-1]])

            final_deps.append( (ind, word.strip()) )

        return rel, final_deps


# - given a list of all the split relations in a particular sentence,
#   create a dtree object from that list
def make_tree(plist):

    # identify number of tokens
    max_ind = -1
    for rel, deps in plist:
        for ind, word in deps:
            if ind > max_ind:
                max_ind = ind

    # load words into nodes, then make a dependency tree
    nodes = [None for i in range(0, max_ind + 1)]
    for rel, deps in plist:
        for ind, word in deps:
            nodes[ind] = word

    tree = dtree(nodes)

    # add dependency edges between nodes
    for rel, deps in plist:
        par_ind, par_word = deps[0]
        kid_ind, kid_word = deps[1]
        tree.add_edge(par_ind, kid_ind, rel)

    return tree  


# - given all dependency parses of a dataset as well as that dataset (in the same order),
#   dumps a processed dataset that can be fed into QANTA:
#   (vocab, list of dep. relations, list of answers, and dict of {fold: list of dtrees})
def process_question_file(raw_parses, question_file, answer_file):

    parses = open(raw_parses, 'r')
    #split = cPickle.load(open('../data/correct_simlit', 'rb'))
    split = makeSplits('', question_file, answer_file)
    parse_text = []
    new = False
    cur_parse = []
    for line in parses:

        line = line.strip()

        if not line:
            new = True

        if new:
            parse_text.append(cur_parse)
            cur_parse = []
            new = False

        else:
            # print line
            rel, final_deps = split_relation(line)
            cur_parse.append( (rel, final_deps) )

    print len(parse_text)

    # make mapping from answers: questions
    # and questions: [sentence trees]
    count = 0
    tree_dict = {}
    for key in split:
        hist = split[key]
        tree_dict[key] = []
        for text, ans, qid in hist:
            for i in range(0, len(text)):

                tree = make_tree(parse_text[count])
                tree.ans = ans.lower().replace(' ', '_').strip()
                tree.dist = i
                tree.qid = qid
                tree_dict[key].append(tree)
                count += 1

    vocab = []
    rel_list = []
    ans_list = []

    for key in tree_dict:
        print 'processing ', key
        qlist = tree_dict[key]
        for tree in qlist:
            if key == 'train':
                if tree.ans not in ans_list:
                    ans_list.append(tree.ans)

            if tree.ans not in vocab:
                vocab.append(tree.ans)

            tree.ans_ind = vocab.index(tree.ans)

            for node in tree.get_nodes():
                if node.word not in vocab:
                    vocab.append(node.word)
                
                node.ind = vocab.index(node.word)

                for ind, rel in node.kids:
                    if rel not in rel_list:
                        rel_list.append(rel)

    print 'rels: ', len(rel_list)
    print 'vocab: ', len(vocab)
    print 'ans: ', len(ans_list)

    cPickle.dump((vocab, rel_list, ans_list, tree_dict), open('../data/final_lit_split', 'wb'))

def makeSplits(qa_folder, questions_file, answers_file):
    splits = {}
    splits['train'] = makeSplit(questions_file, answers_file)
    return splits

def makeSplit(questions_file, answers_file):
    answers = open(answers_file, 'r')
    questions = open(questions_file, 'r')
    qid = 1
    split = []
    for question in questions:
        answer = answers.readline()
        split.append([[question], answer, qid])
        qid += 1
    return split


if __name__ == '__main__':
    question_file = '/home/coreyesj/Downloads/qanta/data/questions'
    answer_file = '/home/coreyesj/Downloads/qanta/data/answers'
    dparse(question_file)
    process_question_file('/home/coreyesj/Downloads/qanta/data/raw_parses', question_file, answer_file)
from classify.learn_classifiers import evaluate
import argparse

## - evaluate QANTA's learned representations on history questions
##   and compare performance to bag of words and dependency relation baselines
## - be sure to train a model first by running qanta.py

if __name__ == '__main__':
    
    # command line arguments
    parser = argparse.ArgumentParser(description='QANTA evaluation')
    parser.add_argument('-data', help='location of dataset', default='data/hist_split')
    parser.add_argument('-model', help='location of trained model', default='models/hist_params')
    parser.add_argument('-d', help='word embedding dimension', type=int, default=100)

    args = vars(parser.parse_args())

    print 'qanta performance: '
    evaluate(args['data'], args['model'], args['d'], rnn_feats=True, \
              bow_feats=False, rel_feats=False)

    print '\n\n\n bow performance: '
    evaluate(args['data'], args['model'], args['d'], rnn_feats=False, \
              bow_feats=True, rel_feats=False)

    print '\n\n\n bow-dt performance: '
    evaluate(args['data'], args['model'], args['d'], rnn_feats=False, \
              bow_feats=True, rel_feats=True)
''' Main query engine which calls upon different models
and selects the model that returns the best answer
'''
import sys
sys.path.append('/home/jcoreyes/NLQA')
sys.path.append('/home/coreyesj/Dropbox/NLQA')
import quepy_test.quepy_api.quepymain as quepymain
import word2vec_test.word2vec_api.word2vecmain as word2vecmain

DEFAULT_RESPONSE = "It depends."
def query(question):
    try:

        # Run fastest model first which is quepy and return if a valid answer
        quepyAnswer = quepymain.query(question)
        if quepyAnswer != DEFAULT_RESPONSE:
            return quepyAnswer
        # Run next model which is basic word2vec
        else:
            return word2vecmain.query(question)
    except:
        return "word2Vec Failed"

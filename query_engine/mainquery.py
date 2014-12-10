''' Main query engine which calls upon different models
and selects the model that returns the best answer
'''
import sys
sys.path.append('/home/jcoreyes/NLQA/quepy_test/quepy_api')
import quepymain
DEFAULT_RESPONSE = "It depends."
def query(question):
    # Run fastest model first which is quepy and return if a valid answer
    quepyAnswer = quepymain.query(question)
    if quepyAnswer != DEFAULT_RESPONSE:
        return quepyAnswer
    # Run next model which is basic word2vec
    else:
        return DEFAULT_RESPONSE


from RAKE.rake import Rake
from gensim.models import TfidfModel, Word2Vec

def QA(question):
	# model = Word2Vec.load('out')
	model = Word2Vec.load_word2vec_format('/home/david/Work/googlenews.bin', binary=True)
	extractor = Rake()
	words = extractor.run(question)
	keywords = [words[i][0] for i in xrange(len(words))]

	return model.most_similar(positive=keywords)[0][0]
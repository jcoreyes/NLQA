from RAKE.rake import Rake
from gensim.models import TfidfModel, Word2Vec

def query(question):
	model = Word2Vec.load('/home/jcoreyes/news_model')
	extractor = Rake()
	words = extractor.run(question)
	keywords = [words[i][0] for i in xrange(len(words))]

	return model.most_similar(positive=keywords)[0][0]
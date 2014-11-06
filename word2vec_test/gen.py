import logging
import os.path
import sys

from gensim.corpora import WikiCorpus
from gensim.models import TfidfModel, Word2Vec

if __name__ == '__main__':
    program = os.path.basename(sys.argv[0])

    logger = logging.getLogger(program)

    logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s')
    logging.root.setLevel(level=logging.INFO)
    logger.info("running %s" % ' '.join(sys.argv))

    if len(sys.argv) < 3:
        print globals()['__doc__'] % locals()
        sys.exit(1)

    inp, outp = sys.argv[1:3]

    wiki = WikiCorpus(inp, dictionary={})
    model = Word2Vec(size=300, window=5, min_count=5, workers=3)
    sentences = wiki.get_texts()
    model.build_vocab(sentences)
    sentences = wiki.get_texts()
    model.train(sentences)
    model.init_sims(replace=True)
    model.save(outp)

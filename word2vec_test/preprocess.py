__author__ = 'coreyesj'
"""
Script to preprocess wikipedia corpus and combine proper nouns
Abraham Lincoln -> Abraham_Lincoln
"""
import nltk
from nltk.tag import pos_tag


def mergeNouns(text):
    """ Merge the proper nouns in a string of text"""
    newText = []
    for sentence in text.split('. '):
        newSentence = []
        for word in sentence.split(' '):
            print newSentence
            if len(word) < 1 or not word[0].isupper():
                newSentence.append(word)
                continue
            if len(newSentence) > 0:
                prev = newSentence[-1]
                if len(prev) > 0 and prev[0].isupper():
                    newSentence[-1] = prev + "_" + word
                    continue
            newSentence.append(word)
        newText.append(" ".join(newSentence))
    print ". ".join(newText)
    return ". ".join(newText)

if __name__ == '__main__':
    # nltk.data.path.append("/home/coreyesj/PycharmProjects/NLQA/quepy_test/nltkdata")
    #
    sentence = "Michael Jackson likes to eat at McDonalds. Abraham Lincoln's shot himself."
    # tagged_sent = pos_tag(sentence.split())
    # # [('Michael', 'NNP'), ('Jackson', 'NNP'), ('likes', 'VBZ'), ('to', 'TO'), ('eat', 'VB'), ('at', 'IN'), ('McDonalds', 'NNP')]
    #
    # propernouns = [word for word,pos in tagged_sent if pos == 'NNP']
    print mergeNouns(sentence)
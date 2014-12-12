__author__ = 'coreyesj'
def mergeNouns(text):
    """ Merge the proper nouns in a string of text"""
    newText = []
    for sentence in text.split('. '):
        newSentence = []
        for word in sentence.split(' '):
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
    return ". ".join(newText)
text = 'test'



print mergeNouns(text)
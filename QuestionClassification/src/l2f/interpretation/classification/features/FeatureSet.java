package l2f.interpretation.classification.features;

public enum FeatureSet {

    //Frequency features
    UNIGRAM,
    BIGRAM,
    TRIGRAM,
    LENGTH,
    POS,
    NER_REPL,
    NER_INCR,
    HEADWORD,
    CATEGORY,
    WORD_SHAPE,
    IMPORTANT_WORDS_LIST,

    //Binary features: either exist or not in each sample
    BINARY_UNIGRAM,
    BINARY_BIGRAM,
    BINARY_TRIGRAM,
    BINARY_POS,
    BINARY_NER_REPL,
    BINARY_NER_INCR,
    BINARY_WORD_SHAPE,

    //My dummy feature
    DUMMY

    /** word unigrams
     * word bigrams
     * word trigrams
     * word unigrams with n-first threshold
     * number of tokens in the question

     * part-of-speech tags
     * named entities (replace)
     * named entities (incremental)

     * question focus
     * target synset of question focus (+ adjective attribute)
     * automatically extracted semantically important words
     * semantically related words of Li&Roth*/
}

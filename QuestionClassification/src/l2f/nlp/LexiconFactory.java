package l2f.nlp;

public enum LexiconFactory {

    INSTANCE;
    Lexicon lexicon = null;

    public Lexicon getLexicon() {


        if (lexicon == null) {

            lexicon = new WordNet();
        }
        return lexicon;
    }
}

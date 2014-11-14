package l2f.interpretation;

import java.util.List;

import edu.berkeley.nlp.ling.Tree;

/**
 * AnalyzedQuestion is a mutable class used to gather information that is
 * acquired after applying several NLP processing tools over the original
 * question, such as POS tags, parse tree, and question tokens.
 * 
 */
public class AnalyzedQuestion {

    /**
     * Question in its original form.
     */
    private String originalQuestion;
    /**
     * Parse tree of the question.
     */
    private Tree<String> parseTree;
    /**
     * String representation of the parse tree of the question.
     */
    private String parseTreeString;
    /**
     * Tokenized version of the question.
     */
    private List<String> tokens;
    /**
     * Part-of-speech tags of the tokens.
     */
    private List<String> posTags;
    /**
     * Headword of the question.
     */
    private String headword;
    private List<String> headwordSynonyms;
    /**
     * Compound Headword of the question, if available.
     */
    private String compoundHeadword;
    /**
     * The target of the headword, according to a <code>LexiconMap</code>.
     */
    private String headwordLexiconTarget;
    /**
     * Word(s) extracted by patterns, and used for non-factoid questions and
     * questions about acronyms and counts.
     */
    private String defWord;
    /**
     * Creates a new instance of AnalyzedQuestion.
     * @param question original question
     */
    private String heuristicForHeadwordExtaction = "";
    
    public AnalyzedQuestion(String question) {
        this.originalQuestion = question;
    }

    public String getOriginalQuestion() {
        return originalQuestion;
    }

    public void setOriginalQuestion(String originalQuestion) {
        this.originalQuestion = originalQuestion;
    }

    public Tree<String> getParseTree() {
        return parseTree;
    }

    public String getParseTreeString() {
        return parseTreeString;
    }

    public void setParseTree(Tree<String> parseTree) {
        this.parseTree = parseTree;
        this.parseTreeString = parseTree.toString();
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getPosTags() {
        return posTags;
    }

    public void setPosTags(List<String> posTags) {
        this.posTags = posTags;
    }

    public String getHeadword() {
        return headword;
    }

    public void setHeadword(String headword) {
        this.headword = headword;
    }

    public List<String> getHeadwordSynonyms() {
        return headwordSynonyms;
    }

    public void setHeadwordSynonyms(List<String> headwordSynonyms) {
        this.headwordSynonyms = headwordSynonyms;
    }

    public void setCompoundHeadword(String compoundHeadword) {
        this.compoundHeadword = compoundHeadword;
    }

    public String getCompoundHeadword() {
        return compoundHeadword;
    }

    public void setHeadwordLexiconTarget(String headwordLexiconTarget) {
        this.headwordLexiconTarget = headwordLexiconTarget;
    }

    public String getHeadwordLexiconTarget() {
        return headwordLexiconTarget;
    }

    public void setDefWord(String defWord) {
        this.defWord = defWord;
    }

    public String getDefWord() {
        return defWord;
    }

    public String getHeuristicForHeadwordExtaction() {
        return heuristicForHeadwordExtaction;
    }

    public void setHeuristicForHeadwordExtaction(
            String heuristicForHeadwordExtaction) {
        this.heuristicForHeadwordExtaction = heuristicForHeadwordExtaction;
    }

}

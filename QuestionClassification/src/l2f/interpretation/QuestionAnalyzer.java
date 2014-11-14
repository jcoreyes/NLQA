package l2f.interpretation;

import java.util.List;

import com.aliasi.util.Pair;

import net.didion.jwnl.data.POS;

import edu.berkeley.nlp.ling.Tree;

import l2f.config.Config;
import l2f.nlp.Lexicon;
import l2f.nlp.LexiconFactory;
import l2f.nlp.Parser;
import l2f.nlp.ParserFactory;
import l2f.nlp.Tokenizer;
import l2f.nlp.TokenizerFactory;
import l2f.nlp.TokenizerFactory.TokenizerType;
import l2f.utils.Utils;

public class QuestionAnalyzer {

    private static final int CARDINATILITY = 32;

    /**
     * Question tokenizer.
     */
    private final Tokenizer tokenizer;
    /**
     * Parser used to infer the syntactic structure of the question.
     */
    private final Parser parser;
    /**
     * QuestionFocusExtractor used to determine the focus of the question.
     * @see QuestionFocusExtractor QuestionFocusExtractor
     */
    private final QuestionFocusExtractor focusExtractor;
    /**
     * LexiconMap is used to map a word into a category.
     * @see LexiconMap LexiconMap
     */
    private final LexiconMap lexiconMap;
    /**
     * Lexicon used in combination with LexiconMap.
     */
    private final Lexicon lexicon;

    /**
     * If all the senses (in WordNet) of the focus should be found,
     * or only 1st sense.
     */
    private boolean allsenses = false;

    /**
     * Instantiates a Question Analyzer, using language specific
     * configurations.
     */
    public QuestionAnalyzer() {
        this.tokenizer = TokenizerFactory.newTokenizer(TokenizerType.valueOf(Config.questionAnalysis_tokenizerType));
        this.parser = ParserFactory.INSTANCE.getParser(Config.questionAnalysis_parserGrammarFile);
        this.focusExtractor = new QuestionFocusExtractor();
        this.lexiconMap = new LexiconMap(Config.questionAnalysis_lexiconmapFile);
        this.lexicon = LexiconFactory.INSTANCE.getLexicon();

    }

    public QuestionAnalyzer(boolean allsenses) {
        this.tokenizer = TokenizerFactory.newTokenizer(TokenizerType.valueOf(Config.questionAnalysis_tokenizerType));
        this.parser = ParserFactory.INSTANCE.getParser(Config.questionAnalysis_parserGrammarFile);
        this.focusExtractor = new QuestionFocusExtractor();
        this.lexiconMap = new LexiconMap(Config.questionAnalysis_lexiconmapFile);
        this.lexicon = LexiconFactory.INSTANCE.getLexicon();
        this.allsenses = allsenses;
    }

    /**
     * Analyzes the original question, returning a container
     * that holds all the information that was gathered, namely,
     * question tokens, parse tree, POS tags, and headword.
     *
     * @param question
     * @return an analyzed question
     */
    public AnalyzedQuestion analyze(String question) {
        AnalyzedQuestion aq = new AnalyzedQuestion(question);
        List<String> tokens = tokenize(question);
        Tree<String> parse = parse(tokens);
        List<String> tags = tag(parse);
        aq.setTokens(tokens);
        aq.setPosTags(tags);
        aq.setParseTree(parse);

        String headword = focusExtractor.extract(aq);
        if (headword != null && headword.indexOf("#QP#") != -1) {
            aq.setHeadword("");
            aq.setHeadwordLexiconTarget(headword.replaceFirst("#QP#", ""));
            return aq;
        }
        aq.setHeadword(headword);
        aq.setHeadwordSynonyms(lexicon.getSynonyms(headword));


        // TARGET LEXICON TODO: CLEAN THIS CLEAN THIS CLEAN THIS mess

        if (aq.getCompoundHeadword() != null) {
            Pair<Long, String> pair = lexicon.intersectMap(aq.getCompoundHeadword(),
                    POS.NOUN, lexiconMap, allsenses);
            if (pair != null) {
                aq.setHeadwordLexiconTarget(pair.b().replaceAll(":::.*", ""));
                aq.setHeuristicForHeadwordExtaction(aq.getHeuristicForHeadwordExtaction() + pair.b().replaceFirst(".*:::", ":::"));
            }

        }

        if (aq.getHeadwordLexiconTarget() == null) {
            int indexHeadword = tokens.indexOf(headword);
            if (indexHeadword >= 0) {
                String posHeadword = tags.get(tokens.indexOf(headword));
                String wnPos = Utils.pennPOSToWordnetPOS(posHeadword);
                if (wnPos == null) {
                    System.err.println("Extracted a Non NN Headword: " + posHeadword + "-"+ headword + ".");
                    return aq;
                }
                Pair<Long, String> pair = lexicon.intersectMap(headword,
                        POS.getPOSForLabel(wnPos),
                        lexiconMap,
                        allsenses);
                if (pair != null) {
                    aq.setHeadwordLexiconTarget(pair.b().replaceAll(":::.*", ""));
                    aq.setHeuristicForHeadwordExtaction(aq.getHeuristicForHeadwordExtaction() + pair.b().replaceFirst(".*:::", ":::"));
                }
            }
        }
        return aq;
    }

    public List<String> tokenize(String question) {
        return tokenizer.tokenize(question);
    }

    public List<String> tag(Tree<String> tree) {
        return tree.getPreTerminalYield();
    }

    public Tree<String> parse(List<String> tokens, List<String> tags) {
        return parser.getBestParse(tokens, tags);
    }

    public Tree<String> parse(List<String> tokens) {
        return parser.getBestParse(tokens);
    }

    public int cardinality(List<String> tokens, List<String> tags, String headword) {
        int cardinality = 1;
        int headwordPOSidx = tags.indexOf(headword);
        if (headwordPOSidx != -1) {
            String headwordPOS = tags.get(headwordPOSidx);
            if (headwordPOS.substring(headwordPOS.length() - 1).equals(Utils.PLURAL_SUFFIX)) {
                int cardinalIdx = tags.indexOf(Utils.CARDINAL);
                if (cardinalIdx != -1) {
                    try {
                        cardinality = Integer.parseInt(tokens.get(cardinalIdx));
                    } catch (NumberFormatException nfe) {
                        cardinality = CARDINATILITY;
                        
                    }
                } else {
                    cardinality = CARDINATILITY; 
                }
            }
        }
        return cardinality;
    }
}

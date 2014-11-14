package l2f.nlp;

import java.util.List;

import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.TreeAnnotations;
import edu.berkeley.nlp.ling.Tree;

/**
 * "Wrapper" for the Berkeley Parser, which requires a serialized
 * grammar as input. It infers the grammatical structure of a 
 * given sentence, yielding a parse tree as a result. 
 * 
 * For further information on training, refer to the README file
 * of the Berkeley Parser.
 * 
 */
public class Parser extends CoarseToFineMaxRuleParser {

	public Parser(Grammar gr, Lexicon lex, double unaryPenalty, int endL,
			boolean viterbi, boolean sub, boolean score, boolean accurate,
			boolean variational, boolean useGoldPOS, boolean initializeCascade) {
		super(gr, lex, unaryPenalty, endL, viterbi, sub, score, accurate, variational,
				useGoldPOS, initializeCascade);
	}
	
	@Override
	public Tree<String> getBestParse(List<String> sentence) {
		Tree<String> parsedTree = super.getBestParse(sentence);
		parsedTree = TreeAnnotations.unAnnotateTree(parsedTree);
		return parsedTree;
	}
	
	/**
	 * Parses a sentence using "gold" part-of-speech tags.
	 * @param sentence list of tokens
	 * @param posTags list of part-of-speech tags for each token
	 * @return a parse tree
	 */
	public Tree<String> getBestParse(List<String> sentence, List<String> posTags) {
		Tree<String> parsedTree = super.getBestConstrainedParse(sentence, posTags, false);
		if (parsedTree.getChildren().isEmpty()) {
			System.err.println("Warning: unable to use gold pos tags in '" + sentence + "'.");
			parsedTree = super.getBestConstrainedParse(sentence, null, false);
		}
		parsedTree = TreeAnnotations.unAnnotateTree(parsedTree);
		return parsedTree;
	}
}

package l2f.interpretation;

import java.util.List;
import java.util.regex.Pattern;

import l2f.nlp.CompoundWordExtractor;
import l2f.nlp.LexiconFactory;
import l2f.nlp.QuestionHeadFinder;
import l2f.utils.Utils;
import edu.berkeley.nlp.ling.HeadFinder;
import edu.berkeley.nlp.ling.Tree;

/**
 * <code>QuestionFocusExtractor</code> extracts the focus of the question,
 * which can be either the question's headword, according to a
 * <code>HeadFinder</code> implementation, or a placeholder for certain
 * questions that match a specific pattern and don't require headword
 * extraction.
 * 
 * This class should be subclassed if the predefined patterns don't suit
 * the needs of the application, such as when extracting the question focus
 * of a different language than English.
 * 
 */
public class QuestionFocusExtractor {

	/**
	 * HeadFinder used to determine the headword of the question.
	 */
	private HeadFinder questionHeadFinder;
	/**
	 * Extracts a compound word from the headword, if possible.
	 */
	private CompoundWordExtractor compoundWordExtractor;
	/**
	 * Matches the question against a set of regular expressions to detect
	 * questions that don't require headword extraction.
	 */
	private QuestionPatternFinder patternFinder = new QuestionPatternFinder();

	public QuestionFocusExtractor() {
		this.questionHeadFinder = new QuestionHeadFinder();
		this.compoundWordExtractor = new CompoundWordExtractor(LexiconFactory.INSTANCE.getLexicon());
	}

	public String extract(AnalyzedQuestion question) {
		String questionFocus = this.patternFinder.find(question);
		if (!questionFocus.isEmpty()) {
			return "#QP#" + questionFocus;
		}
		if (questionFocus.isEmpty()) {
			if (question.getParseTreeString().equals("(ROOT)") || question.getParseTree().isLeaf() || question.getParseTree().isLeaf()) {
				System.err.println("*******");
				System.err.println(question.getOriginalQuestion());
				System.err.flush();
				return "cash crop";
			}
			Tree<String> head = Utils.headPreTerminal(this.questionHeadFinder, question.getParseTree());
			String headword = null;
			if (head.getLabel().matches("NNP?S?|WP")) {
				headword = head.getTerminals().get(0).getLabel();
				String compound = this.compoundWordExtractor.tryGetCompoundWord(question.getTokens(), question.getPosTags(), headword);
				if (!compound.equals(headword)) {
					question.setCompoundHeadword(compound);
					question.setHeuristicForHeadwordExtaction("compound (" + headword + "," + compound + ")");
				} else {
					question.setHeuristicForHeadwordExtaction("single (" + headword + ")");
				}
			} else {
				List<String> tags = question.getPosTags();
				for (int i = 0; i < tags.size(); i++) {
					if (tags.get(i).matches(("NN"))) {
						headword = question.getTokens().get(i);
						question.setHeuristicForHeadwordExtaction("single (" + headword + ")");
						break;
					}
				}
			}
			if (headword == null) {
				question.setHeuristicForHeadwordExtaction("no headword");
			}
			questionFocus = headword;
		}
		return questionFocus;
	}

	/**
	 * Extract the focus of a given question.
	 * First, tries to apply specific, hand-coded rules, to find the head word.
	 * Then, if it can't be found, we apply the syntactic question head finder.
	 * @param question the question for which the focus is being extracted
	 * @return question focus
	 */
	public String extract(String question, List<String> tokens, Tree<String> tree) {
		String questionFocus = applyPatterns(tokens, question);
		if (questionFocus.isEmpty()) {
			Tree<String> head = Utils.headTerminal(this.questionHeadFinder, tree);
			String headword = head.getLabel();
			questionFocus = headword;
			// if POS != NN OR WP Whom (confirm if it appears just with Whom)
			// then: extract first NN or NNS, create util method,
			// then: extract JJ ??
			//(ROOT (S (NP (NNP Colin)(NNP Powell))(VP (VBZ is)(ADJP (RBS most)(JJ famous)(WHPP (IN for)(WHNP (WP what)))))(. ?)))
			// famous WORD => DESC:REASON : X
		}
		return questionFocus;
	}
	private static Pattern DEFINITION_A = Pattern.compile("What(?:'s|\\s+(?:is|are))\\s+(?:an?)\\s+\\w{1,2}", Pattern.CASE_INSENSITIVE);

	// QuestionPatternFinder Interface apply => String
	// Adapted from (xxx, 2009).
	private String applyPatterns(List<String> tokens, String question) {
		/*String first = tokens.get(0);
        if (first.equalsIgnoreCase("when") || first.equalsIgnoreCase("where") ||
        first.equalsIgnoreCase("who") || first.equalsIgnoreCase("how") ||
        first.equalsIgnoreCase("why")) {
        return first;
        }*/
		if (DEFINITION_A.matcher(question).matches()) {
			System.err.println("DEFINITON=" + question);
			return "def#A";
		}
		//Pattern.compile("wh(?:en|ere|o|y)|how");
		return Utils.EMPTY;
	}
}

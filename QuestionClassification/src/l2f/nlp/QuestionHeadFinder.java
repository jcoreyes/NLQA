package l2f.nlp;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


import edu.berkeley.nlp.ling.AbstractCollinsHeadFinder;
import edu.berkeley.nlp.ling.PennTreebankLanguagePack;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.TreebankLanguagePack;
import edu.berkeley.nlp.ling.Trees;

/**
 * Implements a 'question semantic head' variant of the the HeadFinder found
 * in Michael Collins' 1999 dissertation (236-238). For an explanation about
 * tags that are used, @see <a href="http://bulba.sdsu.edu/jeanette/thesis/PennTags.html">here</a>.
 *
 * The new rules can be found in the method @see#ruleChanges.
 */
public class QuestionHeadFinder extends AbstractCollinsHeadFinder {

    /**
     * Words that require a postfix operation.
     */
    private static Pattern POSTFIX_WORD = Pattern.compile("(?:name|kind|type|part|genre|group)s?",
            Pattern.CASE_INSENSITIVE); // part (of the body)
    /**
     * Prepositions/IN that require a postfix operation.
     */
    private static Pattern POSTFIX_IN = Pattern.compile("for|of",
            Pattern.CASE_INSENSITIVE);
    /**
     * Labels that possibly contain a word that requires a postfix operation.
     */
    private static Pattern REQUIRE_POSTFIX = Pattern.compile("NP|NNS?|WHNP");

    //What relative of the racoon .. relative head word (groups|relatives)
    public QuestionHeadFinder() {
        this(new PennTreebankLanguagePack());
    }

    @SuppressWarnings("unchecked")
    protected QuestionHeadFinder(TreebankLanguagePack tlp) {
        super(tlp);
        nonTerminalInfo = new HashMap();
        // This version from Collins' diss (1999: 236-238)
        // NNS, NN is actually sensible (money, etc.)!
        // QP early isn't; should prefer JJR NN RB
        // remove ADVP; it just shouldn't be there.
        // NOT DONE: if two JJ, should take right one (e.g. South Korean)

        nonTerminalInfo.put("ADVP", new String[][]{{"right", "RB", "RBR", "RBS", "FW", "ADVP", "TO", "CD", "JJR", "JJ", "IN", "NP", "JJS", "NN"}});

        nonTerminalInfo.put("INTJ", new String[][]{{"left"}});
        nonTerminalInfo.put("LST", new String[][]{{"right", "LS", ":"}});
        nonTerminalInfo.put("NAC", new String[][]{{"left", "NN", "NNS", "NNP", "NNPS", "NP", "NAC", "EX", "$", "CD", "QP", "PRP", "VBG", "JJ", "JJS", "JJR", "ADJP", "FW"}});
        nonTerminalInfo.put("NX", new String[][]{{"right", "NP", "NX"}});


        nonTerminalInfo.put("PRT", new String[][]{{"right", "RP"}});

        nonTerminalInfo.put("RRC", new String[][]{{"right", "VP", "NP", "ADVP", "ADJP", "PP"}});


        nonTerminalInfo.put("X", new String[][]{{"right", "S", "VP", "ADJP", "NP", "SBAR", "PP", "X"}});

        nonTerminalInfo.put("ADJP", new String[][]{{"left", /*"PP", */ "$", "JJ", "NNS", "NN", "QP", "VBN", "VBG", "ADJP", "JJR", "NP", "JJS", "DT", "FW", "RBR", "RBS", "SBAR", "RB"}});

        nonTerminalInfo.put("QP", new String[][]{{"right", "$", "NNS", "NN", "CD", "JJ", "PDT", "DT", "IN", "RB", "NCD", "QP", "JJR", "JJS"}});

        nonTerminalInfo.put("UCP", new String[][]{{"left"}});

        nonTerminalInfo.put("CONJP", new String[][]{{"right", "TO", "RB", "IN", "CC"}});

        nonTerminalInfo.put("PRN", new String[][]{{"left", "VP", "S", "SINV", "SBAR", "NP", "ADJP", "PP", "ADVP", "INTJ", "WHNP", "NAC", "VBP", "JJ", "NN", "NNP"}});

        nonTerminalInfo.put("POSSP", new String[][]{{"right", "POS"}});

        nonTerminalInfo.put("ROOT", new String[][]{{"left", "S", "SQ", "SINV", "SBAR", "FRAG"}});

        nonTerminalInfo.put("TYPO", new String[][]{{"left", "NN", "NP", "NNP", "NNPS", "TO",
                        "VBD", "VBN", "MD", "VBZ", "VB", "VBG", "VBP", "VP", "ADJP", "FRAG"}});

        nonTerminalInfo.put("ADV", new String[][]{{"right", "RB", "RBR", "RBS", "FW",
                        "ADVP", "TO", "CD", "JJR", "JJ", "IN", "NP", "JJS", "NN"}});

        nonTerminalInfo.put("EDITED", new String[][]{{"left"}});

        nonTerminalInfo.put("XS", new String[][]{{"right", "IN"}}); // rule for new structure in QP
        ruleChanges();
    }

    /**
     * Question specific rules.
     */
    @SuppressWarnings("unchecked")
    private void ruleChanges() {
        nonTerminalInfo.put("NP", new String[][]{{"rightdis", /*"WHPP", */ "NP", "NN", "NNP", "NNPS", "NNS", "NX", "JJR"}, {"right", "NP", "PRP"}, {"rightdis", "$", "ADJP", "PRN"}, {"right", "CD"}, {"rightdis", "JJ", "JJS", "RB", "QP", "DT", "WDT", "RBR", "ADVP"}, {"left", "POS"}});

        // VP: we don't want verbs for the question head word, just names
        // VP: "(SBARQ (WHNP ...) (SQ (VP (VBZ is) (NP ...))))" -> we want NP instead of VBZ
        // VP: "(SBARQ (NP the numbering system that we use today) (VP was (VP introduced to the western world) (WHPP by what culture)))" -> we want 'culture' inside last WHPP
        nonTerminalInfo.put("VP", new String[][]{{"right", "WHPP", "PP", "WHNP"}, {"left", "S", "ADJP", "NN", "NNS", "NNP", "NP", "VP"}, {"right", "WHPP", "PP", "WHNP"}});

        // S, SBAR, SBARQ
        nonTerminalInfo.put("S", new String[][]{{"left", "VP", "S", "FRAG", "SBAR", "ADJP", "UCP", "TO"}, {"right", "NP"}});
        nonTerminalInfo.put("SBAR", new String[][]{{"left", "S", "SQ", "SINV", "SBAR", "FRAG", "WHNP", "WHPP", "WHADVP", "WHADJP", "IN", "DT"}});
        nonTerminalInfo.put("SBARQ", new String[][]{{"left", "SQ", "S", "SINV", "SBARQ", "FRAG"}});

        // SQ: again, we want noun phrases instead of verb phrases
        nonTerminalInfo.put("SQ", new String[][]{{"left", "NP", "VP", "NP", "SQ", "VB", "VBZ", "VBD", "VBP", "MD"}});

        // PP: we look for the NP or SBAR
        // PP: "(PP (IN by) (SBAR (WHNP (WP what) (NN culture))))" -> we want SBAR to get 'culture'
        nonTerminalInfo.put("PP", new String[][]{{"left", "WHNP", "NP", "WHADVP", "SBAR", "S"}, {"right", "IN", "TO", "VBG", "VBN", "RP", "FW"}, {"left", "PP"}});

        // WHNP: clauses should have the same sort of head as an NP
        // WHNP: "(SBARQ (WHNP (WP What) (NN country)))" -> we want NN
        nonTerminalInfo.put("WHNP", new String[][]{{"left", /*"WHNP",*/ "NP"}, {"rightdis", "NN", "NNP", "NNPS", "NNS", "NX", "POS", "JJR"}, {"rightdis", "$", "ADJP", "PRN"}, {"right", "CD"}, {"rightdis", "JJ", "JJS", "RB", "QP"}, {"left", "WHNP", "WHPP", "WHADJP", "WP$", "WP", "WDT"}});

        // WHPP: Prepositional phrase containing a wh-noun phrase (such as of which or by whose authority) that either introduces a PP gap or is contained by a WHNP.
        // WHPP: "(WHPP At (WHNP what (NN speed))) does the Earth revolve around the Sun?" -> we want NN inside WHNP
        nonTerminalInfo.put("WHPP", new String[][]{{"right", "WHNP", "WHADVP", "NP", "SBAR"}});

        // SINV: Inverted declarative sentence, i.e. one in which the subject follows the tensed verb or modal.
        // SINV: "What (VBP are) (NP Canada's two (NNS territories))" -> we want NNS that is inside NP
        nonTerminalInfo.put("SINV", new String[][]{{"left", "NP", "VP", "S", "SINV", "ADJP", "VBZ", "VBD", "VBP", "VB", "MD"}});

        // WHADVP: Introduces a clause with an NP gap. May be null or lexical, containing a wh-adverb: when, where, whence, whereby, wherein, whereupon, how and why (more common).
        // WHADVP: "(WHADVP (WRB How) (RB long))" -> long
        nonTerminalInfo.put("WHADVP", new String[][]{{"right", "RB", "JJ"}});
        // WHADJP:
        nonTerminalInfo.put("WHADJP", new String[][]{{"left", "ADJP", "JJ", "WRB", "CC"}});

        // FRAG: "(FRAG (PP During World War II) , (SBAR Who was the president of the USA))" -> SBAR
        nonTerminalInfo.put("FRAG", new String[][]{{"left", "SBAR", "S", "SQ", "SINV", "ADJP", "ADVP", "FRAG"}});
    }

    @Override
    protected int postOperationFix(int headIdx, List<Tree<String>> daughterTrees) {
        if (daughterTrees.size() > headIdx + 1) {
            Tree<String> daughter = daughterTrees.get(headIdx);
            // 1st: check if the current node may require a post operation fix
            if (REQUIRE_POSTFIX.matcher(daughter.getLabel()).matches()) {
                List<Tree<String>> kids = daughter.getChildren();
                Tree<String> lastKid = kids.get(kids.size() - 1);
                // 2nd: either the daughter is a pre-terminal or the last kid is:
                // e.g., (NP (NN kind) (PP (IN of) animal))) => daughter = (NN kind) pre-terminal; lastKid = kind
                // e.g., (NP the (NN name)) (PP (IN for)) => daughter = NP; lastKid = (NN name) pre-terminal
                if (daughter.isPreTerminal() || lastKid.isPreTerminal()) {
                    String lastLabel = lastKid.getTerminals().get(0).getLabel();
                    // 3nd: check if the last word requires a postfix
                    // 4th: look for the presence of a *-PP postmodifier
                    if (POSTFIX_WORD.matcher(lastLabel).matches()
                            && daughterTrees.get(headIdx + 1).getLabel().equals("PP")) {
                        List<Tree<String>> ppKids = daughterTrees.get(headIdx + 1).getChildren();
                        Tree<String> pp = ppKids.get(0);
                        // 5th: make sure that the preposition really requires a postfix
                        // e.g., What is (NP the longest place (NN name)) (PP (IN in) (NP the U.S.))) => "in" doesn't require
                        // e.g.,(NP (NN kind) (PP (IN of) (NP (NN animal)))) => "of" requires
                        if (ppKids.size() > 1 && pp.getLabel().equals("IN")
                                && POSTFIX_IN.matcher(pp.getTerminals().get(0).getLabel()).matches()) {
                            return headIdx + 1;
                        }
                    }
                    
                }
            }
        }
        return headIdx;
    }

    /**
     * Determine which daughter of the current parse tree is the
     * head. Uses special rules for SBARQ, WHNP, and NP.
     *
     * @param t The parse tree to examine the daughters of.
     *          This is assumed to never be a leaf
     * @return The parse tree that is the head
     */
    protected Tree<String> determineNonTrivialHead(Tree<String> t) {
        String motherCat = Trees.FunctionNodeStripper.transformLabel(t);
        if (motherCat.equals("SBARQ")) {	// Wh- + N => e.g., What person SQ => we want person
            List<Tree<String>> kids = t.getChildren();
            String[] how = new String[]{"left", "WHNP", "WHADJP", "WHPP", "WHADVP"}; // (ROOT (SBARQ (NP // CREATE AS PRIVATE FIELD
            Tree<String> pti = traverseLocate(kids, how, false);
            if (pti != null && pti.getChildren().size() > 1) {
                return pti;
            }
        } else if (motherCat.equals("WHNP")) { // Wh- + (NP N POS) + N => e.g., What country's capital SQ => we want country
            List<Tree<String>> kids = t.getChildren();
            Tree<String> firstKid = kids.get(0);
            // Trivia-style questions (WHNP (WDT (Which)) ...)
            // e.g., Which of the following actors
            if (firstKid.getLabel().equals("WHNP") && firstKid.getChildren().size() == 1 && kids.size() > 1) {
                return kids.get(1);
            }
            String[] how = new String[]{"leftdis", "NP", "WHNP"};
            Tree<String> pti = traverseLocate(kids, how, false);
            if (pti != null && pti.getChildren().size() > 1) {
                String[] howNP = new String[]{"left", "NP"};
                Tree<String> ptiNP = traverseLocate(pti.getChildren(), howNP, false);
                if (ptiNP != null) {
                    List<Tree<String>> children = ptiNP.getChildren();
                    if (children.get(children.size() - 1).getLabel().equals("POS")) {
                        return ptiNP;
                    }
                }
            }
        } else if (motherCat.equals("NP")) { // NP + (WHPP WHNP N) => e.g., Pooh is (NP an imitation) (WHPP of (WHNP which animal))
            List<Tree<String>> kids = t.getChildren();
            if (kids.size() > 1) {
                String[] how = new String[]{"right", "WHPP"};
                Tree<String> pti = traverseLocate(kids, how, false);
                if (pti != null && pti.getChildren().size() > 1) {
                    List<Tree<String>> kidsWHPP = pti.getChildren();
                    Tree<String> pp = kidsWHPP.get(0);
                    if (pp.getLabel().equals("IN")) {
                        String[] howWHNP = new String[]{"left", "WHNP"};
                        Tree<String> ptiWHNP = traverseLocate(kidsWHPP, howWHNP, false);
                        if (ptiWHNP != null && ptiWHNP.getChildren().size() > 1) {
                            List<Tree<String>> kidsWHNP = ptiWHNP.getChildren();
                            String[] howNP = new String[]{"rightdis", "NP", "NN", "NNP", "NNPS", "NNS"};
                            Tree<String> ptiNP = traverseLocate(kidsWHNP, howNP, false);
                            if (ptiNP != null) {
                                return ptiNP;
                            }
                        }
                    }
                }
            }
        }
        return super.determineNonTrivialHead(t);
    }
    private static final long serialVersionUID = -2885112789412211734L;
}

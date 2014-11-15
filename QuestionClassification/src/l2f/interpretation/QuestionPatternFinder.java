package l2f.interpretation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2f.interpretation.classification.QuestionCategory;
import l2f.utils.Utils;

public class QuestionPatternFinder {

    /**
     * Set of categories that are considered by the patterns. For debugging purposes.
     */
    public static final Set<QuestionCategory> CONSIDERED_CATEGORIES = new HashSet<QuestionCategory>(
            Arrays.asList(
            QuestionCategory.ABBREVIATION_ABBREVIATION,
            QuestionCategory.ABBREVIATION_EXPANSION,
            QuestionCategory.DESCRIPTION_DEFINITION,
            QuestionCategory.ENTITY_SUBSTANCE,
            QuestionCategory.ENTITY_TERM,
            QuestionCategory.DESCRIPTION_REASON,
            QuestionCategory.HUMAN_DESCRIPTION));

    private static final String DEFINITION_PLACEHOLDER = "" + QuestionCategory.DESCRIPTION_DEFINITION;
    private static final String EXPANSION_PLACEHOLDER = "" + QuestionCategory.ABBREVIATION_EXPANSION;
    private static final String SUBSTANCE_PLACEHOLDER = "" + QuestionCategory.ENTITY_SUBSTANCE;
    private static final String TERM_PLACEHOLDER = "" + QuestionCategory.ENTITY_TERM;
    private static final String REASON_PLACEHOLDER = "" + QuestionCategory.DESCRIPTION_REASON;
    private static final String HUMAN_DESCRIPTION_PLACEHOLDER = "" + QuestionCategory.HUMAN_DESCRIPTION;

    private static Pattern DEFINITION_B =
            Pattern.compile("What\\s+(?:(?:do)es)(.*)mean",
            Pattern.CASE_INSENSITIVE);
    /**
     * Pattern A to identify definitions patterns. A more readable version:
     * (ROOT (SBARQ (WHNP (WP What)) (SQ (VB[ZP] is/are) (NP (DT the/a/an) (NNP/JJ/NN/NNPS *)))(? ?)))
     * NOTE: "the" was at (DT *) originally, but was removed for causing too much noise
     */
    private static Pattern DEFINITION_A_TREE =
            Pattern.compile("\\(ROOT \\(SBARQ \\(WHNP \\(WP What\\)\\) "
            + "\\(SQ \\(VB[ZP] (?:is|'s|are)\\) "
            + "\\(NP(?: \\(DT (?:an?)\\))?(( \\((NNP?S?|JJ|FW) [^)]+\\))+)\\)\\)"
            + "(?: \\([?.] [?.]\\))?\\)\\)",
            Pattern.CASE_INSENSITIVE);
    /**
     * Pattern B to identify definitions patterns.
     */
    private static Pattern DEFINITION_B_TREE =
            Pattern.compile("\\(ROOT \\(S \\(VP \\(VB Define\\)"
            + "(?: \\(NP(?: \\(DT (?:the|an?)\\))?)?(( \\((?:NNP?S?|JJ|FW) [^)]+\\))+)\\)\\)?"
            + "(?: \\([?.] [?.]\\))?\\)\\)",
            Pattern.CASE_INSENSITIVE);
    private static Pattern EXPANSION_A =
            Pattern.compile("What(?:'s|\\s+is)\\s+(?:\"|``)? *((\\p{javaUpperCase}\\.?)+) *(?:\"|'')?\\s*.?",
            Pattern.CASE_INSENSITIVE);
    private static Pattern EXPANSION_B =
            //Pattern.compile("What\\s+(?:(?:do)es).*stands? for\\s*.?",
            Pattern.compile("What\\s+(?:(?:do)es).*?(?:\"|``)? *((\\p{javaUpperCase}\\.?)+) *(?:\"|'')?\\s+stands?\\s+for\\s*.?",
            Pattern.CASE_INSENSITIVE);
    private static Pattern EXPANSION_C =
            Pattern.compile("What\\s+(?:(?:do)es).*?(?:\"|``)? *((\\p{javaUpperCase}\\.?)+) *(?:\"|'')?\\s+mean",
            Pattern.CASE_INSENSITIVE);
    private static Pattern EXPANSION_D =
            Pattern.compile("(?:\"|``)? *((\\p{javaUpperCase}\\.?)+) *(?:\"|'')?\\s+is\\s+(an|the)\\s+(acronym|abbreviation)\\s+(for|of)\\s+w.*",
            Pattern.CASE_INSENSITIVE);
    //What 's/is the abbreviation/acronym of/for General Motors ?
    private static Pattern ABBREVIATION_A =
            Pattern.compile("What(?:'s|\\s+is)\\s+the\\s+(?:abbreviation|acronym)\\s+(?:for|of)\\s+(?:the\\s+|)([\\w ]+)",
            Pattern.CASE_INSENSITIVE);
    //What is the abbreviated term used for the National Bureau of Investigation ?
    private static Pattern ABBREVIATION_B =
            Pattern.compile("What(?:'s|\\s+is)\\s+the\\s+abbreviated\\s+(?:term|form|expression)\\s+(?:used\\s+|)(?:for|of)\\s+(?:the\\s+|)([\\w ]+)",
            Pattern.CASE_INSENSITIVE);
    //What is p.m. an abbreviation for..?
    private static Pattern ABBREVIATION_C =
            Pattern.compile("What(?:'s|\\s+is)\\s+([\\w \\.]+) +an +(acronym|abbreviation)\\s+(for|of)",
            Pattern.CASE_INSENSITIVE);
    private static Pattern TERM_A_LOOK_AT =
            Pattern.compile("What\\s+do\\s+you\\s+call",
            Pattern.CASE_INSENSITIVE);

    
    private static Pattern REASON_A_LOOK_AT =
            Pattern.compile("What\\s+cause[ds]?",
            Pattern.CASE_INSENSITIVE);
    private static Pattern REASON_B =
            Pattern.compile("What(?:'s|\\s+(?:is|are))\\s+.*(?:used|known)\\s+for\\s*.?", 
            Pattern.CASE_INSENSITIVE);
     private static Pattern SUBSTANCE_A =
            Pattern.compile("What(?:'s|\\s+(?:is|are))\\s+.*(?:composed|made(?:\\s+out)?)\\s+of\\s*.?",
            Pattern.CASE_INSENSITIVE);

    private static Pattern HUMAN_DESCRIPTION_A_TREE =
            Pattern.compile("\\(ROOT \\(SBARQ \\(WHNP \\(WP Who\\)\\) "
            + "\\(SQ \\(VB[ZD] (?:is|'s|was)\\) "
            + "\\(NP(( \\(NNP?S? [^)]+\\))+)\\)\\)"
            + "(?: \\([?.] [?.]\\))?\\)\\)",
            Pattern.CASE_INSENSITIVE);
    private static Pattern NUMERIC_COUNT_MANY_TREE =
            Pattern.compile("\\(ROOT \\(SBARQ \\(WHNP (?:\\(WHNP )?\\(WHADJP \\(WRB How\\) \\(JJ many\\)\\).*?\\((?:NNS|NNPS) (.*?)\\)+", Pattern.CASE_INSENSITIVE);
    private static Pattern NUMERIC_COUNT_MUCH_TREE =
            Pattern.compile("\\(ROOT \\(SBARQ \\(WHNP \\(WHADJP \\(WRB How\\) "
            + "\\(JJ much\\)\\) \\(?:NN (.*?)\\)", Pattern.CASE_INSENSITIVE);

    /**
     * Attempts to match a list of patterns to the question.
     * NOTE: The order of the patterns is relevant!!!
     * @param question
     * @return
     */
    public String find(AnalyzedQuestion question) {
        String placeholder = Utils.EMPTY;
        List<String> tokens = question.getTokens();
        String parseTree = question.getParseTreeString();
        String original = question.getOriginalQuestion();
        /*
         * ABREVIATION:EXPANSION
         */
        Matcher m = EXPANSION_A.matcher(original);
        if (m.matches()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return EXPANSION_PLACEHOLDER;
        }

        m = EXPANSION_B.matcher(original);
        if (m.matches()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return EXPANSION_PLACEHOLDER;

        }
        m = EXPANSION_C.matcher(original);
        if (m.lookingAt()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return EXPANSION_PLACEHOLDER;
        }

        m = EXPANSION_D.matcher(original);
        if (m.lookingAt()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return EXPANSION_PLACEHOLDER;
        }

        /*
         * ABREVIATION:ABBREVIATION
         */
        m = ABBREVIATION_A.matcher(original);
        if (m.lookingAt()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return placeholder;
        }
        m = ABBREVIATION_B.matcher(original);
        if (m.lookingAt()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return placeholder;
        }
        m = ABBREVIATION_C.matcher(original);
        if (m.lookingAt()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return placeholder;
        }
        /*
         * DESCRIPTION:DEFINITION
         */
        m = DEFINITION_A_TREE.matcher(parseTree);
        if (m.matches()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return DEFINITION_PLACEHOLDER;
        }
        m = DEFINITION_B_TREE.matcher(parseTree);
        if (m.matches()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return DEFINITION_PLACEHOLDER;
        }
        m = DEFINITION_B.matcher(original);
        if (m.lookingAt()) {
            String group = m.group(1);
            question.setDefWord(group.trim());
            return DEFINITION_PLACEHOLDER;
        }
        /*
         * ENTITY:SUBSTANCE
         */
        if (SUBSTANCE_A.matcher(original).matches()) {
            return SUBSTANCE_PLACEHOLDER;
        }
        if (TERM_A_LOOK_AT.matcher(original).lookingAt()) {
            return TERM_PLACEHOLDER;
        }
        if (REASON_A_LOOK_AT.matcher(original).lookingAt()
                || REASON_B.matcher(original).matches()) {
            return REASON_PLACEHOLDER;
        }
        /*
         * HUMAN:DESCRIPTION
         */
        m = HUMAN_DESCRIPTION_A_TREE.matcher(parseTree);
        if (m.matches()) {
            String group = m.group(1);
            group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
            question.setDefWord(group);
            return HUMAN_DESCRIPTION_PLACEHOLDER;
        }


        if (original.matches("^Where .*(M|m)ountai(n|ns) .*")) {
            return QuestionCategory.LOCATION_MOUNTAIN + "";
        }

        String first = tokens.get(0);

        if (first.equalsIgnoreCase("Where")) {
            return QuestionCategory.LOCATION_OTHER + "";
        }

        if (first.equalsIgnoreCase("Why")) {
            return QuestionCategory.DESCRIPTION_REASON + "";
        }

        if (first.equalsIgnoreCase("When")) {
            return QuestionCategory.NUMERIC_DATE + "";
        }

        if (first.equalsIgnoreCase("Who")) {
            return QuestionCategory.HUMAN_INDIVIDUAL + "";
        }

        if (first.equalsIgnoreCase("Whose") || first.equalsIgnoreCase("Whom")) {
            return QuestionCategory.HUMAN_INDIVIDUAL + "";
        }
        /*CASOS HOW*/
        if (original.matches("^How do (you|I) say .*")) {
            return QuestionCategory.ENTITY_TERM + "";
        }

        if (original.matches("^How is.* defined.*")) {
            return QuestionCategory.DESCRIPTION_DEFINITION + "";
        }

        if (original.matches("^How long is .*")) {
            return QuestionCategory.NUMERIC_DISTANCE + "";
        }

        if (original.matches("^How much.* (weight|weights)[^\\w].*")) {
            return QuestionCategory.NUMERIC_WEIGHT + "";
        }

        if (original.matches("^How much (.* )?(cost|rent|fine|fined|sell|spend|spent|charge|charged|paid|pay|worth|taxed|tax|wage)[^\\w].*") || original.matches("^How much money .*")) {
            return QuestionCategory.NUMERIC_MONEY + "";
        }

        if (original.matches("^How big .*")) {
            return QuestionCategory.NUMERIC_SIZE + "";
        }

        if (original.matches("^How can .*") || original.matches("^How close .*")
                || original.matches("^How did .*") || original.matches("^How do .*")
                || original.matches("^How does .*") || original.matches("^How effective .*") || original.matches("^How has .*")
                || original.matches("^How is .*") || original.matches("^How successful .*") || original.matches("^How was .*") || original.matches("^How were .*")
                || original.matches("^How you .*") || original.matches("^How would .*")) {
            return QuestionCategory.DESCRIPTION_MANNER + "";
        }

        if (original.matches("^How come .*")) {
            return QuestionCategory.DESCRIPTION_REASON + "";
        }

        if (original.matches("^How deep .*") || original.matches("^How far .*")
                || original.matches("^How high .*") || original.matches("^How tall .*")
                || original.matches("^How wide .*") || original.matches("^How large .*")) {
            return QuestionCategory.NUMERIC_DISTANCE + "";
        }

        if (original.matches("^How fast .*")) {
            return QuestionCategory.NUMERIC_SPEED + "";
        }

        if (original.matches("^How hot .*")) {
            return QuestionCategory.NUMERIC_TEMPERATURE + "";
        }

        if (original.matches("^How large .*") || original.matches("^How loud .*") || original.matches("^How often .*")) {
            return QuestionCategory.NUMERIC_OTHER + "";
        }

        if (original.matches("^How long .*") || original.matches("^How old .*")) {
            return QuestionCategory.NUMERIC_PERIOD + "";
        }

        if (original.matches("^How many .*")) {
            m = NUMERIC_COUNT_MANY_TREE.matcher(parseTree);
            if (m.lookingAt()) {
                String group = m.group(1);
                group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
                question.setDefWord(group);
            }
            return QuestionCategory.NUMERIC_COUNT + "";
        }

        if (original.matches("^How much .*")) {
            m = NUMERIC_COUNT_MUCH_TREE.matcher(parseTree);
            if (m.lookingAt()) {
                String group = m.group(1);
                group = group.replaceAll("\\([^\\s]+\\s|\\)", "").trim();
                question.setDefWord(group);
            }
            return QuestionCategory.NUMERIC_COUNT + "";
        }

        if (original.matches("^How .*")) {
            return QuestionCategory.DESCRIPTION_MANNER + "";
        }
        return placeholder;
    } 
}

package l2f.classifiers;

import com.aliasi.classify.BaseClassifier;
import java.io.Serializable;

import l2f.interpretation.AnalyzedQuestion;
import l2f.interpretation.QuestionPatternFinder;

import com.aliasi.classify.Classification;
import com.aliasi.symbol.MapSymbolTable;
import l2f.interpretation.InterpretedQuestion;
import l2f.interpretation.classification.QuestionCategory;

public class RulesClassifier<E> implements BaseClassifier<E>, Serializable {

    private static final long serialVersionUID = 3517606761648610668L;
    private final MapSymbolTable categorySymbolTable;

    /**
     * Construct a Rule-based classifier from the specified feature extractor,
     * and a set of pre-defined categories.
     *
     * @param categories Instance categories.
     */
    public RulesClassifier(String[] categories) {
        this.categorySymbolTable = new MapSymbolTable();
        initializeCategories(categories);
    }

    private void initializeCategories(String[] categories) {
        for (String category : categories) {
            categorySymbolTable.getOrAddSymbol(category);
        }
    }

    @Override
    public Classification classify(E in) {
        AnalyzedQuestion aq = ((InterpretedQuestion) in).getAnalyzedQuestion();

        QuestionPatternFinder qpf = new QuestionPatternFinder();

        
        
        String category = aq.getHeadwordLexiconTarget();

        if (category == null || category.equalsIgnoreCase("")) {
            category = qpf.find(aq);
        } 

        if (category == null || category.equalsIgnoreCase("")) {
            return new Classification(QuestionCategory.VOID.name());
        }
        //if there is no category on the symbol table, we are testing with the coarse grained categories
        if (categorySymbolTable.symbolToID(category) == -1) {
            category = QuestionCategory.getCoarseCategory(category);
        }

       if (category == null || category.equalsIgnoreCase("")) {
            return new Classification(QuestionCategory.VOID.name());
        }

        return new Classification(category);
    }
}

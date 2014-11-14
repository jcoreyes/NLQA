package l2f.interpretation.classification;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import l2f.interpretation.AnalyzedQuestion;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.ConfusionMatrix;
import l2f.interpretation.InterpretedQuestion;

/**
 * A machine learning-based question classifier.
 * 
 * @author Jo�o
 * @param <E> the type of question that can be classified, e.g., 
 * 	<code>String</code> or <code>AnalyzedQuestion</code>
 */
public class QuestionClassifier<E> implements Serializable {

    private BaseClassifier<E> classifier;
    private final String[] categories;

    /**
     * Creates a new instance of QuestionClassifier, using the classifier
     * strategy provided by <code>classifier</code>.
     * @param classifier
     */
    public QuestionClassifier(BaseClassifier<E> classifier, String[] categories) {
        this.classifier = classifier;
        this.categories = categories;
    }

    /**
     * Classifies a single question.
     * @param instance the question to be classified
     * @return the predicted category of the question
     */
    public String classify(E instance) {
        return classifier.classify(instance).bestCategory();
    }

    /**
     * Classifies a set of questions.
     * @param instances map that contains pairs of questions and corresponding categories
     * @return the confusion matrix for this set of instances
     */
    public ConfusionMatrix classify(Map<E, String> instances, FileWriter fw) {
        ConfusionMatrix cm = new ConfusionMatrix(categories);
        try {

            for (Map.Entry<E, String> entry : instances.entrySet()) {
                //interpreted question
                E key = entry.getKey();
                //best category
                String value = entry.getValue();

                String predictedCategory = "";
                String currentPredictedCategory = classify(key);
               
                InterpretedQuestion iQuestion = ((InterpretedQuestion) key);
                AnalyzedQuestion aQuestion = iQuestion.getAnalyzedQuestion();

                if (iQuestion.getPredictedQuestionCategory().equals(QuestionCategory.VOID)) {
                    predictedCategory = currentPredictedCategory;
                    iQuestion.setPredictedQuestionCategory(QuestionCategory.getCategory(predictedCategory));
                } else {
                    predictedCategory = iQuestion.getPredictedQuestionCategory().name();
                }

                String text = "Q=\"" + aQuestion.getOriginalQuestion();
                text += "\"\tCategory=\"" + value;
                text += "\"\tPredicted=\"" + predictedCategory;
                if (!predictedCategory.equalsIgnoreCase(currentPredictedCategory)) {
                    text += "\"\tCurPredicted=\"" + currentPredictedCategory;
                }
                text += aQuestion.getHeuristicForHeadwordExtaction().equalsIgnoreCase("")? "" : "\"\tHeuristic=\"" + aQuestion.getHeuristicForHeadwordExtaction();
                text += "\"\tCORRECT?" + predictedCategory.equalsIgnoreCase(value);

                System.out.println(text);
                System.out.flush();

                fw.write(text + "\n");
                cm.increment(value, predictedCategory);
                
            }
           
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cm;
    }

    
    private static final long serialVersionUID = 5526851560485565880L;
}

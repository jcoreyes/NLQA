package l2f.interpretation.classification;

import com.aliasi.classify.BaseClassifier;
import java.io.File;
import java.io.IOException;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;

import l2f.config.Config;
import l2f.interpretation.QuestionAnalyzer;
import l2f.classifiers.ClassifierFactory;
import l2f.classifiers.ClassifierFactory.ClassifierType;
import l2f.interpretation.InterpretedQuestion;
import l2f.utils.ResourceLoadException;

/**
 * Factory used to create QuestionClassifier instances.
 * 
 */
public class QuestionClassifierFactory {

    /**
     * Creates a new question classifier or loads an existent one, using 
     * language specific configurations.
     * This is used by <code>QuestionClassifierEvaluator</code> for a finer control
     * of the classification process.
     */


    public static QuestionClassifier<InterpretedQuestion> newQuestionClassifier(QuestionAnalyzer questionAnalyzer,
            FeatureExtractor<InterpretedQuestion> featureExtractor,
            QuestionClassificationCorpus<InterpretedQuestion> corpus,
            String modelType, boolean useFineGrainedCategories) {
        File model = new File(Config.classification_modelFile + "." + modelType);
        boolean forceTraining = Config.classification_forceTraining;
        try {
            // train a new model if one doesn't exist already or if the force option is triggered
            if (!model.exists() || forceTraining) {
                // train and serialize the question classifier
                String[] categories = useFineGrainedCategories
                        ? QuestionCategory.toFineStringArray()
                        : QuestionCategory.toCoarseStringArray();
                BaseClassifier<InterpretedQuestion> classifier =
                        ClassifierFactory.getClassifier(corpus, categories,
                        ClassifierType.valueOf(modelType), featureExtractor);
                QuestionClassifier<InterpretedQuestion> questionClassifier = new QuestionClassifier<InterpretedQuestion>(classifier, categories);
                AbstractExternalizable.serializeTo(questionClassifier, model);
            }
            // load serialized model
            @SuppressWarnings("unchecked")
            QuestionClassifier<InterpretedQuestion> qc =
                    (QuestionClassifier<InterpretedQuestion>) AbstractExternalizable.readObject(model);
            return qc;
        } catch (IOException ioe) {
            throw new ResourceLoadException("Unable to create QuestionClassifier.", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new ResourceLoadException("Unable to load QuestionClassifier.", cnfe);
        }
    }
}

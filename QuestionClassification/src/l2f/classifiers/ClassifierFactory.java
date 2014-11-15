package l2f.classifiers;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Corpus;
import com.aliasi.util.FeatureExtractor;

public class ClassifierFactory {

    public enum ClassifierType {

        SVM, NB, RULES
    }

    public static <E> BaseClassifier<E> getClassifier(
            Corpus<ObjectHandler<E>> corpus,
            String[] categories,
            ClassifierType classifierType,
            FeatureExtractor<E> featureExtractor) {
        if (classifierType == ClassifierType.SVM) {
            return new SvmClassifier<E>(featureExtractor, corpus, categories);
        } else if (classifierType == ClassifierType.RULES) {
            return new RulesClassifier<E>(categories);
        } else if (classifierType == ClassifierType.NB) {
            return new LanguageModelClassifier<E>(featureExtractor, corpus, categories);
        } else {
            throw new IllegalArgumentException("Unrecognized classifier type: '" + classifierType + "'.");
        }
    }
}

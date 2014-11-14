package l2f.classifiers;

import com.aliasi.classify.BaseClassifier;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

import l2f.utils.ResourceLoadException;
import l2f.utils.Utils;

import com.aliasi.classify.Classification;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.FeatureExtractor;
import l2f.interpretation.InterpretedQuestion;

public class LanguageModelClassifier<E> implements BaseClassifier<E>, Serializable {

    private TradNaiveBayesClassifier classifier;
    private FeatureExtractor<E> featureExtractor;

    public LanguageModelClassifier(FeatureExtractor<E> featureExtractor,
            Corpus<ObjectHandler<E>> corpus,
            String[] categories) {
        this.classifier = new TradNaiveBayesClassifier(new HashSet<String>(Arrays.asList(categories)),
                IndoEuropeanTokenizerFactory.INSTANCE);
        this.featureExtractor = featureExtractor;
        train(corpus);
    }

    private void train(Corpus<ObjectHandler<E>> corpus) {
        try {
            corpus.visitTrain(new ObjectHandler<E>() {
                public void handle(E object) {
                    classifier.train(object.toString(), new Classification(((InterpretedQuestion)object).getQuestionCategory().toString()), 1.0);
                }
            });
        } catch (IOException e) {
            throw new ResourceLoadException("Unable to train NB classifier.", e);
        }
    }

    public Classification classify(E instance) {
        String s = Utils.join(featureExtractor.features(instance).keySet(),
                Utils.EMPTY);
        return classifier.classify(s);
    }
    private static final long serialVersionUID = -6202913752375461503L;
}

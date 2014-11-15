package l2f.classifiers;

import com.aliasi.classify.BaseClassifier;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import com.aliasi.classify.Classification;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import l2f.interpretation.InterpretedQuestion;

/**
 * Wrapper for LibSVM.
 * 
 * @param <E> 
 */
public class SvmClassifier<E> implements BaseClassifier<E>, Serializable {

    private static final long serialVersionUID = 3517606761648610668L;
    private svm_model model;
    private final FeatureExtractor<? super E> featureExtractor;
    private final MapSymbolTable featureSymbolTable;
    private final MapSymbolTable categorySymbolTable;

    /**
     * Construct a SVM classifier from the specified feature extractor,
     * and a set of pre-defined categories.
     *
     * @param featureExtractor Feature extractor for objects.
     * @param corpus Corpus to use for training.
     * @param categories Instance categories.
     */
    public SvmClassifier(FeatureExtractor<? super E> featureExtractor,
            Corpus<ObjectHandler<E>> corpus,
            String[] categories) {
        this.featureExtractor = featureExtractor;
        this.featureSymbolTable = new MapSymbolTable();
        this.categorySymbolTable = new MapSymbolTable();
        initializeCategories(categories);
        trainSvm(corpus);

    }

    private void trainSvm(Corpus<ObjectHandler<E>> corpus) {
        CorpusCollector collector = new CorpusCollector();
        try {
            corpus.visitTrain(collector);
        } catch (IOException e) {
            throw new RuntimeException("Unable to train SVM.", e);
        }
        svm_node[][] featureVectors = collector.featureVectors();
        Double[] categoryVectors = collector.categoryVectors();
        corpus = null;
        int maxIndex = 0;
        svm_parameter param = initializeSvmParameters();
        svm_problem prob = new svm_problem();
        prob.l = featureVectors.length;
        
        System.out.println("feature vectors: " + prob.l);
        System.out.println("categories: " + categorySymbolTable.symbolSet().size());
        System.out.println("unique features: " + featureSymbolTable.symbolSet().size());
        System.out.println("feature extractor" + featureExtractor.toString());

        prob.x = new svm_node[prob.l][];
        for (int i = 0; i < prob.l; i++) {
            prob.x[i] = featureVectors[i];
            for (svm_node node : featureVectors[i]) {
                maxIndex = Math.max(maxIndex, node.index);
            }
        }
        prob.y = new double[prob.l];
        for (int i = 0; i < prob.l; i++) {
            prob.y[i] = categoryVectors[i];
        }
        if (param.gamma == 0) {
            param.gamma = 1.0 / maxIndex;
        }
        String errorMsg = svm.svm_check_parameter(prob, param);
        if (errorMsg != null) {
            throw new IllegalArgumentException("A problem has ocurred while training SVM: " + errorMsg);
        }

        model = svm.svm_train(prob, param);
    }

    private void initializeCategories(String[] categories) {
        for (String category : categories) {
            categorySymbolTable.getOrAddSymbol(category);
        }
    }

    private svm_parameter initializeSvmParameters() {
        svm_parameter param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.degree = 3;		// degree in kernel function (default 3)
        param.gamma = 0.02;		// 1/k, where k is the largest index or the total number of features
        param.coef0 = 0;		// coef0 in kernel function (default 0)
        param.nu = 0.5;			// the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
        param.cache_size = 100;	// cache memory size in MB (default 100)
        param.C = 1;			// the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)
        param.eps = 1e-3;		// tolerance of termination criterion (default 0.001)
        param.p = 0.1;			// the epsilon in loss function of epsilon-SVR (default 0.1)
        param.shrinking = 0;	// whether to use the shrinking heuristics, 0 or 1 (default 1)
        param.probability = 0;	// whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
        param.nr_weight = 0;	// the parameter C of class i to weight*C, for C-SVC (default 1)
        param.weight_label = new int[0];
        param.weight = new double[0];
        return param;
    }

    /**
     * Return the scored classification for the specified input.  The
     * input is first converted to a feature vector using the feature
     * extractor, then scored against the SVM.
     *
     * @param in The element to be classified.
     * @return The classification for the specified element.
     */
    @Override
    public Classification classify(E in) {
        Map<String, ? extends Number> featureVector = featureExtractor.features(in);
        List<String> keysToRemove = new ArrayList<String>();
        for (String key : featureVector.keySet()) {
            if (featureSymbolTable.symbolToID(key) < 0) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            featureVector.remove(key);
        }
        svm_node[] inputVector = toVector(featureVector, featureSymbolTable);
        double categoryId = svm.svm_predict(model, inputVector);
        String category = categorySymbolTable.idToSymbol((int) categoryId);
        return new Classification(category);
    }

    class CorpusCollector implements ObjectHandler<E> {

        final List<svm_node[]> _inputFeatureVectorList = new ArrayList<svm_node[]>();
        final List<Double> _inputCategoryVectorList = new ArrayList<Double>();

        public void handle(E object) {
            Map<String, ? extends Number> featureMap = featureExtractor.features(object);
            _inputFeatureVectorList.add(toVectorAddSymbols(featureMap, featureSymbolTable));
            _inputCategoryVectorList.add(
                    new Integer(categorySymbolTable.symbolToID(((InterpretedQuestion)object).getQuestionCategory().toString())).doubleValue());
        }

        svm_node[][] featureVectors() {
            svm_node[][] vectors = new svm_node[_inputFeatureVectorList.size()][];
            _inputFeatureVectorList.toArray(vectors);
            return vectors;
        }

        Double[] categoryVectors() {
            Double[] vectors = new Double[_inputCategoryVectorList.size()];
            _inputCategoryVectorList.toArray(vectors);
            return vectors;
        }
    }

    /**
     * Convert the specified feature vector into a svm_node vector using
     * the specified symbol table to encode features as integers.  Features
     * that do not exist as symbols in the symbol table WILL be added
     * to the symbol table.
     *
     * @param table Symbol table for encoding features as integers.
     * @param featureVector Feature vector to convert to svm_node vector.
     * @return svm_node vector encoding the feature vector with
     * the symbol table.
     */
    static svm_node[] toVectorAddSymbols(Map<String, ? extends Number> featureVector,
            SymbolTable table) {
        int size = featureVector.size();
        svm_node[] x = new svm_node[size];
        int i = 0;
        for (Map.Entry<String, ? extends Number> entry : featureVector.entrySet()) {
            String feature = entry.getKey();
            //double val = entry.getValue().doubleValue();
            int id = table.getOrAddSymbol(feature);
            x[i] = new svm_node();
            x[i].index = id;
            //x[i++].value = 1.0;
            x[i++].value = (entry.getValue()).doubleValue();
        }
        java.util.Arrays.sort(x, VECTOR_COMPARATOR);
        return x;
    }

    /**
     * Convert the specified feature vector into a svm_node vector using
     * the specified symbol table to encode features as integers.  Features
     * that do not exist as symbols in the symbol table will NOT be added
     * to the symbol table.
     *
     * @param table Symbol table for encoding features as integers.
     * @param featureVector Feature vector to convert to svm_node vector.
     * @return svm_node vector encoding the feature vector with
     * the symbol table.
     */
    static svm_node[] toVector(Map<String, ? extends Number> featureVector,
            SymbolTable table) {
        int size = featureVector.size();
        svm_node[] x = new svm_node[size];
        int i = 0;
        for (Map.Entry<String, ? extends Number> entry : featureVector.entrySet()) {
            String feature = entry.getKey();
            int id = table.symbolToID(feature);
            if (id < 0) {
                continue;
            }	// symbol not in any basis vector
            //double val = entry.getValue().doubleValue();
            x[i] = new svm_node();
            x[i].index = id;
            //x[i++].value = 1.0;
            x[i++].value = (entry.getValue()).doubleValue();

        }
        java.util.Arrays.sort(x, VECTOR_COMPARATOR);
        return x;
    }
    // LibSVM requires svm_node[] to be ordered by index
    static final Comparator<svm_node> VECTOR_COMPARATOR = new Comparator<svm_node>() {

        public int compare(svm_node arg0, svm_node arg1) {
            return arg0.index - arg1.index;
        }
    };

    static class Externalizer<F> extends AbstractExternalizable {

        private static final long serialVersionUID = -2184305195285567770L;
        private final SvmClassifier<F> _classifier;

        public Externalizer() {
            this(null);
        }

        public Externalizer(SvmClassifier<F> classifier) {
            _classifier = classifier;
        }

        @Override
        protected Object read(ObjectInput in) throws ClassNotFoundException, IOException {
            // feature extractor
			/*FeatureExtractor<F> featureExtractor = (FeatureExtractor<F>)in.readObject();
            // category symbol table
            MapSymbolTable categories = (MapSymbolTable)in.readObject();
            // feature symbol table
            MapSymbolTable features = (MapSymbolTable)in.readObject();
            // svm model
            svm_problem problem = (svm_problem)in.readObject();
             */
            return null;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            // feature extractor
            out.writeObject(_classifier.featureExtractor);
            // category symbol table
            out.writeObject(_classifier.categorySymbolTable);
            // feature symbol table
            out.writeObject(_classifier.featureSymbolTable);
            // svm model
            out.writeObject(_classifier.model);
        }
    }
}

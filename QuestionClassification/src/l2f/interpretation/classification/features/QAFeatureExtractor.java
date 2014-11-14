package l2f.interpretation.classification.features;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import l2f.interpretation.AnalyzedQuestion;

import com.aliasi.util.Counter;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import l2f.interpretation.InterpretedQuestion;

public class QAFeatureExtractor implements FeatureExtractor<InterpretedQuestion>, Serializable {

    

    @Override
    public Map<String, Counter> features(InterpretedQuestion iq) {
        AnalyzedQuestion in = iq.getAnalyzedQuestion();
        ObjectToCounterMap<String> map = new ObjectToCounterMap<String>();

        //UNIGRAMS
        List<String> tokens = in.getTokens();
        for (int i = 0; i < tokens.size(); i++) {
            map.increment("#UNI#" + tokens.get(i));
        }
        //HEADWORD
        map.increment("#HW#" + in.getHeadword());

        //WORDNET MAP
        map.increment("#WN#" + in.getHeadwordLexiconTarget());

        return map;
    }
}

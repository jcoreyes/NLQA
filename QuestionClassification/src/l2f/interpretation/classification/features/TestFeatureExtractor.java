package l2f.interpretation.classification.features;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import l2f.interpretation.AnalyzedQuestion;
import com.aliasi.util.Counter;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import l2f.interpretation.InterpretedQuestion;

/**
 * A FeatureExtractor used for testing purposes.
 * 
 */
public class TestFeatureExtractor implements FeatureExtractor<InterpretedQuestion>, Serializable {

    public final EnumSet<FeatureSet> activeFeatures;

    public TestFeatureExtractor(EnumSet<FeatureSet> activeFeatures) {
        this.activeFeatures = activeFeatures;
    }

    @Override
    public Map<String, Counter> features(InterpretedQuestion it) {
        AnalyzedQuestion in = it.getAnalyzedQuestion();
        ObjectToCounterMap<String> map = new ObjectToCounterMap<String>();
        List<String> tokens = in.getTokens();
        if (activeFeatures.contains(FeatureSet.BINARY_UNIGRAM)) {
            for (int i = 0; i < tokens.size(); i++) {
                if (!map.containsKey("#B#" + tokens.get(i))) {
                    map.increment("#B#" + tokens.get(i));
                }
            }
        }
        if (activeFeatures.contains(FeatureSet.UNIGRAM)) {
            for (int i = 0; i < tokens.size(); i++) {
                map.increment(tokens.get(i));
            }
        }
        if (activeFeatures.contains(FeatureSet.BINARY_BIGRAM)) {
            for (int i = 1; i < tokens.size(); i++) {
                if (!map.containsKey("#B#" + tokens.get(i - 1) + " " + tokens.get(i))) {
                    map.increment("#B#" + tokens.get(i - 1) + " " + tokens.get(i));
                }
            }
        }
        if (activeFeatures.contains(FeatureSet.BIGRAM)) {
            for (int i = 1; i < tokens.size(); i++) {
                map.increment(tokens.get(i - 1) + " " + tokens.get(i));
            }
        }
        if (activeFeatures.contains(FeatureSet.TRIGRAM)) {
            for (int i = 2; i < tokens.size(); i++) {
                map.increment(tokens.get(i - 2) + " " +  tokens.get(i - 1) + " " + tokens.get(i));
            }
        }
        if (activeFeatures.contains(FeatureSet.BINARY_TRIGRAM)) {
            for (int i = 2; i < tokens.size(); i++) {
                if (!map.containsKey("#B#" + tokens.get(i - 2) +  " " + tokens.get(i - 1) + " " + tokens.get(i))) {
                    map.increment("#B#" + tokens.get(i - 2) + " " +  tokens.get(i - 1) + " " + tokens.get(i));
                }
            }
        }

        if (activeFeatures.contains(FeatureSet.WORD_SHAPE)) {
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).matches("^[a-z]+$")) {
                    map.increment("#F#LOWERCASED#");
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z]+$")) {
                    map.increment("#F#UPPERCASED#");
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z][a-zA-Z]+$")) {
                    map.increment("#F#CAPITALIZED#");
                    continue;
                }
                if (tokens.get(i).matches("^[a-zA-Z]+$")) {
                    map.increment("#F#MIXEDCASED#");
                    continue;
                }
                if (tokens.get(i).matches("^[0-9]+$")) {
                    map.increment("#F#DIGITSONLY#");
                    continue;
                }
                map.increment("#F#OTHERCASED#");
            }
        }

        if (activeFeatures.contains(FeatureSet.BINARY_WORD_SHAPE)) {
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).matches("^[a-z]+$")) {
                    if (!map.containsKey("#B#LOWERCASED#")) {
                        map.increment("#B#LOWERCASED#");
                    }
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z]+$")) {
                    if (!map.containsKey("#B#UPPERCASED#")) {
                        map.increment("#B#UPPERCASED#");
                    }
                    continue;
                }
                if (tokens.get(i).matches("^[A-Z][a-zA-Z]+$")) {
                    if (!map.containsKey("#B#CAPITALIZED#")) {
                        map.increment("#B#CAPITALIZED#");
                    }
                    continue;
                }
                if (tokens.get(i).matches("^[a-zA-Z]+$")) {
                    if (!map.containsKey("#B#MIXEDCASED#")) {
                        map.increment("#B#MIXEDCASED#");
                    }
                    continue;
                }

                if (tokens.get(i).matches("^[0-9]+$")) {
                    if (!map.containsKey("#B#DIGITSONLY#")) {
                        map.increment("#B#DIGITSONLY#");
                    }
                    continue;
                }
                if (!map.containsKey("#B#OTHERCASED#")) {
                    map.increment("#B#OTHERCASED#");
                }
            }
        }
        if (activeFeatures.contains(FeatureSet.LENGTH)) {
            map.increment("#LENGHT" + (tokens.size() < 6 ? "#S#" : "#L#"));
        }
        if (activeFeatures.contains(FeatureSet.POS)) {
            List<String> posTags = in.getPosTags();
            for (int i = 0; i < posTags.size(); i++) {
                map.increment(posTags.get(i));
            }
            /*for (int i = 1; i < posTags.size(); i++) {
            map.increment(posTags.get(i-1) + " " + posTags.get(i));
            }*/
        }
        if (activeFeatures.contains(FeatureSet.HEADWORD)) {
            map.increment("#HW#" + in.getHeadword());
            
        }
        if (activeFeatures.contains(FeatureSet.CATEGORY)) {         // WORDNET MAP
            map.increment("#WN#" + in.getHeadwordLexiconTarget());
            
        }

//if (activeFeatures.contains(FeatureSet.NER_INCR)) {
//	Chunking chunking = recognizer.chunk(in.getOriginalQuestion());
//	for (Chunk chunk : chunking.chunkSet()) {
//		map.increment(chunk.type());
//	}   
//}
//if (activeFeatures.contains(FeatureSet.NER_REPL)) {
//	Chunking chunking = recognizer.chunk(in.getOriginalQuestion());
//	for (Chunk chunk : chunking.chunkSet()) {
//		map.increment(chunk.type());
//	}   
//}
        return map;
    }
}

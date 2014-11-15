package l2f.nlp;

import l2f.utils.ResourceLoadException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.aliasi.util.Pair;
import l2f.config.Config;

import l2f.interpretation.LexiconMap;
import l2f.utils.Utils;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * This class provides a <code>Lexicon</code> implementation, using
 * Princeton's WordNet -- a lexical database.
 *  
 * @author Jo�o
 */
public class WordNet implements Lexicon {

    public final Dictionary dictionary;

    public WordNet() {
        try {

            JWNL.initialize(new FileInputStream(Config.nlp_wordnetProperties));
            this.dictionary = Dictionary.getInstance();
        } catch (JWNLException je) {
            throw new ResourceLoadException(je);
        } catch (IOException ioe) {
            throw new ResourceLoadException("WordNet: couldn't found the properties file.", ioe);
        }
    }

    /**
     * Returns the <b>first</b> synset of a given word, which represents
     * the most frequent one.
     * @param word
     * @return the first synset of the word
     */
    private Synset getSynset(String word, POS pos) {
        Synset synset = null;
        try {
            if (word != null) {
                IndexWord indexWord = dictionary.lookupIndexWord(pos, word);
                if (indexWord == null) {
                    return null;
                }
                synset = indexWord.getSense(1);
            }

        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return synset;
    }

    private Synset[] getSynsets(String word, POS pos) {
        Synset[] synset = null;
        try {
            IndexWord indexWord = dictionary.lookupIndexWord(pos, word);
            if (indexWord == null) {
                return null;
            }
            synset = indexWord.getSenses();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return synset;
    }

    private Synset getSynset(String word) {
        return getSynset(word, POS.NOUN);
    }

    @Override
    public List<String> getSynonyms(String word) {
        List<String> synonyms = new ArrayList<String>();
        try {
            if (word != null && !word.equalsIgnoreCase("")) {
                IndexWordSet iws = dictionary.lookupAllIndexWords(word);
                if (iws != null) {
                    for (IndexWord indexWord : iws.getIndexWordArray()) {
                        for (Synset synset : indexWord.getSenses()) {
                            for (Word w : synset.getWords()) {
                                synonyms.add(w.getLemma());
                            }
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return synonyms;
    }

    @Override
    public List<String> getSynonyms(String word, POS pos) {
        List<String> synonyms = new ArrayList<String>();
        try {
            IndexWord indexWord = dictionary.lookupIndexWord(pos, word);
            if (indexWord != null) {
                for (Synset synset : indexWord.getSenses()) {
                    for (Word w : synset.getWords()) {
                        synonyms.add(w.getLemma());
                    }
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return synonyms;
    }

    @Override
    public Set<String> getCommonHypernyms(String[] words) {
        Set<String> overlap = new HashSet<String>();
        boolean isFirst = true;
        for (String word : words) {
            Set<String> current = new HashSet<String>();
            Synset synset = getSynset(word); // 1st sense
            if (synset == null) {
                continue;
            }
            Queue<Pointer> queue = new LinkedList<Pointer>();
           
            queue.addAll(Arrays.asList(synset.getPointers(PointerType.HYPERNYM)));
            while (!queue.isEmpty()) {
                Pointer p = queue.poll();
                Synset targetSynset = null;
                try {
                    targetSynset = p.getTargetSynset();
                } catch (JWNLException e) {
                    e.printStackTrace();
                    continue;
                }
                for (Word w : targetSynset.getWords()) {
                    if (isFirst) {
                        overlap.add(w.getLemma());
                    }
                    current.add(w.getLemma());
                }
                queue.addAll(Arrays.asList(targetSynset.getPointers(PointerType.HYPERNYM)));
            }
            overlap.retainAll(current);
            isFirst = false;
        }
        return overlap;
    }

    @Override
    public List<String> getFullHyponym(String word, int maxDepth) {
        List<String> hyponyms = new ArrayList<String>();
        Map<Long, Integer> hyponymsDepth = new HashMap<Long, Integer>();
        Synset synset = getSynset(word); // 1st sense
        if (synset == null) {
            return hyponyms;
        }
        Queue<Pointer> queue = new LinkedList<Pointer>();
        queue.addAll(Arrays.asList(synset.getPointers(PointerType.HYPONYM)));
        for (Pointer p : queue) {
            hyponymsDepth.put(p.getTargetOffset(), 0);
        }
        while (!queue.isEmpty() && Collections.max(hyponymsDepth.values()) < maxDepth) {
            Pointer p = queue.poll();
            Synset targetSynset = null;
            try {
                targetSynset = p.getTargetSynset();
            } catch (JWNLException e) {
                e.printStackTrace();
                continue;
            }
            for (Word w : targetSynset.getWords()) {
                hyponyms.add(w.getLemma().replace('_', ' '));
            }
            Pointer[] targetPointers = targetSynset.getPointers(PointerType.HYPONYM);
            queue.addAll(Arrays.asList(targetPointers));
            for (Pointer pp : targetPointers) {
                hyponymsDepth.put(pp.getTargetOffset(), hyponymsDepth.get(p.getTargetOffset()) + 1);
            }
        }
        return hyponyms;
    }

    @Override
    public List<String> getFullHyponym(String word) {
        return getFullHyponym(word, Integer.MAX_VALUE);
    }

    @Override
    public boolean hasWord(String word) {
        IndexWord iw = null;
        try {
            iw = dictionary.lookupIndexWord(POS.NOUN, word);
        } catch (JWNLException e) {
            e.printStackTrace();
            return false;
        }
        /* We need to count whitespaces, because WordNet's morphological
         * processor can remove important words, and therefore turn a
         * nonexistent word into an existent one, such as "real birthday"
         * into "real".
         */
        return iw != null
                && Utils.countWhitespaces(iw.getLemma()) == Utils.countWhitespaces(word);
    }

    @Override
    public String getGloss(String word) {
        Synset synset = getSynset(word); // 1st sense
        if (synset != null) {
            return synset.getGloss();
        } else {
            return Utils.EMPTY;
        }
    }

    @Override
    public String getName() {
        return "WordNet 3.0";
    }

    @Override
    public Pair<Long, String> intersectMap(String word, POS pos, LexiconMap map, boolean allsenses) {
        if (allsenses) {
            return intersectMapAllSenses(word, pos, map);
        } else {
            return intersectMap1stSense(word, pos, map);
        }
    }


    public Pair<Long, String> intersectMapAllSenses(String word, POS pos, LexiconMap map) {
        Synset[] headSynsets = getSynsets(word, pos);
        if (headSynsets == null) {
            System.err.println("Unable to find the word '"
                    + word + "/" + pos + "' in WordNet.");
        } else {
            int i = 0;
            for (i = 0; i < headSynsets.length; i++) {
                Synset headSynset = headSynsets[i];
                long targetOffset = headSynset.getOffset();
                String targetString = map.get(targetOffset);
                if (targetString != null) {
                    //TTTTEEEESSSTTTTEEESSS
                    String ws = "";
                    for (Word w : headSynset.getWords()) {
                        ws += w.getLemma() + ",";
                    }
                    ws = ws.replaceAll(",$", "");
                    return new Pair<Long, String>(targetOffset, targetString + ":::" + (i + 1) + "sense[" + ws + "]"); // intersection!
                    //return new Pair<Long, String>(targetOffset, targetString); // intersection!
                }
                Queue<Pointer> queue = new LinkedList<Pointer>();
                queue.addAll(Arrays.asList(headSynset.getPointers(PointerType.HYPERNYM)));
                while (!queue.isEmpty()) {
                    Pointer p = queue.poll();
                    Synset targetSynset = null;
                    try {
                        targetSynset = p.getTargetSynset();
                    } catch (JWNLException e) {
                        e.printStackTrace();
                        continue;
                    }
                    targetOffset = targetSynset.getOffset();
                    targetString = map.get(targetOffset);
                    if (targetString != null) {
                        //TESTESSSS
                        String ws = "";
                        for (Word w : targetSynset.getWords()) {
                            ws += w.getLemma() + ",";
                        }
                        ws = ws.replaceAll(",$", "");
                        return new Pair<Long, String>(targetOffset, targetString + ":::" + (i + 1) + "sense.hypernyms[" + ws + "]"); // intersection!
                        //return new Pair<Long, String>(targetOffset, targetString);
                    }
                    queue.addAll(Arrays.asList(targetSynset.getPointers(PointerType.HYPERNYM)));
                }
            }
        }
        return null;
    }

    public Pair<Long, String> intersectMap1stSense(String word, POS pos, LexiconMap map) {
        Synset headSynset = getSynset(word, pos); // 1st sense
        if (headSynset == null) {
            System.err.println("Unable to find the word '"
                    + word + "/" + pos + "' in WordNet.");
        } else {
            int i = 0;

            long targetOffset = headSynset.getOffset();
            String targetString = map.get(targetOffset);
            if (targetString != null) {
                //TTTTEEEESSSTTTTEEESSS
                String ws = "";
                for (Word w : headSynset.getWords()) {
                    ws += w.getLemma() + ",";
                }
                ws = ws.replaceAll(",$", "");
                return new Pair<Long, String>(targetOffset, targetString + ":::" + (i + 1) + "sense[" + ws + "]"); // intersection!
                //return new Pair<Long, String>(targetOffset, targetString); // intersection!
            }
            Queue<Pointer> queue = new LinkedList<Pointer>();
            queue.addAll(Arrays.asList(headSynset.getPointers(PointerType.HYPERNYM)));
            while (!queue.isEmpty()) {
                Pointer p = queue.poll();
                Synset targetSynset = null;
                try {
                    targetSynset = p.getTargetSynset();
                } catch (JWNLException e) {
                    e.printStackTrace();
                    continue;
                }
                targetOffset = targetSynset.getOffset();
                targetString = map.get(targetOffset);
                if (targetString != null) {
                    //TESTESSSS
                    String ws = "";
                    for (Word w : targetSynset.getWords()) {
                        ws += w.getLemma() + ",";
                    }
                    ws = ws.replaceAll(",$", "");
                    return new Pair<Long, String>(targetOffset, targetString + ":::" + (i + 1) + "sense.hypernyms[" + ws + "]"); // intersection!
                    //return new Pair<Long, String>(targetOffset, targetString);
                }
                queue.addAll(Arrays.asList(targetSynset.getPointers(PointerType.HYPERNYM)));
            }
        }

        return null;
    }
}

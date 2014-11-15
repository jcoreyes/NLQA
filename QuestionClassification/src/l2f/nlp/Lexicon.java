package l2f.nlp;

import java.util.List;
import java.util.Set;

import l2f.interpretation.LexiconMap;

import com.aliasi.util.Pair;

import net.didion.jwnl.data.POS;

/**
 * Lexicon represents a word-based ontology, on which one can make inferences,
 * such as IS-A (hypernymy) and KIND-OF (hyponymy), and other word-based utility
 * methods such as obtaining all the synonyms of a given word.
 * 
 */
public interface Lexicon {	
	/**
	 * Returns all synonyms for a given word.
	 */
	List<String> getSynonyms(String word);
	
	/**
	 * Returns all synonyms for a given word, given a POS tag.
	 */
	List<String> getSynonyms(String word, POS pos);
		
	/**
	 * Returns a set of hypernyms that are shared by every word in <code>words</code>. 
         *
         * @return a set of hypernyms that are shared by every word in <code>words</code>
         */
	Set<String> getCommonHypernyms(String[] words);
		
	/**
	 * Returns all hyponyms of a given word.
	 */
	List<String> getFullHyponym(String word);
	
	/**
	 * Returns all hyponyms of a given word with depth <= maxDepth. 
	 */
	List<String> getFullHyponym(String word, int maxDepth);
			
	/**
	 * Check if a given word exists in the lexicon.
         *
         * @param word the word to be checked in the lexicon.
         * @return true if the lexicon contains the word.
         */
	boolean hasWord(String word);
	
	/**
	 * Returns the gloss a given word.
	 */
	String getGloss(String word);
	
	/**
	 * Name of this lexicon;
	 */
	String getName();
	
	/**
	 * Intersects a word with a given POS with an entry of <code>map</code>,
	 * by percolating the word's hypernym tree and trying to match each node's
	 * key/offset with an existing key/offset in <code>map</code>.
	 * Returns the <code>Pair</code> containing both the intersected offset and
	 * its target value String in the <code>map</code>.
	 */
	Pair<Long, String> intersectMap(String word, POS pos, LexiconMap map, boolean allsenses);
	
}
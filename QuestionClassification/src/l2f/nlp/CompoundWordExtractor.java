package l2f.nlp;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import l2f.nlp.Lexicon;
import l2f.utils.Utils;
/**
 * A compound word is a combination of two or more words that constitute a 
 * single unit of meaning, such as 'recycle bin'. In English/this work, we
 * consider compound words in two cases: i) when the head word is premodified
 * by several nouns; ii) postmodifier: when there's a pp attachment.
 * Composition of premodifier and head or head and postmodifier.
 * 
 * There is also a special case of juxtaposition, which we don't need
 * to consider because WordNet indexes both. In this type of compounding,
 * the head word is linked/concatenated to the premodifier.

 */
public class CompoundWordExtractor {	
	private static Pattern PREMODIFIER_TAGS = Pattern.compile("NNP?S?|JJS?");
	
	private static Pattern PREMODIFIER_WORDS_IGNORE = Pattern.compile("many|much|main");
	
	private static Pattern POSTMODIFIER_TAGS = Pattern.compile("DT|NNP?S?|JJS?");
	
	/**
	 * Lexicon used to look up compound words.
	 */
	private Lexicon lexicon;
	
	public CompoundWordExtractor(Lexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	/**
	 * Tries to identify a compound word in the <code>words</code>
	 * collection, using <code>head</code> as the head word.
	 * It assumes <code>words</code> contains <code>head</code>.
	 * 
	 * Example: What is the capital of Portugal?
	 * 	=> Returns 'capital of Portugal' from the head 'capital'
	 * Example: What is the personal identification number of Bill Gates?
	 *  => Returns 'personal identification number' from the head 'number'
	 * 
	 * @param words question 
	 * @param head head word
	 * @return compound word if it can by found, if it can't, return unmodified headword.
	 */
	public String tryGetCompoundWord(List<String> words, List<String> tags,
			String head) {
		int headIdx = words.indexOf(head);
		Deque<String> premodifiers = getPremodifiers(words, tags, headIdx);
		if (!premodifiers.isEmpty()) {
			premodifiers.addLast(head);
			String compound = Utils.join(premodifiers, ' ');
			// premodifier + head;
			//System.err.println("premodifiers + head = " + compound);
			if (lexicon.hasWord(compound)) {
				return compound;
			} else if (premodifiers.size() > 2) {
				// Try again without the first premodifier. This can be useful if
				// the first modifier is one of the following:
				// 	attribute adjective (e.g., _red_ Chinese flag) or
				// 	indefinite adjectives (e.g., many; a few) or
				// 	numeral adjective (e.g., _only_ world cup, _first_ world cup)
				premodifiers.removeFirst();
				compound = Utils.join(premodifiers, ' ');
				//System.err.println("premodifiers skip + head = " + compound);
				if (lexicon.hasWord(compound)) {
					return compound;
				}
			}
		}
		Deque<String> postmodifiers = getPostmodifiers(words, tags, headIdx);
		if (postmodifiers.size() > 1) {
			//String compound = head + postmodifiers;
			postmodifiers.addFirst(head);
			String compound = Utils.join(postmodifiers, ' ');
			//System.err.println(compound);
			if (lexicon.hasWord(compound)) {
				return compound;
			}
		}
		return head;
	}
	
	private Deque<String> getPremodifiers(List<String> words, List<String> tags, 
			int headIdx) {
		Deque<String> premodifiers = new LinkedList<String>();
		int previous = headIdx - 1;
		for (int i = previous; i >= 0 && PREMODIFIER_TAGS.matcher(tags.get(i)).matches() &&
			!PREMODIFIER_WORDS_IGNORE.matcher(words.get(i)).matches(); i--) {
			premodifiers.addFirst(words.get(i));
		}
		return premodifiers;
	}
	
	/**
	 * Examples: master_of_science_in_engineering;
	 * arcuate_artery_of_the_kidney mixed;
	 * capital of the bahamas. 
	 */	
	private Deque<String> getPostmodifiers(List<String> words, List<String> tags,
			int headIdx) {		
		int size = words.size();
		int next = headIdx + 1;
		Deque<String> postmodifiers = new LinkedList<String>();
		if (next < size && tags.get(next).equals("IN")) {
			postmodifiers.add(words.get(next));
			for (int i = next + 1; i < size && POSTMODIFIER_TAGS.matcher(tags.get(i)).matches(); i++) {
				postmodifiers.add(words.get(i));	
			}
		}
		return postmodifiers;
	}

	@SuppressWarnings("unused")
	@Deprecated
	private String getPostmodifier(List<String> words, List<String> tags,
			int headIdx) {		
		int size = words.size();
		int next = headIdx + 1;
		StringBuilder sb = new StringBuilder(32);
		if (next < size && tags.get(next).equals("IN")) {
			sb.append(' ');
			sb.append(words.get(next));
			for (int i = next + 1; i < size && POSTMODIFIER_TAGS.matcher(tags.get(i)).matches(); i++) {
				sb.append(' ');
				sb.append(words.get(i));	
			}
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private String getPremodifier(List<String> words, List<String> tags, 
			int headIdx) {
		StringBuilder sb = new StringBuilder();
		int previous = headIdx - 1;
		for (int i = previous; i >= 0 && PREMODIFIER_TAGS.matcher(tags.get(i)).matches(); i--) {
			sb.insert(0, ' '); 
			sb.insert(0, words.get(i));
		}
		return sb.toString();
	}
}

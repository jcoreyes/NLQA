package l2f.nlp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.PCFGLA.BerkeleyParser.Options;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Numberer;

/**
 * Factory for creating instances of the Berkeley Parser, using
 * a caching mechanism to avoid loading two instances of the same 
 * serialized grammar file.
 * 
 * @author Jo�o
 */
public enum ParserFactory {
	INSTANCE;
		
	private Map<String, Parser> grammarToParser = new HashMap<String, Parser>();
	
	public Parser getParser(String grammarFile) {
		synchronized(grammarToParser) {
			Parser parser = grammarToParser.get(grammarFile);
			if (parser == null) {
				Options opts = new Options();
				double threshold = 1.0;
				ParserData pData = ParserData.Load(grammarFile);
			    Grammar grammar = pData.getGrammar();
			    Numberer.setNumberers(pData.getNumbs());
			    parser = new Parser(grammar, pData.getLexicon(), threshold,-1,opts.viterbi,
			    		opts.substates, opts.scores, opts.accurate, false, true, true);
			    //parser.binarization = pData.getBinarization();
				grammarToParser.put(grammarFile, parser);
			}
			return parser;
		}
	}
}

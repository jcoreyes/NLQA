package l2f.nlp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

/**
 * Factory for creating <code>l2f.nlp.Tokenizer</code> interface compliant 
 * tokenizers, by means of wrapping other vendors' implementations. 
 * 
 */
public class TokenizerFactory {

    public enum TokenizerType {
        PTB,
        LINGPIPE;
    }

    public static Tokenizer newTokenizer(TokenizerType type) {
        switch (type) {
            case PTB:
                return new PTBTokenizer();
            case LINGPIPE:
                return new Tokenizer() {

                    private final com.aliasi.tokenizer.TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;

                    @Override
                    public List<String> tokenize(String line) {
                        Iterator<String> iterator =
                                factory.tokenizer(line.toCharArray(), 0, line.length()).iterator();
                        List<String> tokens = new ArrayList<String>();
                        while (iterator.hasNext()) {
                            tokens.add(iterator.next());
                        }
                        return tokens;
                    }
                };
            default:
                throw new IllegalArgumentException("Unrecognized tokenizer type: '" + type + "'.");
        }
    }
}

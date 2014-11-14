package l2f.interpretation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import l2f.utils.ResourceLoadException;
import l2f.utils.Utils;

import org.xml.sax.InputSource;
import com.aliasi.util.Streams;

/**
 * LexiconMap is used to map Lexicon keys (offsets) into question categories.
 * A typical use is for WordNet, wherein each offset corresponds to a synset,
 * or synonym set.
 */
public class LexiconMap extends HashMap<Long, String> {

    public LexiconMap(String filename) {
        File file = Utils.checkInputFile(filename);
        loadMap(file);
    }

    @SuppressWarnings("deprecation")
    private void loadMap(File file) {
        LexiconMapParser parser = new LexiconMapParser();
        parser.setHandler(new LexiconMapHandler() {

            @Override
            public void handle(Map<Long, String> out) {
                putAll(out);
            }
        });
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            InputSource inSource = new InputSource(inputStream);
            parser.parse(inSource);
        } catch (IOException ioe) {
            throw new ResourceLoadException("Couldn't load LexiconMap.", ioe);
        } finally {
            Streams.closeInputStream(inputStream);
        }
    }
    private static final long serialVersionUID = 1936957942132058888L;
}

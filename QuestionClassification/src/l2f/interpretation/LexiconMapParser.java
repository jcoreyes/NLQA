package l2f.interpretation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.aliasi.corpus.XMLParser;

/**
 * <code>WordNetMapParser</code> is able to parse the WordNetMap XML file.
 * The parser takes a visitor in the form of a <code>WordNetMapHandler</code>,
 * which returns a <code>Map</code> from 'offsets' to a 'question category'.
 */
public class LexiconMapParser extends XMLParser<LexiconMapHandler> { 

	/**
	 * Construct a WordNetMap parser with the specified handler.
	 *
	 * @param handler Handler for the map.
	 */
	public LexiconMapParser(LexiconMapHandler handler) {
		super(handler);
	}
	
	public LexiconMapParser() {
	}

	/**
	 * Parse the WordNetMap in the specified input source
	 * and give them to the specified handler for processing.
	 *
	 * @param inSource Input source to parse.
         * @param handler LexiconMap to process.
         * @throws IOException
         * @throws SAXException
	 */
	public void parse(InputSource inSource,
			LexiconMapHandler handler)
	throws IOException, SAXException {
		setHandler(handler);
		parse(inSource);
	}

	@Override
	protected DefaultHandler getXMLHandler() {
		return new VisitingHandler(getHandler());
	}

	private static class VisitingHandler extends DefaultHandler {    	
		private static final String WNMAP_ROOT_ELT = "map";
		//private static final String WNMAP_ROOT_VERSION_ATT = "wn-version";
		private static final String WNMAP_CATEGORY_ELT = "category";
		private static final String WNMAP_CATEGORY_NAME_ATT = "name";        
		private static final String WNMAP_TARGET_ELT = "target";
		private static final String WNMAP_TARGET_OFFSET_ATT = "offset";
		//private static final String WNMAP_TARGET_POS_ATT = "POS";
		//private static final String WNMAP_TARGET_WORDS_ATT = "words";

		private final LexiconMapHandler visitor;

		private Map<Long, String> _map = new HashMap<Long, String>();                        
		
		//private String _version;
		private String _currentCategory;

		public VisitingHandler(LexiconMapHandler visitor) {
			this.visitor = visitor;
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts)
		throws SAXException {
			if (qName.equals(WNMAP_ROOT_ELT)) {
				//_version = atts.getValue(WNMAP_ROOT_VERSION_ATT);
			} else if (qName.equals(WNMAP_CATEGORY_ELT)) {
				_currentCategory = atts.getValue(WNMAP_CATEGORY_NAME_ATT); 
			} else if (qName.equals(WNMAP_TARGET_ELT)) {
				String offset = atts.getValue(WNMAP_TARGET_OFFSET_ATT);
				//String pos = atts.getValue(WNMAP_TARGET_POS_ATT);
				//String words = atts.getValue(WNMAP_TARGET_WORDS_ATT);
				if (!offset.isEmpty()) {
					_map.put(Long.valueOf(offset), _currentCategory);
				}
			}
		}

		@Override
		public void endDocument() throws SAXException {
			visitor.handle(_map);
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
		}

		@Override
		public void endElement(String uri, String localName, String name)
		throws SAXException {
		}
	}
}
package l2f.config;

import java.io.File;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Config {

    private static Config config = null;

    protected Config() {
        // only to defeat instantiation.
    }

    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }
    /*********
     * Question interpretation variables
     *********/
    /** classification */
    /**
     * The directory where the train set is
     */
    public static String classification_trainDir = "";
    /**
     * The directory where the test set is
     */
    public static String classification_testDir = "";
    public static boolean classification_forceTraining = false;
    public static String classification_modelFile = "";
    /** analysis*/
    public static String questionAnalysis_tokenizerType = "";
    public static String questionAnalysis_parserGrammarFile = "";
    public static String questionAnalysis_lexiconmapFile = "";
    /**Natural Language Processing */
    public static String nlp_wordnetProperties = "";

    public static void parseConfig(String configfile) {
        try {
            //InputStream is = new FileInputStream(configfile);
            File file = new File(configfile);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr;
            Node node;

            /**classification*/
            expr = xpath.compile("//interpretation/classification/trainDir");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            classification_trainDir = node.getTextContent();

            expr = xpath.compile("//interpretation/classification/testDir");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            classification_testDir = node.getTextContent();

            expr = xpath.compile("//interpretation/classification/forceTraining");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            classification_forceTraining = Boolean.parseBoolean(node.getTextContent());

            expr = xpath.compile("//interpretation/classification/modelsClassifierFile");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            classification_modelFile = node.getTextContent();

            /**analysis*/
            expr = xpath.compile("//interpretation/analysis/tokenizerType");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            questionAnalysis_tokenizerType = node.getTextContent();

            expr = xpath.compile("//interpretation/analysis/parserGrammarFile");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            questionAnalysis_parserGrammarFile = node.getTextContent();

            expr = xpath.compile("//interpretation/analysis/lexiconmapFile");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            questionAnalysis_lexiconmapFile = node.getTextContent();

            /**natural language processing */
            expr = xpath.compile("//nlp/wordnet/properties");
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            nlp_wordnetProperties = node.getTextContent();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

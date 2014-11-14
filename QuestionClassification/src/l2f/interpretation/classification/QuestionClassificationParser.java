package l2f.interpretation.classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import l2f.interpretation.AnalyzedQuestion;
import l2f.interpretation.InterpretedQuestion;
import l2f.interpretation.QuestionAnalyzer;
import l2f.utils.Utils;

import org.xml.sax.InputSource;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

/**
 * The <code>QuestionClassificationParser</code> class parses a file of
 * question classification instances, in the following format:
 * 
 * <blockquote><pre>CATEGORY_SUBCATEGORY Word_1 Word_2 Word_N</pre></blockquote>
 */
public class QuestionClassificationParser
extends Parser<ObjectHandler<InterpretedQuestion>> {

	/**
	 * Indicates whether or not to consider fine grained categories.
	 */
	private boolean finer;
	/**
	 * Category separator, when using coarse grained categories.
	 * Default: "_" (underscore)
	 */
	private String separator;
	/**
	 * Question analyzer used to convert question strings into
	 * an AnalyzedQuestion container.
	 */
	private QuestionAnalyzer questionAnalyzer;

	/**
	 * Construct a question classification parser.
	 * @param handler Classification handler for data.
	 */
	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer,
			ObjectHandler<InterpretedQuestion> handler,
			boolean useFineGrainedCategories, String separator) {
		super(handler);
		this.finer = useFineGrainedCategories;
		this.separator = separator;
		this.questionAnalyzer = questionAnalyzer;
	}

	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer,
			boolean useFineGrainedCategories, String separator) {
		this(questionAnalyzer, null, useFineGrainedCategories, separator);
	}

	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer,
			boolean useFineGrainedCategories) {
		this(questionAnalyzer, null, useFineGrainedCategories, "_");
	}

	/**
	 * Construct a question classification parser, using the default
	 * parameters: finer categories and empty separator.
	 */
	public QuestionClassificationParser(QuestionAnalyzer questionAnalyzer) {
		this(questionAnalyzer, true, Utils.EMPTY);
	}

	static String stripComment(String line) {
		int commentStart = line.indexOf('#');
		return commentStart < 0
				? line
						: line.substring(0, commentStart);
	}


	@Override
	public void parse(InputSource is) throws IOException {
		try {
			String fileName = is.getSystemId().replaceFirst("file:", "");
			FileReader fr = new FileReader(fileName);
			//avoiding hidden directories
			if(fileName.contains("/.")){
				return;
			}
			BufferedReader br = new BufferedReader(fr);

			String line = "";
			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				if (line.matches("[A-Z_]+ .*")) {
					lineNumber++;
					if (lineNumber % 100 == 0) {
						System.out.println("Processing line " + lineNumber + ".");
					}

					line = stripComment(line);
					int questionStart = line.indexOf(' ');
					if (questionStart < 0) {
						return;
					}
					String category = line.substring(0, questionStart).trim();
					if (!this.finer) {
						category = category.substring(0, category.indexOf(this.separator)).trim();
					}
					final String question = line.substring(questionStart + 1).trim();
					AnalyzedQuestion analyzedQuestion = this.questionAnalyzer.analyze(question);
					InterpretedQuestion interpretedQuestion = new InterpretedQuestion(analyzedQuestion, QuestionCategory.valueOf(category));
					getHandler().handle(interpretedQuestion);
				}
			}
		} catch (FileNotFoundException fnf) {
			System.err.println(fnf.getMessage());
		}

	}




	@Override
	public void parseString(char[] chars, int i, int i1) throws IOException {

		String s = new String(chars);
		s = "";
	}

}

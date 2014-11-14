package l2f;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import l2f.config.Config;
import l2f.interpretation.InterpretedQuestion;
import l2f.interpretation.QuestionAnalyzer;
import l2f.interpretation.classification.QuestionClassificationCorpus;
import l2f.interpretation.classification.QuestionClassificationParser;
import l2f.interpretation.classification.QuestionClassifier;
import l2f.interpretation.classification.QuestionClassifierFactory;
import l2f.interpretation.classification.features.FeatureSet;
import l2f.interpretation.classification.features.TestFeatureExtractor;
import l2f.utils.FileUtils;
import l2f.utils.SetUtils;
import l2f.utils.Utils;

import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.corpus.ObjectHandler;

/**
 * Evaluates a <code>QuestionClassifier</code> using different feature
 * combinations, and reports the results to the filesystem.
 * 
 */
public class QuestionClassifierEvaluator {



	/**
	 * Go, Go Gadget!
	 */
	public void run(boolean allsenses, String filename, ArrayList<String> classifiers,
			String dir, List<FeatureSet> features, boolean useFineGrainedCategories) throws IOException {

		File testDir = FileUtils.checkDir(Config.classification_testDir);

		QuestionAnalyzer questionAnalyzer = new QuestionAnalyzer(allsenses);

		System.out.println("Loaded QuestionAnalyzer.");

		final SortedMap<InterpretedQuestion, String> testInstances = new TreeMap<InterpretedQuestion, String>();
		load(questionAnalyzer, testInstances, testDir, useFineGrainedCategories);
		System.out.println("Loaded test file.");


		File trainingDir = FileUtils.checkDir(Config.classification_trainDir);
		QuestionClassificationCorpus<InterpretedQuestion> trainInstances = new QuestionClassificationCorpus<InterpretedQuestion>(
				new QuestionClassificationParser(questionAnalyzer, useFineGrainedCategories),
				trainingDir, testDir);
		System.out.println("Loaded training file.");


		// evaluate
		List<List<FeatureSet>> featurePowerSet = new ArrayList<List<FeatureSet>>();
		featurePowerSet.add(features);
		//featurePowerSet = SetUtils.powerSet(features, false);
		Map<Double, EnumSet<FeatureSet>> scores = new TreeMap<Double, EnumSet<FeatureSet>>(new Comparator<Double>() {

			@Override
			public int compare(Double arg1, Double arg2) {
				return (int) (arg2 - arg1);
			}
		});

		long startTotal = System.currentTimeMillis();
		for (List<FeatureSet> featureCombination : featurePowerSet) {
			long start = System.currentTimeMillis();
			EnumSet<FeatureSet> current = EnumSet.copyOf(featureCombination);
			System.out.println("=> " + current);

			TestFeatureExtractor featureExtractor = new TestFeatureExtractor(current);
			ConfusionMatrix matrix = null;
			String classifier = "";
			for (int i = 0; i < classifiers.size(); i++) {
				File f = new File(filename);
				FileWriter fw = new FileWriter(f);
				classifier += classifiers.get(i) + "_";
				classifier = classifier.replaceAll("_$", "");
				System.out.println("=> " + classifier);

				QuestionClassifier<InterpretedQuestion> qc =
						QuestionClassifierFactory.newQuestionClassifier(questionAnalyzer,
								featureExtractor, trainInstances, classifiers.get(i), useFineGrainedCategories);

				start = System.currentTimeMillis();
				matrix = qc.classify(testInstances, fw);
				long end = System.currentTimeMillis();


				BufferedWriter writer = new BufferedWriter(new FileWriter(dir + classifier
						+ (useFineGrainedCategories ? "_FINE" : "_COARSE")
						+ (allsenses ? "_ALLSENSES_" : "_1STSENSE_") + Utils.join(current, "_")));
				writer.write(new StringBuffer(matrix.toString()).toString());
				writer.newLine();
				writer.write(String.valueOf(matrix.totalAccuracy()));
				writer.newLine();

				int instances = testInstances.keySet().size();

				String time = classifier + (useFineGrainedCategories ? " FINE" : " COARSE") + " Question classification of " + instances
						+ " instances in " + (end - start) + " ms.";

				writer.write(time);
				fw.write("\n\n" + time + " with accuracy " + matrix.totalAccuracy());
				System.out.println("\n" + time + " with accuracy " + matrix.totalAccuracy());


				System.out.println("Question classification of a single instance in " + ((end - start) / (double) instances) + " ms.");
				writer.close();
				scores.put(matrix.totalAccuracy(), current);
				fw.close();
				// clean up and rest for a while
				System.gc();
				try {
					Thread.sleep(3000); // lets the CPU breathe : )
				} catch (InterruptedException e) {
				}
			}
		}
		long endTotal = System.currentTimeMillis();
		String timeTotal = "Question classification of " + featurePowerSet
				+ " in " + (endTotal - startTotal) + " ms.";
		System.out.println(timeTotal);
	}

	@SuppressWarnings("unused")
	private void filter(List<List<FeatureSet>> featurePowerSet) {
		featurePowerSet.removeAll(
				SetUtils.powerSet(new ArrayList<FeatureSet>(Arrays.asList(FeatureSet.UNIGRAM,
						FeatureSet.BIGRAM, FeatureSet.TRIGRAM, FeatureSet.LENGTH)), false));
		featurePowerSet.removeAll(
				SetUtils.powerSet(new ArrayList<FeatureSet>(Arrays.asList(FeatureSet.LENGTH,
						FeatureSet.POS)), false));
	}

	private void load(QuestionAnalyzer questionAnalyzer,
			final Map<InterpretedQuestion, String> testInstances, File testDir,
			boolean useFineGrainedCategories) throws IOException {
		QuestionClassificationCorpus<InterpretedQuestion> corpus =
				new QuestionClassificationCorpus<InterpretedQuestion>(
						new QuestionClassificationParser(questionAnalyzer, useFineGrainedCategories), testDir);
		corpus.visitTest(new ObjectHandler<InterpretedQuestion>() {

			@Override
			public void handle(InterpretedQuestion question) {
				testInstances.put(question, question.getQuestionCategory().toString());
			}
		});
	}

	public static void main(String[] args) throws IOException {

		QuestionClassifierEvaluator evaluator = new QuestionClassifierEvaluator();

		Config.parseConfig("config/config_en.xml");
		String[] categoriesVec = {


				//"-u-", "-b-", "-t-", "-x-", "-fw-", "-nc-", "-nt-", "-p-",
				//"-nr-",
				//"-ph-",
				"-bu-c-h-",
				"-h-",
				"-c-",
				//"-x-nt-nc-",
				//"-u-b-t-fw-",
				//"-u-", "-b-", "-t-", "-x-", "-fw-", "-nc-", "-nt-", "-p-",
				//"-nr-",
				//"-nr-",
				//"-h-",
				//"-h-nr-",
				//"-nr-"
				//"-x-nt-nc-",
				//"-u-b-t-fw-"
				//"-u-b-t-", "-u-b-t-",
				//"-bu-bb-bt-", "-bu-bb-bt-",
				//"-bt-", "-bt-",
				//"-x-l-nt-", "-x-l-nt-",
				//"-c-", "-c-", "-h-", "-h-",
				//"-bu-c-", "-bu-c-", "-bu-h-", "-bu-h-", "-h-c-", "-h-c-", "-bu-c-h-",
				//"-bu-c-h-"
		};
		String[] grainedVec = {

				"true", "true", "true", "true", "true", "true", "true", "true", "true", "true", "true", "true",
				"false", "false", "false", "false", "false", "false", "false","false", "false","false", "false"
		};

		for (int i = 0; i < categoriesVec.length; i++) {

			String classifiersArgument = "s";
			String featuresArgument = categoriesVec[i];
			String allsensesArgument = "false";
			String useFineGrainedCategoriesArgument = grainedVec[i];

			if (args.length > 0) {
				if (args.length < 4) {
					System.out.println("java -jar <file>.jar <classifiers> <features> <allsenses> <useFineGrainedCategories>\n"
							+ "*** Classifiers: r=rules  s=svm\n"
							+ "*** Features: -u-=unigram  -h-=headword  -c-=category  -b-=bigram  -t-=trigram  x=wordshape  "
							+ "p=pos  l=length  nr=NER_replace  ni=NER_increment\n"
							+ "*** AllSenses = true or false\n"
							+ "*** UseFineGrainedCategories = true or false");
					System.exit(0);
				}
				classifiersArgument = args[0];
				featuresArgument = args[1];
				allsensesArgument = args[2];
				useFineGrainedCategoriesArgument = args[3];
			}

			String classifiersString = "";
			ArrayList<String> classifiers = new ArrayList<String>();
			if (classifiersArgument.matches(".*r.*")) {
				classifiers.add("RULES");
				classifiersString += "+rulesclassifier";
			}
			if (classifiersArgument.matches(".*s.*")) {
				classifiers.add("SVM");
				classifiersString += "+svmclassifier";
			}
			String f = "-";
			List<FeatureSet> features = new ArrayList<FeatureSet>();
			if (classifiers.size() == 1 && classifiers.get(0).equalsIgnoreCase("RULES")) {
				features.add(FeatureSet.DUMMY);
			} else {
				if (featuresArgument.matches(".*-u-.*")) {
					features.add(FeatureSet.UNIGRAM);
					f += "unigram-";
				}
				if (featuresArgument.matches(".*-bu-.*")) {
					features.add(FeatureSet.BINARY_UNIGRAM);
					f += "binary_unigram-";
				}
				if (featuresArgument.matches(".*-b-.*")) {
					features.add(FeatureSet.BIGRAM);
					f += "bigram-";
				}
				if (featuresArgument.matches(".*-bb-.*")) {
					features.add(FeatureSet.BINARY_BIGRAM);
					f += "binary_bigram-";
				}
				if (featuresArgument.matches(".*-t-.*")) {
					features.add(FeatureSet.TRIGRAM);
					f += "trigram-";
				}
				if (featuresArgument.matches(".*-bt-.*")) {
					features.add(FeatureSet.BINARY_TRIGRAM);
					f += "binary_trigram-";
				}
				if (featuresArgument.matches(".*-c-.*")) {
					features.add(FeatureSet.CATEGORY);
					f += "category-";
				}
				if (featuresArgument.matches(".*-h-.*")) {
					features.add(FeatureSet.HEADWORD);
					f += "headword-";
				}
				if (featuresArgument.matches(".*-x-.*")) {
					features.add(FeatureSet.WORD_SHAPE);
					f += "word_shape-";
				}
				if (featuresArgument.matches(".*-bx-.*")) {
					features.add(FeatureSet.BINARY_WORD_SHAPE);
					f += "binary_word_shape-";
				}
				if (featuresArgument.matches(".*-p-.*")) {
					features.add(FeatureSet.POS);
					f += "pos-";
				}
				if (featuresArgument.matches(".*-l-.*")) {
					features.add(FeatureSet.LENGTH);
					f += "length-";
				}
				if (featuresArgument.matches(".*-ni-.*")) {
					features.add(FeatureSet.NER_INCR);
					f += "ner_increment-";
				}
				if (featuresArgument.matches(".*-nr-.*")) {
					features.add(FeatureSet.NER_REPL);
					f += "ner_replace-";
				}
			}

			boolean allsenses = (Boolean.valueOf(allsensesArgument)).booleanValue();
			boolean useFineGrainedCategories = (Boolean.valueOf(useFineGrainedCategoriesArgument)).booleanValue();
			String dir = "results/classification/questions/" + (useFineGrainedCategories ? "fine" : "coarse") + "/";

			boolean exists = (new File(dir)).exists();
			if (!exists) {
				boolean success = (new File(dir)).mkdirs();
				if (success) {
					System.out.println("Directory: " + dir + " created");
				}
			}
			System.out.println("STARTING:   Classifiers: " + classifiersString + " "
					+ "Features: " + f
					+ (allsenses ? " allsenses" : " 1stsense")
					+ (useFineGrainedCategories ? " fine" : " coarse"));


			evaluator.run(allsenses,
					dir + classifiersString.replaceFirst("^\\+", "")
					+ (useFineGrainedCategories ? "-FINE" : "-COARSE")
					+ (allsenses ? "allsenses" : "1stsense") + f.replaceAll("-$", "") + ".txt",
					classifiers, dir, features, useFineGrainedCategories);

			System.out.println("ENDED:   Classifiers: " + classifiersString + " Features: " + f + (allsenses ? " allsenses" : " 1stsense")
					+ (useFineGrainedCategories ? " fine" : " coarse"));
		}
	}
}

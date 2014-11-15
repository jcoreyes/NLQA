package l2f.interpretation.classification;

import java.io.File;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.DiskCorpus;
import com.aliasi.corpus.Parser;

public class QuestionClassificationCorpus<E> extends 
	DiskCorpus<ObjectHandler<E>> {

	public QuestionClassificationCorpus(
			Parser<ObjectHandler<E>> parser,
			File trainDir, File testDir) {
		super(parser, trainDir, testDir);		
	}

	public QuestionClassificationCorpus(
			Parser<ObjectHandler<E>> parser,
			File testDir) {
		super(parser, null, testDir);		
	}
}

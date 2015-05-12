import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Test {
	public static void main(String[] args) {
		MaxentTagger tagger = new MaxentTagger(
				"/usr/local/lib/my-java-libs/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger");
		Reader reader = new StringReader("This is test's OU. One more sentence.");
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		dp.setSentenceFinalPuncWords(new String[] { ".", "MR.", "test." });
		
		List<String> sentenceList = new ArrayList<String>();
		
		System.out.println(tagger.tagString("This is test Dr. One more sentence."));
		

		String test = "Let's try this out.";

		PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(reader);

		for (Word w : ptb.tokenize()) {
			System.out.println(w.word());
		}
		
		// for (List<HasWord> sentence : dp) {
		// String sentenceString = Sentence.listToString(sentence);
		//
		// PTBTokenizer<Word> ptb = PTBTokenizer
		// .newPTBTokenizer(new StringReader(sentenceString));
		//
		// for (Word w : ptb.tokenize()) {
		// System.out.println(w);
		// }
		//
		// sentenceList.add(sentenceString.toString());
		// }
		// for (String sentence : sentenceList) {
		// System.out.println(sentence);
		// }
	}
}

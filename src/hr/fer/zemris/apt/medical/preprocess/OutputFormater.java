package hr.fer.zemris.apt.medical.preprocess;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.tartarus.snowball.ext.englishStemmer;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class OutputFormater {

	private englishStemmer stemmer;
	private MaxentTagger tagger;

	public OutputFormater() {
		stemmer = new englishStemmer();
		tagger = new MaxentTagger(
				"/usr/local/lib/my-java-libs/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger");

	}

	private String stem(String word) {
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent().toLowerCase();
	}

	private String wordShape(String word) {

		IntStream stream = word.chars();

		Iterator<Integer> it = stream.iterator();

		StringBuilder sb = new StringBuilder();

		while (it.hasNext()) {
			int next = it.next();
			if (Character.isAlphabetic(next)) {
				if (Character.isUpperCase(next)) {
					sb.append('C');
				} else {
					sb.append('c');
				}
			} else {
				if (Character.isDigit(next)) {
					sb.append('n');
				} else {
					sb.append((char) next);
				}
			}
		}

		return sb.toString();
	}

	private void printLine(PrintWriter pw, String... inputs) {
		for (String in : inputs) {
			pw.print(in + " ");
		}
		pw.println();
	}

	public void processText(String text, WordTagFinder wtf, OutputStream os) {
		PrintWriter pw = new PrintWriter(os);

		Reader reader = new StringReader(text);

		// String ls = System.getProperty("line.separator");

		PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(reader);
		Set<String> sentenceDelimeiters = new HashSet<>(
				Arrays.asList(DocumentPreprocessor.DEFAULT_SENTENCE_DELIMS));

		String[] posTags = tagger.tagString(text).split(" ");

		for (int i = 0; i < posTags.length; i++) {
			posTags[i] = posTags[i].split("_")[1];
		}

		int start = 0;
		int end = 0;
		boolean ok = true;
		int c = 0;

		List<String> list = new ArrayList<>(5);

		for (Word w : ptb.tokenize()) {

			if (ok) {
				start = w.beginPosition();
				end = w.endPosition();
				if (wtf.isStartingPoint(start)) {
					ok = false;
					list.add(w.word());
				} else {
					String word = w.word();
					// TODO STEM TO LOWER CASE?
					// System.out.println(word + " ----- ");
					printLine(pw, word, posTags[c], wtf.getLabel(start, end),
							stem(word), wordShape(word));
					if (sentenceDelimeiters.contains(word)) {
						pw.println();
					}
				}
			} else {
				end = w.endPosition();
				list.add(w.word());
				int size = list.size();

				if (wtf.isEndingPoint(end)) {
					String firstWord = list.remove(0);
					String label = wtf.getLabel(start, end);

					printLine(pw, firstWord, posTags[c - size + 1], label,
							stem(firstWord), wordShape(firstWord));
					size--;
					label = label.charAt(0) + "I";
					for (String word : list) {
						printLine(pw, word, posTags[c - size + 1],
								wtf.getLabel(start, end), stem(word),
								wordShape(word));
						size--;
					}
					list.clear();
					ok = true;
				}
			}
			c++;
		}
		pw.close();
	}

	public static void main(String[] args) {
		System.out.println(new OutputFormater().wordShape("AbCd.*'"));
	}
}

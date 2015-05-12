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

		int last = -1;

		while (it.hasNext()) {
			int next = it.next();
			int current = -1;
			if (Character.isAlphabetic(next)) {
				if (Character.isUpperCase(next)) {
					current = 'C';
					// sb.append('C');
				} else {
					current = 'c';
					// sb.append('c');
				}
			} else {
				if (Character.isDigit(next)) {
					current = 'd';
					// sb.append('n');
				} else {
					current = 'o';
					// sb.append((char) next);
				}
			}
			if (current != last) {
				sb.append((char) current);
				last = current;
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

		int start = 0;
		int end = 0;
		boolean ok = true;
		int c = 0;

		List<String> list = new ArrayList<>(5);

		List<Word> tokens = ptb.tokenize();

		StringBuilder sb = new StringBuilder();

		for (Word token : tokens) {
			sb.append(token.word()).append(' ');
		}

		String[] posTagsFull = tagger.tagTokenizedString(sb.toString()).split(
				" ");

		String[] posTags = new String[posTagsFull.length];

		for (int i = 0; i < posTags.length; i++) {
			posTags[i] = posTagsFull[i].split("_")[1];
		}

		Iterator<Word> it = tokens.iterator();

		while (it.hasNext()) {
			Word w = it.next();
			if (ok) {
				start = w.beginPosition();
				end = w.endPosition();
				if (wtf.isStartingPoint(start)) {
					if (wtf.isEndingPoint(end)) {
						String word = w.word();
						printLine(pw, word, posTags[c],
								wtf.getLabel(start, end), stem(word),
								wordShape(word));
					} else {
						ok = false;
						list.add(w.word());
					}
				} else {
					String word = w.word();
					// TODO STEM TO LOWER CASE?
					// pw.println(word + " ----- ");
					// String tag = posTags[c];
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
			if (c > posTags.length) {
				System.err.println(c + " " + posTags.length + " "
						+ it.hasNext());
				System.err.println("Not the same");
				System.exit(-1);
			}
		}
		pw.flush();
	}
}

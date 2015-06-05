package hr.fer.zemris.apt.medical.preprocess;

import hr.fer.zemris.apt.seqclassification.models.DocumentBase;

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
	private DocumentBase db;
	private DocumentBase db2;

	public OutputFormater(DocumentBase db, MaxentTagger tagger, DocumentBase db2) {
		stemmer = new englishStemmer();
		this.tagger = tagger;
		this.db = db;
		this.db2 = db2;
		// db = new CUIDicitonary("noStopWords.txt");
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

	private void printLineBetter(PrintWriter pw, String word, int c, int start,
			int end, String[] posTags, WordTagFinder wtf) {
		String stem = stem(word);
		String label = wtf.getLabel(start, end);
		if (label.equals("DB")) {
			disjointLabel = true;
		} else {
			if (label.equals("DI")) {
				if (!disjointLabel) {
					return;
				}
			}
		}
		if (label.equals("OI")
				&& !(lastLabel.equals("OI") || lastLabel.equals("OB"))) {
			return;
		}
		lastLabel = label;
		String sufix;
		String prefix;
		if (word.length() >= 2) {
			sufix = word.substring(word.length() - 2);
			prefix = word.substring(0, 2).toLowerCase();
		} else {
			sufix = word;
			prefix = word.toLowerCase();
		}
		if (db2 == null) {
			printLine(pw, word, posTags[c], label, stem, wordShape(word),
					freq((db.termDocuments(stem).size())),
					Character.isUpperCase(word.charAt(0)) ? "1" : "0", sufix,
					prefix, Integer.toString(start) + "-" + end);
		} else {
			printLine(pw, word, posTags[c], label, stem, wordShape(word),
					freq((db.termDocuments(stem).size())),
					Character.isUpperCase(word.charAt(0)) ? "1" : "0", sufix,
					prefix, db2.termDocuments(stem).isEmpty() ? "0" : "1",
					Integer.toString(start) + "-" + end);

		}
	}

	private void printLineBetter2(PrintWriter pw, String word, int c,
			int start, int end, String[] posTags, WordTagFinder wtf,
			String label) {
		String stem = stem(word);
		if (label.equals("DB")) {
			disjointLabel = true;
		} else {
			if (label.equals("DI")) {
				if (!disjointLabel) {
					return;
				}
			}
		}
		printLine(pw, word, posTags[c], label, stem, wordShape(word),
				freq((db.termDocuments(stem).size())), Integer.toString(start)
						+ "-" + end);
	}

	private String freq(int i) {
		// if (i > 500) {
		// return "H";
		// }
		// if (i > 100) {
		// return "M";
		// }
		// if (i > 0)
		// return "L";
		// return "N";
		return Integer.toString(i);
	}

	private boolean disjointLabel = false;
	private String lastLabel = "";

	public void processText(String text, WordTagFinder wtf, OutputStream os) {
		PrintWriter pw = new PrintWriter(os);

		Reader reader = new StringReader(text);

		PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(reader);
		Set<String> sentenceDelimeiters = new HashSet<>(
				Arrays.asList(DocumentPreprocessor.DEFAULT_SENTENCE_DELIMS));

		int start = 0;
		int end = 0;
		boolean ok = true;
		int c = 0;
		disjointLabel = false;

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
			start = w.beginPosition();
			end = w.endPosition();
			if (wtf.isStartingPoint(start)) {
				if (wtf.isEndingPoint(end)) {
					String word = w.word();
					if (checkForSpecialCase(pw, word, c, start, end, posTags,
							wtf)) {
						continue;
					}
					// printLine(pw, word, posTags[c],
					// wtf.getLabel(start, end), stem(word),
					// wordShape(word));
					printLineBetter(pw, word, c, start, end, posTags, wtf);
				} else {
					String word = w.word();
					if (wtf.isEndingPoint(end - 1)) {
						if (checkForSpecialCase(pw, word, c, start, end,
								posTags, wtf)) {
							continue;
						}
						printLineBetter(pw, word, c, start, end, posTags, wtf);
					} else {
						printLineBetter(pw, word, c, start, end, posTags, wtf);
					}
				}
			} else {
				String word = w.word();
				printLineBetter(pw, word, c, start, end, posTags, wtf);
			}

			if (sentenceDelimeiters.contains(w.word())) {
				if (wtf.getLabel(w.beginPosition(), w.endPosition())
						.equals("O")) {
					pw.println();
					disjointLabel = false;
					lastLabel = "";
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

	private boolean checkForSpecialCase(PrintWriter pw, String word, int c,
			int start, int end, String[] posTags, WordTagFinder wtf) {
		if (!wtf.getLabel(start, end).equals("O")) {
			return false;
		}

		int len = word.length();

		for (int i = 0; i < len; i++) {
			String label = wtf.getLabel(start, start + i);
			if (!label.equals("O")) {
				printLineBetter(pw, word, c, start, start + i, posTags, wtf);
				break;
			}
		}

		return true;
	}

}

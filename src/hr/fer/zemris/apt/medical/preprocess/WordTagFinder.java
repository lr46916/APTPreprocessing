package hr.fer.zemris.apt.medical.preprocess;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordTagFinder implements Iterable<Point> {

	private Map<Point, String> spanLabel;
	private List<Point> sortedLabels;
	private Set<Integer> startingPoints;
	private Set<Integer> endingPoints;

	public WordTagFinder() {
		super();
		spanLabel = new HashMap<Point, String>();
		startingPoints = new HashSet<>();
		endingPoints = new HashSet<>();
	}

	private void storeLabels(String span, String text, boolean isDist,
			boolean first) {
		String[] split = span.split("-");
		int start = Integer.parseInt(split[0]);
		int endSpan = Integer.parseInt(split[1]);
		String spanText = null;
		spanText = text.substring(start, endSpan);
		String[] spanTextWords = spanText.split("\\s+");

		boolean firstDistSingle = first;
		// TODO ako cemo ubacit i HB i HD labele dodat
		// boolean firstDisMultiple = true;
		boolean firstNormal = true;
		int end = 0;

		for (String word : spanTextWords) {
			int size = word.length();
			end = start + size;

			Point p = new Point(start, end);

			startingPoints.add(p.x);
			endingPoints.add(p.y);

			if (!spanLabel.containsKey(p)) {
				if (isDist) {
					if (firstDistSingle) {
						spanLabel.put(p, "DB");
						firstDistSingle = false;
					} else {
						spanLabel.put(p, "DI");
					}
				} else {
					if (firstNormal) {
						spanLabel.put(p, "OB");
						firstNormal = false;
					} else {
						spanLabel.put(p, "OI");
					}
				}
			}

			while (end < endSpan
					&& (text.charAt(end + 1) == ' ' || text.charAt(end + 1) == '\n')) {
				System.out.println("???");
				end++;
			}
			start = end + 1;
		}

		if (end != endSpan && text.length() > end) {
			System.err.println("Fatal Error!!");
			System.err.println(span);

			System.err.println(spanLabel);

			// System.err.println(text.substring(start, end));
			System.err.println(text);
			System.exit(-1);
		}
	}

	public void preprocessText(String text, String pipe) throws IOException {
		// File text = new File(textFile);
		// File pipe = new File(pipeFile);
		//
		// BufferedReader br = new BufferedReader(new InputStreamReader(
		// new BufferedInputStream(new FileInputStream(text))));
		//
		// char[] data = new char[(int) text.length()];
		//
		// br.read(data);
		// br.close();

		String allText = text;

		System.out.println(allText.charAt(177) == '\n');
		System.exit(-1);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new BufferedInputStream(new FileInputStream(pipe))));

		String line = null;

		// TODO dolje
		Set<String> valueSet = new HashSet<>();

		while ((line = br.readLine()) != null) {

			String[] split = line.split("\\|");

			String span = split[1];
			// TODO ??? provjerit sta ti je tocno bilo dok si ovo pisao
			if (valueSet.contains(span)) {
				continue;
			} else {
				valueSet.add(span);
			}

			String[] spans = span.split(",");
			if (spans.length > 1) {
				boolean first = true;
				for (String spanTmp : spans) {
					storeLabels(spanTmp, allText, true, first);
					first = false;
				}
			} else {
				storeLabels(spans[0], allText, false, false);
			}

		}
		br.close();
		sortedLabels = new ArrayList<>(spanLabel.keySet());
		Collections.sort(sortedLabels, (x, y) -> Integer.compare(x.x, y.x));

	}

	public Map<Point, String> getAll() {
		return spanLabel;
	}

	@Override
	public Iterator<Point> iterator() {
		return sortedLabels.iterator();
	}

	private Point p = new Point();

	public String getLabel(int start, int end) {
		p.x = start;
		p.y = end;
		return spanLabel.getOrDefault(p, "O");
	}

	public boolean isStartingPoint(int index) {
		return startingPoints.contains(index);
	}

	public boolean isEndingPoint(int index) {
		return endingPoints.contains(index);
	}

}

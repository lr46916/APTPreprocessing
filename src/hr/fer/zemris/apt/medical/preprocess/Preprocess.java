package hr.fer.zemris.apt.medical.preprocess;

import hr.fer.zemris.apt.seqclassification.models.DocumentBase;
import hr.fer.zemris.apt.seqclassification.models.vsm.CUIDicitonary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Preprocess {

	public static void main(String[] args) throws ClassNotFoundException,
			IOException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File("dict")));

		DocumentBase db = (DocumentBase) ois.readObject();
		ois.close();

		MaxentTagger tagger = new MaxentTagger(
				"/usr/local/lib/my-java-libs/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger");

		WordTagFinder wtf = new WordTagFinder();

		OutputFormater of = new OutputFormater(db, tagger, new CUIDicitonary(
				"SNOMEDres.txt"));

		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);

		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String[] data = br.readLine().split("\\s+");
			String path = data[0];
			if (path.equals("-1")) {
				br.close();
				break;
			}
			File textFile = new File(path);
			FileOutputStream fos = new FileOutputStream(data[1]);

			byte[] textbytes = new byte[(int) textFile.length()];

			FileInputStream fis = new FileInputStream(textFile);

			fis.read(textbytes);
			fis.close();

			String text = new String(textbytes);

			wtf.preprocessText(text, textFile.getAbsolutePath().split("\\.")[0]
					+ ".pipe");

			of.processText(text, wtf, fos);
			PrintWriter pw = new PrintWriter(outputFile);
			pw.println("done");
			pw.close();
			fos.close();
			br.close();
		}

	}

}

package hr.fer.zemris.apt.medical.preprocess;

import hr.fer.zemris.apt.seqclassification.models.DocumentBase;
import hr.fer.zemris.apt.seqclassification.models.vsm.CUIDicitonary;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class MainTest {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File("dict")));

		DocumentBase db = (DocumentBase) ois.readObject();
		ois.close();

		MaxentTagger tagger = new MaxentTagger(
				"/usr/local/lib/my-java-libs/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger");

		WordTagFinder wtf = new WordTagFinder();

		OutputFormater of = new OutputFormater(db, tagger, new CUIDicitonary(
				"SNOMEDres.txt"));

		File dir = new File(args[0]);
		String[] list = null;
		if (dir.isDirectory()) {
			list = dir.list((direc, name) -> name.endsWith(".text"));
		} else {
			list = new String[] { dir.getName() };
			dir = dir.getAbsoluteFile().getParentFile();
		}

		// list = new String[] { "07797-005646.text" };//
		// "25585-058370.text","00414-104513.text"};

		for (String fn : list) {
			File textFile = null;
			// fn = "00414-104513.text";
			textFile = new File(dir.getAbsolutePath() + "/" + fn);

			byte[] textbytes = new byte[(int) textFile.length()];

			FileInputStream fis = new FileInputStream(textFile);

			fis.read(textbytes);
			fis.close();

			String text = new String(textbytes);

			// System.out.println(textFile.getName());
			// System.out.println(text);
			// System.exit(-1);

			wtf.preprocessText(text,
					dir.getAbsolutePath() + "/" + fn.split("\\.")[0] + ".pipe");

			// int c = 0;
			// for (Point p : wtf) {
			// // if (wtf.getLabel(p.x, p.y).equals("OB")) {
			// System.out.println(p + ": " + wtf.getLabel(p.x, p.y)
			// + text.substring(p.x, p.y));
			// c++;
			// // }
			// }
			// System.out.println(c);
			// System.exit(-1);

			of.processText(text, wtf, System.out);
			System.out.println();
			// System.exit(-1);
		}

	}

}

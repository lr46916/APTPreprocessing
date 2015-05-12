package hr.fer.zemris.apt.medical.preprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainTest {

	public static void main(String[] args) throws IOException {

		WordTagFinder wtf = new WordTagFinder();
		OutputFormater of = new OutputFormater();

		File dir = new File(args[0]);

		String[] list = dir.list((direc, name) -> name.endsWith(".text"));

		for (String fn : list) {

			File textFile = new File(dir.getAbsolutePath() + "/" + "25585-058370.text");//dir.getAbsolutePath() + "/" + fn);

			byte[] textbytes = new byte[(int) textFile.length()];

			FileInputStream fis = new FileInputStream(textFile);

			fis.read(textbytes);
			fis.close();

			String text = new String(textbytes);
			
//			System.out.println(textFile.getName());
//			System.out.println(text);
//			System.exit(-1);
			
			wtf.preprocessText(text,
					dir.getAbsolutePath() + "/" + fn.split("\\.")[0] + ".pipe");

			// for(Point p : wtf){
			// System.out.println(p + ": " + wtf.getLabel(p.x, p.y));
			// }
			System.out.println("ONE!" + "ASDASD");
			of.processText(text, wtf, System.out);
		}

	}

}

package hr.fer.zemris.apt.medical.preprocess;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainTest {

	public static void main(String[] args) throws IOException {

		WordTagFinder wtf = new WordTagFinder();
		OutputFormater of = new OutputFormater();

		File dir = new File(args[0]);

		String[] list = dir.list((direc, name) -> name.endsWith(".text"));

//		list = new String[]{"07780-347384.text"};//"25585-058370.text","00414-104513.text"};
		
		for (String fn : list) {
			
//			fn = "00414-104513.text";
			
			File textFile = new File(dir.getAbsolutePath() + "/" + fn);

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

//			for (Point p : wtf) {
//				System.out.println(p + ": " + wtf.getLabel(p.x, p.y));
//			}
//			System.exit(-1);
			
			of.processText(text, wtf, System.out);
//			System.exit(-1);
		}

	}

}

package tomba2.tool;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class BuildJPSXdecXml {
	
	public static void main(String[] args) throws FileNotFoundException {
		PrintWriter pw=new PrintWriter(new FileOutputStream("D:\\ps3\\hanhua\\jpsxdec_v0-99-7_rev3397\\my.xml"));
		pw.println("<?xml version=\"1.0\"?>");
		pw.println("<!-- usage: java -jar jpsxdec.jar -x my.idx -i 88 -replaceframes my.xml -->");
		pw.println("<str-replace version=\"0.2\">");
		for(int i=170;i<=255;i++){
			pw.printf("<replace frame=\"%d\">logo.bmp</replace>\n", i);
//			pw.printf("<partial-replace frame=\"%d\" tolerance=\"0\" rect=\"0,180,320,60\">logo.bmp</partial-replace>\n", i); //too blurred
		}
		pw.print("</str-replace>");
		pw.flush();
		pw.close();
		System.out.println("finish");
	}

}

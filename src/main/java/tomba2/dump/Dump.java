package tomba2.dump;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import common.Conf;

public class Dump {
	
	public static void main(String[] args) throws IOException {
		XSSFWorkbook jpbook = new XSSFWorkbook();
		List<NoPointerText> sop = new NoPointerTextReader(0x58, 0x1b8).readJp(Conf.getJpSop());
		new NoPointerTextExporter().export(sop, jpbook, "SOP.BIN");
		new DatJpReader(new DatMultilineExporter(jpbook)).loopScripts();
		List<NoPointerText> mainExe = new NoPointerTextReader(0xF14, 0x57f2).readJp(Conf.getJpExe());//起始地址位于0x800a5180附近,无法看出规律
		mainExe.addAll(new NoPointerTextReader(0x82c4, 0x82d4).readJp(Conf.getJpExe()));
		mainExe.addAll(new NoPointerTextReader(0x8308, 0x830c).readJp(Conf.getJpExe()));
		new NoPointerTextExporter().export(mainExe, jpbook, "MAIN.EXE");
		new SmallJpReader(new SmallExporter(jpbook)).loopScripts();
		saveXls(jpbook, Conf.desktop+"tomba2-jp.xlsx");
		
//		XSSFWorkbook enbook = new XSSFWorkbook();
//		sop = new NoPointerTextReader(0x58, 0x1c5).readEn(Conf.getEnSop());
//		new NoPointerTextExporter().export(sop, enbook, "SOP.BIN");
//		new DatEnReader(new DatMultilineExporter(enbook)).loopScripts();
//		mainExe = new NoPointerTextReader(0xE94, 0x5219).readEn(Conf.getEnExe());
//		new NoPointerTextExporter().export(mainExe, enbook, "MAIN.EXE");
//		new SmallEnReader(new SmallExporter(enbook)).loopScripts();
//		saveXls(enbook, Conf.desktop+"tomba2-en.xlsx");
	}
	
	private static void saveXls(XSSFWorkbook book, String file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		book.write(fos);
		book.close();
		fos.close();
	}
	
	

}

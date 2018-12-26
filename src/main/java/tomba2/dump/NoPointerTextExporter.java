package tomba2.dump;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//for SOP.BIN & MAIN.EXE  
public class NoPointerTextExporter {
	
	public void export(List<NoPointerText> texts, XSSFWorkbook book, String sheetName){
		Sheet sheet = book.createSheet(sheetName);
		Row r=sheet.createRow(0);
		r.createCell(0).setCellValue("地址");
		r.createCell(1).setCellValue("字节数");
		r.createCell(2).setCellValue("日文");
		r.createCell(3).setCellValue("中文");
		int rownum=1;
		for(NoPointerText t:texts){
			Row row=sheet.createRow(rownum++);
			row.createCell(0).setCellValue(String.format("%05X", t.addr));
			row.createCell(1).setCellValue(t.size);
			row.createCell(2).setCellValue(t.text);
		}
	}

}

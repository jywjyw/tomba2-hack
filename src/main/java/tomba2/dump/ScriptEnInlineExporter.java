package tomba2.dump;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ScriptEnInlineExporter implements ScriptEnReader.Callback{
	
	XSSFWorkbook book ;
	XSSFSheet sheet;
	int rowNum=0;
	Row row;
	StringBuilder text = new StringBuilder();
	
	public ScriptEnInlineExporter() {
		this.book = new XSSFWorkbook();
		this.sheet = book.createSheet();
	}
	
	@Override
	public void textStart(String scriptId, String textId) {
		row = sheet.createRow(rowNum++);
		row.createCell(0).setCellValue(scriptId);
		row.createCell(1).setCellValue(textId);
	}
	
	
	@Override
	public void textEnd(String scriptId, String textId) {
		row.createCell(2).setCellValue(text.toString());
		text = new StringBuilder();
	}
	
	@Override
	public void everyByteInText(String char_, int b, boolean isCtrl) {
		text.append(char_);
	}
	
	public void export(String outputFile) throws IOException{
		FileOutputStream fos = new FileOutputStream(outputFile);
		book.write(fos);
		book.close();
		fos.close();
	}
}

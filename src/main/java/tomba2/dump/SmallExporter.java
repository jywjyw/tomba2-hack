package tomba2.dump;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SmallExporter implements DatReaderCallback{
	
	XSSFWorkbook book ;
	XSSFSheet sheet;
	int rowNum=0;
	Row row;
	StringBuilder text = new StringBuilder();
	
	public SmallExporter(XSSFWorkbook book) {
		this.book = book;
		this.sheet = book.createSheet("SMALL");
	}
	
	@Override
	public void sentenceStart(String scriptId, String textId) {
		row = sheet.createRow(rowNum++);
		row.createCell(0).setCellValue(scriptId);
		row.createCell(2).setCellValue(textId);
	}
	
	
	@Override
	public void sentenceEnd(String scriptId, String textId) {
		row.createCell(3).setCellValue(text.toString());
		text = new StringBuilder();
	}
	
	@Override
	public void everyChar(String char_, int unsignedShort, boolean isCtrl) {
		text.append(char_);
	}

	@Override
	public void finalTextSize(String scriptId, int textsize) {
		row.createCell(1).setCellValue(textsize);
	}
	
}

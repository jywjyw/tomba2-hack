package tomba2.dump;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DatInlineExporter implements DatReaderCallback{
	
	XSSFWorkbook book ;
	XSSFSheet sheet;
	int rowNum=0;
	Row row;
	StringBuilder text = new StringBuilder();
	
	public DatInlineExporter(XSSFWorkbook book) {
		this.book = book;
		this.sheet = book.createSheet("TOMBA2.DAT");
		row = sheet.createRow(rowNum++);
		row.createCell(0).setCellValue("起始地址");
		row.createCell(1).setCellValue("文本字节数");
		row.createCell(2).setCellValue("文本编号");
		row.createCell(3).setCellValue("原文");
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

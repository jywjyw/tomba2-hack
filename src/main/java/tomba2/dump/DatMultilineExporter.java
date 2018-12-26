package tomba2.dump;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DatMultilineExporter  implements DatReaderCallback{
	static final boolean LEFT=true,RIGHT=false;
	
	XSSFWorkbook book ;
	XSSFSheet sheet;
//	int rowNum=0;
	Row row;
	boolean lastDirection=LEFT;
	StringBuilder text = new StringBuilder();
	
	XSSFCellStyle colorBg=null;
//	int scriptIndex;
	String scriptId,textId;
	
	public DatMultilineExporter(XSSFWorkbook book) {
		this.book = book;
		this.sheet = book.createSheet("TOMBA2.DAT");
//		colorBg=book.createCellStyle();
//		colorBg.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//		colorBg.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
//		
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("起始地址");
		row.createCell(1).setCellValue("文本字节数");
		row.createCell(2).setCellValue("文本编号");
		row.createCell(3).setCellValue("控制符");
		row.createCell(4).setCellValue("原文");
		row.createCell(5).setCellValue("中文");
	}


	@Override
	public void sentenceStart(String scriptId, String textId) {
		this.scriptId=scriptId;
		this.textId=textId;
		newRow();
	}

	@Override
	public void sentenceEnd(String scriptId, String textId) {
//		newRow();
	}

	@Override
	public void everyChar(String char_, int i, boolean isCtrl) {
		if("{wt}".equals(char_)||"{br}".equals(char_)||"{pz}".equals(char_)) {
			if(lastDirection==RIGHT){
				newRow();
			}
			append(row, 3, char_);
			lastDirection = LEFT;
		} else {
			append(row, 4, char_);
			lastDirection = RIGHT;
		}
	}
	
	private void append(Row row, int cellIndex, String val) {
		Cell cell = row.getCell(cellIndex);
		if(cell==null) cell = row.createCell(cellIndex);
		cell.setCellValue(cell.getStringCellValue()+val);
//		if(scriptIndex%2==0)	cell.setCellStyle(colorBg);
	}
	
	private void newRow(){
		if(row==null) {
			row = sheet.createRow(1);
		} else {
			row = sheet.createRow(row.getRowNum()+1);
		}
//		if(scriptIndex%2==0)	row.setRowStyle(colorBg);
		row.createCell(0).setCellValue(scriptId);
		row.createCell(2).setCellValue(textId);
//		c.setCellValue(scriptIndex);
//		if(scriptIndex%2==0)	c.setCellStyle(colorBg);
	}
	
	@Override
	public void finalTextSize(String scriptId, int textsize) {
		row.createCell(1).setCellValue(textsize);
	}
}

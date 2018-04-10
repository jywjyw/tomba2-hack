package common;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * excel解析类. 和Rowcallback配合使用
 * 
 * @author JingYing 2013-9-17
 */
public class ExcelParser {
	
	private InputStream is;
	private String fileName;
	public Workbook workbook;
	DecimalFormat noDigit = new DecimalFormat("0");
	
	/**
	 * 构造函数1
	 * @param file
	 */
	public ExcelParser(File file){
		try {
			is = new FileInputStream(file);
			this.fileName = file.getName();
			initWorkbook(fileName, is);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 构造函数2
	 * @param is
	 */
	public ExcelParser(InputStream is, String fileName)	{
		this.is = is;
		this.fileName = fileName;
		initWorkbook(fileName, is);
	}
	
	private void initWorkbook(String fileName, InputStream is) {
		try {
			if(fileName.endsWith(".xls")) {
				workbook = new HSSFWorkbook(is);
			} else if(fileName.endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(is);
			} else {
				throw new UnsupportedOperationException("不识别的excel文件:" + fileName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 主方法
	 * @param startRow 从第几行开始解析
	 * @param columnCount 每一行有多少列
	 * @param callback
	 */
	public void parse(int startRow, RowCallback callback)	{
		Sheet read = workbook.getSheetAt(0);
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		int rowNum = startRow-1, cell=0;
		try	{
			while (true) {
				Row row = read.getRow(rowNum);
				if (row == null || row.getCell(0) == null)	{
					break;	//遇到空白行, 则读取完成
				}
				
				List<String> columns = new ArrayList<String>();
				for(int i=0; i <= row.getLastCellNum()-1; i++)	{
					cell=i;
					columns.add(getCellValue(row.getCell(i), evaluator));
				}
				callback.doInRow(columns, rowNum);
				rowNum++;
			}
		} catch(Exception e)	{
			throw new RuntimeException(String.format("第%d行第%d列错误: %s", rowNum+1, cell, e.getMessage()), e);
		} finally	{
			try {
				is.close();
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public byte[] writeReplica(int startRow, RowEditCallback callback) throws IOException {
		Sheet read = workbook.getSheetAt(0);
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		int rowNum = startRow-1;
		while (true) {
			Row row = read.getRow(rowNum);
			if (row == null || row.getCell(0) == null)	{
				break;	//遇到空白行, 则读取完成
			}
			
			List<String> columns = new ArrayList<String>();
			for(int i=0; i <= row.getLastCellNum()-1; i++)	{
				columns.add(getCellValue(row.getCell(i),evaluator));
			}
			callback.doInRow(row, columns, rowNum);
			rowNum++;
		}
		
		is.close();
		ByteArrayOutputStream replica = new ByteArrayOutputStream();
		workbook.write(replica);
		workbook.close();
		return replica.toByteArray();
	}
	
	private String getCellValue(Cell cell, FormulaEvaluator evaluator) {
		if(cell==null) 
			return null;
		else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			double val = cell.getNumericCellValue();
			if ((val == Math.floor(val)) && !Double.isInfinite(val)) {
				return noDigit.format(val);
			} else {
				return val+"";
			}
		} else if(cell.getCellType()==Cell.CELL_TYPE_FORMULA){
			return evaluator.evaluate(cell).getStringValue();
		} else {
			return cell.getStringCellValue();
		}
	}
	
	
	
	public interface RowCallback {
		
		/**
		 * 读取每一行后, 回调 
		 * @param strs 该行中的每一列
		 * @param rowNum 行号 1开始
		 * @return
		 */
		void doInRow(List<String> strs, int rowNum);
		
	}
	public interface RowEditCallback {
		
		void doInRow(Row row, List<String> strs, int rowNum);
		
	}

}

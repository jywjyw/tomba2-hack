package tomba2.hack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import common.ExcelParser;
import common.ExcelParser.RowCallback;
import tomba2.EncodingLarge;
import common.Util;

public class ExeLargeFontImporter {
	
	EncodingLarge encLarge;
	public ExeLargeFontImporter(EncodingLarge table){
		this.encLarge=table;
	}
	
	StringBuilder err = new StringBuilder();
	
	public void import_(File excel, RandomAccessFile exe) throws IOException{
		new ExcelParser(excel).parse("MAIN.EXE", 2, new RowCallback() {
			@Override
			public void doInRow(List<String> strs, int rowNum) {
				if(getCell(strs, 4).equals("1") || getCell(strs, 4).equals("3")){ //大小字库共用的文本都在这里处
					String translation=getCell(strs,3);
					if(Util.isNotEmpty(translation)){
						byte[] bs = toBytes(translation);
						if(bs.length>Integer.parseInt(strs.get(1))){
							err.append(String.format("%s文件第%d行超长,已达到%d字节: %s", excel.getName(), rowNum,bs.length,strs.get(3))).append("\n");
							return;
						}
						long addr=Long.parseLong(strs.get(0),16);
						try {
							exe.seek(addr);
							exe.write(bs);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		});
		if(err.length()>0) {
			System.err.println(err);
			throw new RuntimeException();
		}
	}
	
	byte[] toBytes(String text) {
		ByteBuffer ret = ByteBuffer.allocate(5000);
		ret.order(ByteOrder.BIG_ENDIAN);	//SOP的字节序特殊
		for(String s:TranslateTextReader.readGBK(text)){
			Integer code=encLarge.get(s);
			if(code==null){
				if(s.startsWith("{")&&s.endsWith("}")) {
					code= Integer.parseInt(s.substring(1, s.length()-1), 16);
				}else{
					code=encLarge.put(s);
				}
			}
			ret.putShort(code.shortValue());
		}
		ret.put((byte) 0);	//结束符为00
		return Arrays.copyOfRange(ret.array(), 0, ret.capacity()-ret.remaining());
	}
	
	private String getCell(List<String> strs,int i){
		if(strs.size()>i) return strs.get(i);
		return null;
	}

}

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

public class SOPImporter {
	
	EncodingLarge table;
	public SOPImporter(EncodingLarge table){
		this.table=table;
	}
	
	public void import_(File excel, String binFile) throws IOException{
		RandomAccessFile bin = new RandomAccessFile(new File(binFile), "rw");
		new ExcelParser(excel).parse("SOP.BIN", 2, new RowCallback() {
			@Override
			public void doInRow(List<String> strs, int rowNum) {
				byte[] bs = toBytes(strs.get(3));
				if(bs.length>Integer.parseInt(strs.get(1))){
					throw new RuntimeException(String.format("%s文件第%d行超长,已达到%d字节: %s", excel.getName(), rowNum,bs.length,strs.get(3)));
				}
				long addr=Long.parseLong(strs.get(0),16);
				try {
					bin.seek(addr);
					bin.write(bs);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		bin.close();
	}
	
	byte[] toBytes(String text) {
		ByteBuffer ret = ByteBuffer.allocate(5000);
		ret.order(ByteOrder.BIG_ENDIAN);	//SOP的字节序特殊
		for(String s:TranslateTextReader.readGBK(text)){
			Integer code=table.get(s);
			if(code==null){
				if(s.startsWith("{")&&s.endsWith("}")) {
					code= Integer.parseInt(s.substring(1, s.length()-1), 16);
				}else{
					code=table.put(s);
				}
			}
			ret.putShort(code.shortValue());
		}
		ret.put((byte)0);	//结束符为0
		return Arrays.copyOfRange(ret.array(), 0, ret.capacity()-ret.remaining());
	}

}

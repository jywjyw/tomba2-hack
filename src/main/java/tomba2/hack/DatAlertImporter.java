package tomba2.hack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.ExcelParser;
import common.ExcelParser.RowCallback;
import common.Util;
import tomba2.EncodingMenu;

public class DatAlertImporter {
	
	EncodingMenu enc;
	private List<byte[]> sentences = new ArrayList<>();
	int textSizeLimit=0;
	
	public DatAlertImporter(EncodingMenu enc) {
		this.enc=enc;
	}

	public void import_(File excel, String datFile) throws IOException{
		RandomAccessFile dat = new RandomAccessFile(new File(datFile), "rw");
		new ExcelParser(excel).parse("SMALL", 1, new RowCallback() {
			@Override
			public void doInRow(List<String> strs, int rowNum) {
				String translation=getCell(strs,4);
				if(translation==null) translation="";
				sentences.add(sentenceToBytes(translation));
				if(Util.isNotEmpty(getCell(strs, 1))){
					textSizeLimit=Integer.parseInt(getCell(strs, 1));
				}
			}
		});
		
		dat.seek(0x8374);
		byte[] unk=new byte[2];
		dat.read(unk);
		dat.seek(dat.getFilePointer()-2);
		byte[] textarea=buildPointerText(unk);
		dat.write(textarea);
		dat.close();
	}
	
	byte[] sentenceToBytes(String sentence) {
		ByteBuffer ret = ByteBuffer.allocate(5000);
		ret.order(ByteOrder.LITTLE_ENDIAN);
		for(String s:TranslateTextReader.readGBK(sentence)){
			Integer code=enc.get(s);
			if(code==null){
				if(s.startsWith("{")&&s.endsWith("}")) {
					code= Integer.parseInt(s.substring(1, s.length()-1), 16);
				}else{
					code=enc.put(s);
				}
			}
			ret.putShort(code.shortValue());
		}
		ret.putShort((short) 0xffff);	
		return Arrays.copyOfRange(ret.array(), 0, ret.capacity()-ret.remaining());
	}
	
	private byte[] buildPointerText(byte[] unk2){
		int textSize=0;
		for(byte[] sent:sentences){
			textSize+=sent.length;
		}
		if(textSize>textSizeLimit) 
			throw new RuntimeException("small文字超长,已达到"+textSize);
		int headBytes = Util.getMultiple(16+sentences.size()*2, 16);
		int textBytes = Util.getMultiple(textSize, 16);
		ByteBuffer buf=ByteBuffer.allocate(headBytes+textBytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.put(unk2);
		buf.putShort((short)((sentences.size()+1)&0xffff));
		buf.put(new byte[12]);//定位到第1个指针处
		int pos=0;
		for(byte[] sent:sentences){
			buf.putShort((short)pos);
			pos+=sent.length/2;
		}
		byte[] ff=new byte[headBytes-buf.position()];
		if(ff.length>0){
			Arrays.fill(ff, (byte)0xff);
			buf.put(ff);
		}
		for(byte[] sent:sentences){
			buf.put(sent);
		}
		return buf.array();
	}
	
	private String getCell(List<String> strs,int i){
		if(strs.size()>i) return strs.get(i);
		return null;
	}

}

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
import tomba2.EncodingLarge;
import tomba2.TextSector;
import tomba2.TextSectorLoader;

public class DatMultilineImporter {
	
	EncodingLarge enc;
	TextSectorLoader sectors=TextSectorLoader.loadJp();
	public DatMultilineImporter(EncodingLarge enc){
		this.enc=enc;
	}
	
	private String lastScriptId;
	private Integer lastPointer;
	private Integer lastScriptSize;
	private List<byte[]> perScript = new ArrayList<>();
	private StringBuilder textPerPointer = new StringBuilder();
	
	
	public void import_(File excel, String datFile) throws IOException{
		RandomAccessFile dat = new RandomAccessFile(new File(datFile), "rw");
		new ExcelParser(excel).parse("TOMBA2.DAT", 2, new RowCallback() {
			@Override
			public void doInRow(List<String> strs, int rowNum) {
				if("".equals(strs.get(0))) return;
				
				String thisScriptId = strs.get(0);
				int thisPointer = Integer.parseInt(strs.get(2));
				if(lastScriptId!=null && !thisScriptId.equals(lastScriptId)) {
					perScript.add(toBytes(textPerPointer.toString()));
					writeToDat(dat,lastScriptId, lastScriptSize);
					textPerPointer = new StringBuilder();
					perScript = new ArrayList<>();
				} else if(lastPointer!=null && lastPointer!=thisPointer) {
					perScript.add(toBytes(textPerPointer.toString()));
					textPerPointer = new StringBuilder();
				} 
				
				String s3="";
				if(strs.size()>3 && strs.get(3)!=null) {
					s3 = strs.get(3);
				}
				String s5="";
				if(strs.size()>5 && strs.get(5)!=null) {
					s5 = strs.get(5);
				}
				textPerPointer.append(s3).append(s5);
				
				lastScriptId = thisScriptId;
				lastPointer = thisPointer;
				if(Util.isNotEmpty(strs.get(1)))	lastScriptSize =Integer.parseInt(strs.get(1)); 
			}
		});
		writeToDat(dat,lastScriptId, lastScriptSize);
		dat.close();
	}


	protected byte[] toBytes(String text) {
		ByteBuffer ret = ByteBuffer.allocate(5000);
		ret.order(ByteOrder.LITTLE_ENDIAN);
		for(String s:TranslateTextReader.readGBK(text)){
			Integer code=enc.get(s);
			if(code==null){
				if(s.startsWith("{")&&s.endsWith("}")) {
					code= Integer.parseInt(s.substring(1,5), 16);
				}else{
					code=enc.put(s);
				}
			}
			ret.putShort(code.shortValue());
		}
		ret.putShort((short) 0xFFFF);
		return Arrays.copyOfRange(ret.array(), 0, ret.capacity()-ret.remaining());
	}


	protected void writeToDat(RandomAccessFile dat, String scriptId, int sizeLimit)  {
		for(byte[] bs:perScript){
			sizeLimit-=bs.length;
		}
		if(sizeLimit<0) throw new RuntimeException(scriptId+"文本超出"+sizeLimit+"个字节");
		
		int headerStartAddr=Integer.parseInt(scriptId,16);
		for(TextSector sec:sectors) {
			if(sec.addr==headerStartAddr) {
				sec.sameTextAddr.add(sec.addr);
				for(int addr:sec.sameTextAddr) {
					try{
						dat.seek(addr);
						dat.skipBytes(2);	//前2个字节意义不明
						int pointerCount = Util.hiloShort(dat.readUnsignedShort())-1;
						dat.skipBytes(12);	//定位到第1个指针处
						dat.skipBytes(4);//跳过第1组指针
						int total=0;
						for(int i=0;i<perScript.size()-1;i++){
							int size=perScript.get(i).length;
							total+=size;
							dat.writeShort(Util.hiloShort(total/2));
							dat.skipBytes(2);
						}
						
						int headBytes = 16+pointerCount*4+2;	//size - pointers - ffff
						headBytes = Util.getMultiple(headBytes, 16);
						dat.seek(addr+headBytes); //text start addr
						for(byte[] bs:perScript){
							dat.write(bs);
						}
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				}
				break;
			}
		}
		
	}

}

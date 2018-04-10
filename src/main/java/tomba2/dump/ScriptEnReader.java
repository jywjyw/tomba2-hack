package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import common.Conf;
import common.Util;
import tomba2.CharTable;
import tomba2.TextAddr;

public class ScriptEnReader {
	
	public Callback callback;
	
	public void loopScripts() throws IOException {
		RandomAccessFile dat = new RandomAccessFile(Conf.enDat, "r");
		CharTable charTable = CharTable.loadEn();
		TextAddr addrs = TextAddr.loadEn();
		for(Entry<Integer,Integer> e: addrs){
			dat.seek(e.getKey());
//			callback.newScript(addrStr(e.getKey()));
			read(dat, charTable, e.getKey());
		}
		dat.close();
	}
	
	private String addrStr(long addr){
		return String.format("%08X", addr);
	}
	
	public void read(RandomAccessFile dat, CharTable charTable, long scriptStartAddr) throws IOException {
		//前16个字节代表指针个数。
		dat.skipBytes(2);
		int pointerCount = Util.hiloShort(dat.readUnsignedShort())-1;
		dat.skipBytes(12);	//定位到第1个指针处
		List<Integer> pointers = new ArrayList<>();
		for(int i=0;i<pointerCount;i++){
			pointers.add(Util.hiloShort(dat.readUnsignedShort()));
			dat.skipBytes(2);
		}
		if(0xffff != dat.readUnsignedShort()){
			throw new RuntimeException();
		}
		int headBytes = 16+pointerCount*4+2;	//size + pointers + ffff
		headBytes = Util.getMultiple(headBytes, 16);
		long textStart = scriptStartAddr+headBytes; 
		
		for(int i=0;i<pointers.size();i++){
			callback.textStart(addrStr(scriptStartAddr), i+"");
			dat.seek(textStart+pointers.get(i));
			int buf=0;
			while(true){
				buf = dat.readUnsignedByte();
				boolean isCtrl = buf>>>4==0xF;
				String char_ = charTable.getChar(buf);
				if(char_==null) 
					char_ = String.format("{%02X}", buf);
				callback.everyByteInText(char_, buf, isCtrl);
				if(buf==0xFF) break;
			}
			callback.textEnd(addrStr(scriptStartAddr), i+"");
		}
		
	}
	
	public interface Callback {
		void everyByteInText(String char_, int b, boolean isCtrl);
		void textStart(String scriptId, String textId);
		void textEnd(String scriptId, String textId);
	}

}

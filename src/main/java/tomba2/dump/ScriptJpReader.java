package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import common.Conf;
import common.Util;
import tomba2.CharTable;
import tomba2.TextAddr;

public class ScriptJpReader {
	
	public Callback callback;
	private TreeMap<Integer,String> allChars = new TreeMap<>();
	
	public void loopScripts() throws IOException {
		RandomAccessFile dat = new RandomAccessFile(Conf.jpDat, "r");
		CharTable charTable = CharTable.loadJp();
		TextAddr addrs = TextAddr.loadJp();
		for(Entry<Integer,Integer> e: addrs){
			dat.seek(e.getKey());
//			callback.newScript(addrStr(e.getKey()));
			read(dat, charTable, e.getKey());
		}
		dat.close();
		
		for(Entry<Integer,String> e:allChars.entrySet()){
			System.out.printf("%04X=%s\n", e.getKey(),e.getValue());
		}
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
		int headBytes = 16+pointerCount*4+2;	//size - pointers - ffff
		headBytes = Util.getMultiple(headBytes, 16);
		long textStart = scriptStartAddr+headBytes; 
		
		for(int i=0;i<pointers.size();i++){
			callback.textStart(addrStr(scriptStartAddr), i+"");
			int offset = pointers.get(i)*2;
			dat.seek(textStart+offset);
			int buf=0;
			while(true){
				buf = dat.readUnsignedShort();
				boolean isCtrl = (buf&0xff)==0xff || (buf&0xff)==0;	//如果是FFxx或00XX代表控制符
				buf = Util.hiloShort(buf);
				String char_ = charTable.getChar(buf);
				if(char_==null) 
					char_ = String.format("{%04X}", buf);
				callback.every2BytesInText(char_, buf, isCtrl);
				allChars.put(buf, char_);
				if(buf==0xffff) break;
			}
			callback.textEnd(addrStr(scriptStartAddr), i+"");
		}
		
	}
	
	public interface Callback {
		
		/**
		 * @param index 这2个字节是第几次,例如,前2个字节的index=0
		 * @param char_
		 * @param unsignedShort
		 * @param isCtrl
		 */
		void every2BytesInText(String char_, int unsignedShort, boolean isCtrl);
		
		void textStart(String scriptId, String textId);
		void textEnd(String scriptId, String textId);
	}

}

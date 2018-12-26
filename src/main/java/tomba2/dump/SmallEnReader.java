package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import common.Charset;
import common.Conf;
import common.Util;

public class SmallEnReader {
	
	public DatReaderCallback callback;
	
	public SmallEnReader(DatReaderCallback callback) {
		this.callback = callback;
	}

	public void loopScripts() throws IOException {
		RandomAccessFile dat = new RandomAccessFile(Conf.getEnDat(), "r");
		Charset charTable = Charset.loadEn();
		Map<Integer,Integer> addr_size = new LinkedHashMap<>();
		addr_size.put(0x81d4, 0);
		for(Entry<Integer,Integer> e: addr_size.entrySet()){
			read(dat, charTable, e.getKey());
		}
		dat.close();
	}
	
	private String addrStr(long addr){
		return String.format("%08X", addr);
	}
	
	public void read(RandomAccessFile dat, Charset charTable, long headerStartAddr) throws IOException {
		TextPointers tps=TextPointers.read2(dat, headerStartAddr);
		
		for(int i=0;i<tps.pointers.size();i++){
			callback.sentenceStart(addrStr(headerStartAddr), i+"");
			dat.seek(tps.textStartAddr+tps.pointers.get(i));
			int buf=0;
			while(true){
				buf = dat.readUnsignedByte();
				boolean isCtrl = buf>>>4==0xF;
				String char_ = charTable.getChar(buf);
				if(char_==null) 
					char_ = String.format("{%02X}", buf);
				callback.everyChar(char_, buf, isCtrl);
				if(buf==0xFF) break;
			}
			callback.sentenceEnd(addrStr(headerStartAddr), i+"");
		}
	}
}

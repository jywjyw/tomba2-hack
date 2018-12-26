package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;

import common.Charset;
import common.Conf;
import tomba2.TextSector;
import tomba2.TextSectorLoader;

public class DatEnReader {
	
	public DatReaderCallback callback;
	
	public DatEnReader(DatReaderCallback callback) {
		this.callback = callback;
	}

	public void loopScripts() throws IOException {
		RandomAccessFile dat = new RandomAccessFile(Conf.getEnDat(), "r");
		Charset charTable = Charset.loadEn();
		for(TextSector t: TextSectorLoader.loadEn()){
			read(dat, charTable, t);
		}
		dat.close();
	}
	
	private String addrStr(long addr){
		return String.format("%08X", addr);
	}
	
	public void read(RandomAccessFile dat, Charset charTable, TextSector ts) throws IOException {
		TextPointers tps=TextPointers.read4(dat, ts.addr);
		for(int i=0;i<tps.pointers.size();i++){
			dat.seek(tps.textStartAddr+tps.pointers.get(i));
			callback.sentenceStart(addrStr(ts.addr), i+"");
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
			callback.sentenceEnd(addrStr(ts.addr), i+"");
		}
		
	}
}

package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;

import common.Charset;
import common.Conf;
import common.Util;
import tomba2.TextSector;
import tomba2.TextSectorLoader;

public class DatJpReader {
	public static void main(String[] args) {
		byte b=(byte)0xf8;
		System.out.println((b&0xff)>>>3);
	}
	
	DatReaderCallback callback;
	
	public DatJpReader(DatReaderCallback callback) {
		this.callback = callback;
	}

	public void loopScripts() throws IOException {
		RandomAccessFile dat = new RandomAccessFile(Conf.getJpDat(), "r");
		Charset charTable = Charset.loadJp();
		for(TextSector t: TextSectorLoader.loadJp()){
			read(dat, charTable, t);
		}
		dat.close();
	}
	
	private String addrStr(long addr){
		return String.format("%08X", addr);
	}
	
	public void read(RandomAccessFile dat, Charset charTable, TextSector ts) throws IOException {
		TextPointers tps=TextPointers.read4(dat, ts.addr);
		int textsize=0;
		for(int i=0;i<tps.pointers.size();i++){
			int offset = tps.pointers.get(i)*2;	
			dat.seek(tps.textStartAddr+offset);
			callback.sentenceStart(addrStr(ts.addr), i+"");
			int buf=0;
			while(true){
				buf=dat.readUnsignedShort();
				textsize+=2;
				if(buf==0xffff) break;
				boolean isCtrl = (buf&0xff)==0xff || (buf&0xff)==0;	//如果是FFxx或00XX代表控制符
				buf = Util.hiloShort(buf);
				String char_ = charTable.getChar(buf);
				if(char_==null) char_ = String.format("{%04X}", buf);
				callback.everyChar(char_, buf, isCtrl);
//				allChars.put(buf, char_);
			}
			callback.sentenceEnd(addrStr(ts.addr), i+"");
		}
		callback.finalTextSize(addrStr(ts.addr), Util.getMultiple(textsize, 16));
	}

}

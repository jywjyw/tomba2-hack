package tomba2.dump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import common.Charset;
import common.Conf;
import common.Util;

public class SmallJpReader {
	
	public static void main(String[] args) throws IOException {
		XSSFWorkbook jpbook = new XSSFWorkbook();
		SmallJpReader reader=new SmallJpReader(new SmallExporter(jpbook));
		
		RandomAccessFile dat = new RandomAccessFile(Conf.outdir+"TOMBA2.DAT", "r");
		Charset charset = Charset.load(new File(Conf.desktop+"新菜单码表.tbl"));
		Map<Integer,Integer> addr_size = new LinkedHashMap<>();
		addr_size.put(0x8374, 0);
		for(Entry<Integer,Integer> e: addr_size.entrySet()){
			reader.read(dat, charset, e.getKey());
		}
		dat.close();
		
		FileOutputStream fos = new FileOutputStream(Conf.desktop+"small.xlsx");
		jpbook.write(fos);
		jpbook.close();
		fos.close();
	}
	
	DatReaderCallback callback;
	
	public SmallJpReader(DatReaderCallback callback) {
		this.callback = callback;
	}

	public void loopScripts() throws IOException {
		RandomAccessFile dat = new RandomAccessFile(Conf.getJpDat(), "r");
		Charset charset = Charset.loadSmallJp();
		Map<Integer,Integer> addr_size = new LinkedHashMap<>();
		addr_size.put(0x8374, 0);
		for(Entry<Integer,Integer> e: addr_size.entrySet()){
			read(dat, charset, e.getKey());
		}
		dat.close();
	}
	
	private String addrStr(long addr){
		return String.format("%08X", addr);
	}
	
	public void read(RandomAccessFile dat, Charset charTable, long headerStartAddr) throws IOException {
		TextPointers tps=TextPointers.read2(dat, headerStartAddr);
		int textsize=0;
		for(int i=0;i<tps.pointers.size();i++){
			int offset = tps.pointers.get(i)*2;
			dat.seek(tps.textStartAddr+offset);
			callback.sentenceStart(addrStr(headerStartAddr), i+"");
			int buf=0;
			while(true){
				buf = dat.readUnsignedShort();
				textsize+=2;
				boolean isCtrl = (buf&0xff)==0xff || (buf&0xff)==0;	//如果是FFxx或00XX代表控制符
				buf = Util.hiloShort(buf);
				String char_ = charTable.getChar(buf);
				if(char_==null) 
					char_ = String.format("{%04X}", buf);
				callback.everyChar(char_, buf, isCtrl);
				if(buf==0xffff) break;
			}
			callback.sentenceEnd(addrStr(headerStartAddr), i+"");
		}
		callback.finalTextSize(addrStr(headerStartAddr), Util.getMultiple(textsize, 16));
		
	}

}

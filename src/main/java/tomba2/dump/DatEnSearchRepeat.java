package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.Conf;
import tomba2.TextSector;
import tomba2.TextSectorLoader;

public class DatEnSearchRepeat {
	
	public static void main(String[] args) throws IOException {
		new DatEnSearchRepeat().loopScripts();
	}
	
	List<TextSector> filteredSectors = new ArrayList<>();
	
	public void loopScripts() throws IOException {
		RandomAccessFile dat = new RandomAccessFile(Conf.getEnDat(), "r");
		for(TextSector t: TextSectorLoader.loadEn()){
			read(dat, t);
		}
		dat.close();
		printRepeatTextSector();
	}
	
	private String addrStr(long addr){
		return String.format("%X", addr);
	}
	
	private void printRepeatTextSector(){
		for(TextSector _t:filteredSectors){
			StringBuilder line=new StringBuilder();
			line.append(addrStr(_t.addr));
			for(int i:_t.sameTextAddr){
				line.append(",");
				line.append(addrStr(i));
			}
			System.out.println(line.toString());
		}
	}
	
	public void read(RandomAccessFile dat, TextSector textSector) throws IOException {
		TextPointers tps=TextPointers.read4(dat, textSector.addr);
		MessageDigest md=null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		for(int i=0;i<tps.pointers.size();i++){
			int offset = tps.pointers.get(i)*2;	
			dat.seek(tps.textStartAddr+offset);
			byte[] buf=new byte[1];
			while(true){
				dat.read(buf);
				md.update(buf);
				if(buf[0]==(byte)0xff) 
					break;
			}
		}
		
		byte[] md5=md.digest();
		boolean hasSame=false;
		for(TextSector _t:filteredSectors){
			if(Arrays.equals(md5, _t.md5)){
				_t.sameTextAddr.add(textSector.addr);
				hasSame=true;
			}
		}
		if(!hasSame){
			TextSector _t=new TextSector();
			_t.addr=textSector.addr;
			_t.md5=md5;
			filteredSectors.add(_t);
		}
	}

}

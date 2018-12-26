package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import common.Charset;
public class NoPointerTextReader {
	
	private long thisAddr,endAddr;
	private StringBuilder sentence = new StringBuilder();
	private boolean prevIsEnd;
	private Integer lastBuf=null;
	private int sentenceSize=0;	//这段文本占多少字节
	
	public NoPointerTextReader(long startAddr, long endAddr){
		this.thisAddr=startAddr;
		this.endAddr=endAddr;
	}
	
	//SHIFT-JIS编码
	public List<NoPointerText> readJp(String file) throws IOException{
		Charset charTable = Charset.loadJp();
		List<NoPointerText> ret = new ArrayList<>();
		RandomAccessFile exe = new RandomAccessFile(file, "r");
		exe.seek(thisAddr);
		byte[] buf=new byte[2];
		while(exe.getFilePointer()<endAddr){
			exe.read(buf, 0, 1);
			if(!isEndFlag(buf[0])) {
				if(prevIsEnd) {
					ret.add(new NoPointerText(thisAddr, sentenceSize, sentence.toString()));
					thisAddr = exe.getFilePointer()-1;
					sentence = new StringBuilder();
					sentenceSize=0;
				}
				exe.read(buf, 1, 1);
				int code=(buf[0]&0xff)<<8|(buf[1]&0xff);
				sentenceSize+=2;
				String char_ = charTable.getChar(code);
				if(char_==null) char_ = String.format("{%04X}", code);
				sentence.append(char_);
				prevIsEnd=false;
			} else {
				prevIsEnd=true;
				sentenceSize++;
			}
		}
		ret.add(new NoPointerText(thisAddr, sentenceSize, sentence.toString()));
		exe.close();
		return ret;
	}
	
	private boolean isEndFlag(byte i) {
		return i==0||i==1;
	}
	
	//ASCII编码
	public List<NoPointerText> readEn(String file) throws IOException{
		RandomAccessFile exe = new RandomAccessFile(file, "r");
		List<NoPointerText> ret = new ArrayList<>();
		exe.seek(thisAddr);
		byte buf=0;
		while(exe.getFilePointer()<endAddr){
			buf = exe.readByte();
			sentenceSize+=1;
			if(buf==0){
				if(lastBuf!=0){
					ret.add(new NoPointerText(thisAddr, sentenceSize, sentence.toString()));
					thisAddr = exe.getFilePointer();
					sentence = new StringBuilder();
					sentenceSize=0;
				}
			} else {
				sentence.append((char)buf);
			}
			lastBuf = (int)buf;
		}
		ret.add(new NoPointerText(thisAddr, sentenceSize, sentence.toString()));
		exe.close();
		return ret;
	}
	
}

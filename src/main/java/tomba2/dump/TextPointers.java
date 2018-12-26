package tomba2.dump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import common.Util;

public class TextPointers {
	
	public List<Integer> pointers;
	public long textStartAddr;
	
	public static TextPointers read2(RandomAccessFile dat, long headerStartAddr) throws IOException{
		return read(dat, headerStartAddr, 2);
	}

	public static TextPointers read4(RandomAccessFile dat, long headerStartAddr) throws IOException{
		 return read(dat, headerStartAddr, 4);
	}
	
	//每个指针占4个或2个字节
	private static TextPointers read(RandomAccessFile dat, long headerStartAddr, int sizePerPointer) throws IOException{
		dat.seek(headerStartAddr);
		//指针头共16字节
		dat.skipBytes(2);	//前2个字节意义不明
		int pointerCount = Util.hiloShort(dat.readUnsignedShort())-1;
		dat.skipBytes(12);	//定位到第1个指针处
		List<Integer> pointers = new ArrayList<>();
		for(int i=0;i<pointerCount;i++){
			pointers.add(Util.hiloShort(dat.readUnsignedShort()));
			dat.skipBytes(sizePerPointer-2);
		}
		
		if(0xffff != dat.readUnsignedShort())	throw new RuntimeException();
		
		int headBytes = 16+pointerCount*sizePerPointer+2;	//size - pointers - ffff
		headBytes = Util.getMultiple(headBytes, 16);
		
		TextPointers ret = new TextPointers();
		ret.pointers = pointers;
		ret.textStartAddr=headerStartAddr+headBytes;
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pointers == null) ? 0 : pointers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextPointers other = (TextPointers) obj;
		if (pointers == null) {
			if (other.pointers != null)
				return false;
		} else if (!pointers.equals(other.pointers))
			return false;
		return true;
	}
}

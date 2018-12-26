package common;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class VramImg {
	public int w,h;	//original w&h in video ram
	public byte[] data;

	public VramImg(int w, int h, byte[] data) {
		this.w = w;
		this.h = h;
		this.data = data;
	}
	
	public static class VramImg4bitWriter{
		ByteBuffer data;
		private int w,h;
		public VramImg4bitWriter(int realW, int realH){
			if(realW%4!=0) throw new UnsupportedOperationException("width must be mutilply of 4");
			this.w=realW/4;
			this.h=realH;
			data = ByteBuffer.allocate(this.w*this.h*2);
		}
		
		byte buf=0;
		boolean bufFlag=true;
		public void addPixelIndex(int i){
			if(bufFlag){
				buf=(byte)i;
			} else {
				buf|=i<<4;
				data.put(buf);
			}
			bufFlag=!bufFlag;
		}
		
		public VramImg build(){
			return new VramImg(w, h, data.array());
		}
	}
	
	
	public Iterator<Byte> get4bitIterator(){
		return new Iterator4bit();
	}
	
	private class Iterator4bit implements Iterator<Byte>{
		int cursor=0;
		@Override
		public boolean hasNext() {
			return cursor<data.length*2;
		}

		@Override
		public Byte next() {
			byte b=data[cursor/2];
			if((cursor++)%2==0){
				return (byte)(b&0xf);
			} else {
				return (byte)(b>>>4);
			}
		}
		
	}
}

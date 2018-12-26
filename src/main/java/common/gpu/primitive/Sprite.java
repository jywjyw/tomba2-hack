package common.gpu.primitive;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import common.Util;

public class Sprite implements GpuCmd {
	public static void main(String[] args) {
		Sprite s = new Sprite(Util.decodeHex("80808064 93ff3500 28285640 08000800"));
		System.out.println(s);
		System.out.println(Util.hexEncode(s.toBytes()));
	}

	public static Sprite read(RandomAccessFile file) throws IOException {
		byte[] buf=new byte[SIZE];
		file.read(buf);
		return new Sprite(buf);
	}
	
	public static final int SIZE = 16;
	public static final byte CMD = 0x64;
	
	public byte r,g,b;
	public short x,y; //screenX, screenY. upper left corner location
	public byte u,v;	//texture coordinates page y,x
	public ClutId clutId;
	public short w,h;
	
	public Sprite(byte[] bs){
		ByteBuffer is = ByteBuffer.wrap(bs);
		is.order(ByteOrder.LITTLE_ENDIAN);
		this.r=is.get();
		this.g=is.get();
		this.b=is.get();
		if(CMD!=is.get()) throw new UnsupportedOperationException();
		this.x=is.getShort();
		this.y=is.getShort();
		this.u=is.get();
		this.v=is.get();
		this.clutId=new ClutId(is.getShort());
		this.w=is.getShort();
		this.h=is.getShort();
	}
	
	public byte[] toBytes(){
		ByteBuffer buf = ByteBuffer.allocate(SIZE);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.put((byte)r);
		buf.put((byte)g);
		buf.put((byte)b);
		buf.put(CMD);
		buf.putShort(x);
		buf.putShort(y);
		buf.put(u);
		buf.put(v);
		buf.put(clutId.toBytes());
		buf.putShort(w);
		buf.putShort(h);
		return buf.array();
	}

	@Override
	public String toString() {
		return String.format("sprite (%d, %d)*(%d, %d) clut(%d, %d), UV(%d, %d) RGB(%x, %x, %x)",
				x,y,w,h,clutId.x,clutId.y,u,v,r,g,b);
	}
}

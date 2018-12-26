package common.gpu.primitive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import common.Util;

public class Sprite8x8 implements GpuCmd {
	public static void main(String[] args) {
		//0D5D74:75 - sprite 8*8 (70, 158) clut(1008, 496), UV(64, 64) RGB(3E,  0, 9C)
		Sprite8x8 s = new Sprite8x8(Util.decodeHex("3E009C75 46009E00 40403F7C"));
		System.out.println(s);
		System.out.println(Util.hexEncode(s.toBytes()));
	}

	public static final int SIZE = 12;
	public static final byte CMD = 0x75;	//TODO 0x75 in agemo's debugger, but 0x74 in psx.pdf 
	
	public byte r,g,b;
	public short x,y; //screenX, screenY. upper left corner location
	public byte u,v;	//texture coordinates page y,x
	public ClutId clutId;
	
	public Sprite8x8(byte[] bs){
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
		return buf.array();
	}

	@Override
	public String toString() {
		return String.format("sprite 8*8 (%d, %d) clut(%d, %d), UV(%d, %d) RGB(%x, %x, %x)",
				x,y,clutId.x,clutId.y,u,v,r,g,b);
	}
}

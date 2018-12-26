package common.gpu.primitive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import common.Util;

//TODO 验证失败,因为 asm.log和psx.pdf不一致
public class FT4 implements GpuCmd {
	public static void main(String[] args) {
		//FT4 (82, 216)*(192, 16) clut(992, 22) TP(320, 256)(bit:0) UV<(374, 448)*(16, 16)>(216, 192)*(16, 16) RGB(80, 80, 80)
		byte[] src = Util.decodeHex("8080802C 5200D800 D8C0BE05 1201D800 E8C01500 5200E800 D8D0D800 1201E800 E8D0E800");
		FT4 s = new FT4(src);
		System.out.println(s);
		System.out.println(Util.hexEncode(s.toBytes()));
		System.out.println(Arrays.equals(src, s.toBytes()));
	}
	
	@Override
	public String toString() {
		int tpOffsetX = tpage.getTx() + u0;
		if(tpage.getBit()==0) {
			tpOffsetX = tpage.getTx() + u0/4; 
		} else if(tpage.getBit()==1) {
			tpOffsetX = tpage.getTx() + u0/2;
		}
		return String.format("FT4 (%d, %d)*(?, ?) clut(%d, %d) TP(%d, %d)(bit:%d) UV<(%d, %d)*(?, ?)>(%d, %d)*(?, ?) RGB(%x, %x, %x)",
				x0,y0,clutId.x,clutId.y, tpage.getTx(), tpage.getTy(), tpage.getBit(), 
				tpOffsetX, tpage.getTy()+v0,
				u0,v0,r,g,b);
	}

	public static final int SIZE = 36;
	public static final byte CMD = 0x2C;
	
	public byte r,g,b;
	public int x0,y0; //screenX, screenY. upper left corner location
	public int u0,v0;	//texture coordinates page y,x
	public ClutId clutId;
	public int x1,y1;
	public int u1,v1;
	public Tpage tpage;
	public int x2,y2,u2,v2,x3,y3,u3,v3;
	
	public FT4(byte[] bs){
		ByteBuffer is = ByteBuffer.wrap(bs);
		is.order(ByteOrder.LITTLE_ENDIAN);
		this.r=is.get();
		this.g=is.get();
		this.b=is.get();
		if(CMD!=is.get()) throw new UnsupportedOperationException();
		this.x0=is.getShort();
		this.y0=is.getShort();
		this.u0=is.get()&0xff;
		this.v0=is.get()&0xff;
		this.clutId=new ClutId(is.getShort());
		this.x1=is.getShort();
		this.y1=is.getShort();
		this.u1=is.get()&0xff;
		this.v1=is.get()&0xff;
		byte[] tpagebuf=new byte[Tpage.SIZE];
		is.get(tpagebuf);
		this.tpage=new Tpage(tpagebuf);
		this.x2=is.getShort();
		this.y2=is.getShort();
		this.u2=is.get()&0xff;
		this.v2=is.get()&0xff;
		is.getShort();
		this.x3=is.getShort();
		this.y3=is.getShort();
		this.u3=is.get()&0xff;
		this.v3=is.get()&0xff;
	}
	
	public byte[] toBytes(){
		ByteBuffer buf = ByteBuffer.allocate(SIZE);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.put((byte)r);
		buf.put((byte)g);
		buf.put((byte)b);
		buf.put(CMD);
		buf.putShort((short)x0);
		buf.putShort((short)y0);
		buf.put((byte)u0);
		buf.put((byte)v0);
		buf.put(clutId.toBytes());
		buf.putShort((short)x1);
		buf.putShort((short)y1);
		buf.put((byte)u1);
		buf.put((byte)v1);
		buf.put(tpage.toBytes());
		buf.putShort((short)x2);
		buf.putShort((short)y2);
		buf.put((byte)u2);
		buf.put((byte)v2);
		buf.putShort((short)0);
		buf.putShort((short)x3);
		buf.putShort((short)y3);
		buf.put((byte)u3);
		buf.put((byte)v3);
		return buf.array();
	}

	
}

package common.gpu.primitive;

import common.Util;

public class ClutId {
	public int x,y;	//unsigned short
	
	public ClutId() {}
	
	public ClutId(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public ClutId(short s){
		this.x = (s&0x3F)*16;	//x除以16,占6位
		this.y = (s>>>6)&0x1FF; //y占9位
	}
	
	public byte[] toBytes(){
		byte[] ret = new byte[2];
		ret[0]= (byte)((y&3)<<6 | x/16&0x3F);
		ret[1]= (byte)(y>>>2);
		return ret;
	}
	@Override
	public String toString() {
		return String.format("(%d,%d)", x,y);
	}
	public static void main(String[] args) {
		System.out.println(Util.hexEncode(new ClutId(496,728).toBytes()));
		System.out.println(Util.hexEncode(new ClutId(960,480).toBytes()));
		System.out.println(new ClutId((short) 0xb61f));
	}
}

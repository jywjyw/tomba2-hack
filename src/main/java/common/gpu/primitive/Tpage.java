package common.gpu.primitive;

import common.Util;

public class Tpage {
	public static void main(String[] args) {
		Tpage t=new Tpage(Util.decodeHex("1500"));
		System.out.println(t);
		System.out.println(Util.hexEncode(t.toBytes()));
	}
	
	public static final int SIZE=2;
	
	private int tx,ty,abr,tp;
	
	public Tpage(byte[] bs){
		short s = (short)(bs[0] | bs[1]<<8);
		this.tx = s&0xf;
		this.ty = s>>>4&1;
		this.abr = s>>>5&3;
		this.tp = s>>>7&3;
	}
	
	public int getTx(){
		return 64*tx;
	}
	
	public int getTy(){
		if(this.ty==0) return 0;
		else if(this.ty==1) return 256;
		else throw new RuntimeException();
	}
	
	public int getBit(){
//		if(this.tp==0) return 4;
//		else if(this.tp==1) return 8;
//		else if(this.tp==2) return 15;
//		else throw new RuntimeException();
		return this.tp; //just show raw data
	}
	
	public byte[] toBytes(){
		byte[] bs = new byte[2];
		bs[0]=(byte)(this.tx&0xf | (this.ty&1)<<4 | (this.abr&3)<<5 | (this.tp&1)<<7);
		bs[1]=(byte) (this.tp&2);
		return bs;
	}

}

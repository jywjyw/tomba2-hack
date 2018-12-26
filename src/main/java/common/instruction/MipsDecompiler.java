package common.instruction;

import common.Util;

public class MipsDecompiler {
	public static void main(String[] args) {
		System.out.println(Long.parseLong("80079344",16));
		byte[] bs=Util.decodeHex("0000a2ac");
		System.out.println(new MipsDecompiler().decodeInstruction(bs));
	}
	
	public String decodeInstruction(byte[] bs){
		int i=toInt(bs);
		int op=i>>>26;
		if(op==43){
			I it = decodeI(i);
			return String.format("sw $%s, %x($%s)", Register.name(it.rs), it.immediate, Register.name(it.rt));
		} else {
			return "unk";
		}
	}
	
	private I decodeI(int i){
		return new I(i>>>21&0x1f, i>>>16&0x1f, (short)i);
	}
	
//	private byte[] encodeItype(int op, int rs, int rt, short immediate){
//		
//	}
//	
//	private int opcode(int x, int y){
//		
//	}
	
	//little endian
	private int toInt(byte[] bs4){
		return ((bs4[3]&0xff) << 24) | ((bs4[2]&0xff) << 16) | ((bs4[1]&0xff) << 8) | (bs4[0]&0xff);
	}
	
	class I{
		public int rs, rt;
		public short immediate;
		public I(int rs, int rt, short immediate) {
			this.rs = rs;
			this.rt = rt;
			this.immediate = immediate;
		}
	}

}

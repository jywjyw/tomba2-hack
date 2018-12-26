package common.instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Special {
	private static List<String> names = new ArrayList<>();
	
	static {
		for(int i=0;i<64;i++){
			names.add(null);
		}
		names.set(0, "sll");
		names.set(2, "srl");
		names.set(3, "sra");
		names.set(4, "sllv");
		names.set(6, "srlv");
		names.set(7, "srav");
		names.set(8, "jr");
		names.set(9, "jalr");
		names.set(12, "syscall");
		names.set(13, "break");
		names.set(16, "mfhi");
		names.set(17, "mthi");
		names.set(18, "mflo");
		names.set(19, "mtlo");
		names.set(24, "mult");
		names.set(25, "multu");
		names.set(26, "div");
		names.set(27, "divu");
		names.set(32, "add");
		names.set(33, "addu");
		names.set(34, "sub");
		names.set(35, "subu");
		names.set(36, "and");
		names.set(37, "or");
		names.set(38, "xor");
		names.set(39, "nor");
		names.set(42, "slt");
		names.set(43, "sltu");
	}
	
	public static int index(String name){
		int i = names.indexOf(name);
		if(i==-1) throw new UnsupportedOperationException("unk op name: "+name);
		return i;
	}
	
	public static String name(int index){
		return names.get(index);
	}
	
	public static void main(String[] args) {
		System.out.println(index("sll"));
	}
	

}

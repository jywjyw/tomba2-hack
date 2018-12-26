package common.instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * 寄存器
 */
public class Op {
	public static void main(String[] args) {
		System.out.println(Op.index("lw"));
	}
	
	private static List<String> l = new ArrayList<>();
	static {
		l.add("special");
		l.add("bcond");
		l.add("j");
		l.add("jal");
		l.add("beq");
		l.add("bne");
		l.add("blez");
		l.add("bgtz");
		l.add("addi");
		l.add("addiu");
		l.add("slti");
		l.add("sltiu");
		l.add("andi");
		l.add("ori");
		l.add("xori");
		l.add("lui");
		l.add("cop0");
		l.add("cop1");
		l.add("cop2");
		l.add("cop3");
		for(int i=0;i<12;i++){
			l.add("");
		}
		l.add("lb");
		l.add("lh");
		l.add("lwl");
		l.add("lw");
		l.add("lbu");
		l.add("lhu");
		l.add("lwr");
		l.add("");
		l.add("sb");
		l.add("sh");
		l.add("swl");
		l.add("sw");
		l.add("");
		l.add("");
		l.add("swr");
		l.add("");
		l.add("lwc0");
		l.add("lwc1");
		l.add("lwc2");
		l.add("lwc3");
		for(int i=0;i<4;i++){
			l.add("");
		}
		l.add("swc0");
		l.add("swc1");
		l.add("swc2");
		l.add("swc3");
		for(int i=0;i<4;i++){
			l.add("");
		}
	}
	
	public static int index(String name){
		int i = l.indexOf(name);
		if(i==-1) throw new UnsupportedOperationException("unk op: "+name);
		return i;
	}
	
	public static String name(int index){
		return l.get(index);
	}
	
}

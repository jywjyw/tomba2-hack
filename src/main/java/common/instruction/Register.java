package common.instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * 寄存器
 */
public class Register {
	public static void main(String[] args) {
		System.out.println(index("s1"));
	}
	
	private static List<String> names = new ArrayList<>();
	static {
		names.add("r0"); //alway zero, alias ZR
		names.add("at");
		names.add("v0");
		names.add("v1");
		names.add("a0");
		names.add("a1");
		names.add("a2");
		names.add("a3");
		names.add("t0");
		names.add("t1");
		names.add("t2");
		names.add("t3");
		names.add("t4");
		names.add("t5");
		names.add("t6");
		names.add("t7");
		names.add("s0");
		names.add("s1");
		names.add("s2");
		names.add("s3");
		names.add("s4");
		names.add("s5");
		names.add("s6");
		names.add("s7");
		names.add("t8");
		names.add("t9");
		names.add("k0");
		names.add("k1");
		names.add("gp");
		names.add("sp");
		names.add("fp");
		names.add("ra");
	}
	
	public static int index(String name){
		int i = names.indexOf(name.replace("$", ""));
		if(i==-1) throw new UnsupportedOperationException("unk register name: "+name);
		return i;
	}
	
	public static String name(int index){
		return names.get(index);
	}
	
}

package common;

import java.io.UnsupportedEncodingException;

public class GbkCharGen {
	public static void main(String[] args) {
		System.out.println(gen(50));
	}
	
	public static String gen(int count){
		int a=0xb0, b=0xa1;
		StringBuilder ret=new StringBuilder();
		while(count-->0){
			try {
				ret.append(new String(new byte[]{(byte)(a&0xff), (byte)(b&0xff)}, "gbk"));
			} catch (UnsupportedEncodingException e) {	}
			b++;
			if(b>0xfe) {
				b=0xa1;
				a++;
				if(a>0xf7)
					throw new UnsupportedOperationException("too many char");
			}
		}
		return ret.toString();
	}

}

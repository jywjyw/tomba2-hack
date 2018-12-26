package tomba2.hack;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

import common.Util;


public class TranslateTextReader {
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		List<String> ret=TranslateTextReader.readGBK("{02f0}{03f0B}从不同沸点的混合液体中分离{br}出一种液体{end}");
		for(String s:ret)
			System.out.println(s);
	}
	
	public static List<String> readGBK(String text) {
		DataInputStream is=null;
		try {
			is = new DataInputStream(new ByteArrayInputStream(text.getBytes("gbk")));
		} catch (UnsupportedEncodingException e) {
		}
		StringBuilder unit = null;
		int mode = 0; //0=normal, 控制符=1, 特殊字(图标)=2;
		List<String> ret = new ArrayList<>();
		while(true) {
			try {
				byte b = is.readByte();
				if(mode==1) {
					unit.append((char)b);
					if(b=='}') {
						mode=0;
						ret.add(unit.toString());
					}
				} else if(mode==2){
					unit.append((char)b);
					if(b==']') {
						mode=0;
						ret.add(unit.toString());
					}
				} else {
					if(b=='{') {
						mode=1;
						unit=new StringBuilder();
						unit.append((char)b);
					} else if(b=='['){
						mode=2;
						unit=new StringBuilder();
						unit.append((char)b);
					} else {
						String char_ = null;
						if(b>=0&&b<=0x7f) {	//ascii编码下直接转换
							char_ = String.valueOf((char)b);
						} else {	//超出ascii编码时,再读一个字节拼接起来
							char_ = new String(new byte[]{b, is.readByte()}, "gbk");
						}
						ret.add(char_);
					}
				}
			} catch(EOFException e1) {
				break;
			} catch(IOException e2) {
				break;
			}
		}
		Util.close(is);
		return ret;
	}
}

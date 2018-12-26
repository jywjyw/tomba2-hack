package tomba2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import common.MultiLayerChars;

public class EncodingMenu {
	public static void main(String[] args) {
		new EncodingMenu();
	}
	
	public Map<String,Integer> 
			char_code=new LinkedHashMap<>(),
			char_count=new LinkedHashMap<>();
	LinkedList<Integer> availableCodes=new LinkedList<>();
	
	public MultiLayerChars chars=new MultiLayerChars();
	
	public EncodingMenu(){
		for(int j=0;j<=0xE;j+=2){
			for(int k=0;k<25;k++){
				for(int i=0x80;i<=0x83;i++){
					int code=(i<<8)+(j<<4)+k;
					availableCodes.add(code);
//					System.out.printf("%X\n",code);
				}
			}
		}
	}
	
	public Integer get(String key){
		Integer code=char_code.get(key);
		if(code!=null){
			char_count.put(key, char_count.get(key)+1);
		}
		return code;
	}
	
	public Integer put(String key){
		if(char_code.containsKey(key)) throw new UnsupportedOperationException();
		int i=availableCodes.pop();
		char_code.put(key, i);
		char_count.put(key, 1);
		chars.put(key);
		return i;
	}
	
	public void debug(String targetFile){
		try {
			FileOutputStream fos = new FileOutputStream(targetFile);
			for(Entry<String,Integer> e:char_code.entrySet()){
				fos.write(String.format("%04X=%s\n", e.getValue(),e.getKey()).getBytes("gbk"));
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int size(){
		return char_code.size();
	}
	
	public void printOnceCharCount(){
		for(Entry<String,Integer> e: char_count.entrySet()){
			if(e.getValue()==1)
				System.out.printf("%s=%d\n",e.getKey(),e.getValue());
		}
	}
	
}

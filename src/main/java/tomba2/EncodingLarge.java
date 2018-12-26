package tomba2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
JIS X 0208字集的所有字符
“第一位字节”使用0x81-0x9F、0xE0-0xEF（共47个）
“第二位字节”使用0x40-0x7E、0x80-0xFC（共188个）
 */
public class EncodingLarge {
	
	public Map<String,Integer> map=new LinkedHashMap<>();
	Integer accum=0x889F;
	
	public Integer get(String key){
		return map.get(key);
	}
	
	public Integer put(String key){
		if(map.containsKey(key)) throw new UnsupportedOperationException();
		int use=accum.intValue();
		map.put(key, use);
		if((accum&0xff)==0x7E)	accum+=2;
		else if((accum&0xff)==0xFC)	accum+=0x44;
		else accum++;
		return use;
	}
	
	public String getAllChinese(){
		StringBuilder ret = new StringBuilder();
		for(Entry<String,Integer> e:map.entrySet()){
			if(e.getValue()>=0x889f && e.getValue()<=0x9872)
				ret.append(e.getKey());
		}
		return ret.toString();
	}
	
	public void debug(String targetFile){
		try {
			FileOutputStream fos = new FileOutputStream(targetFile);
			for(Entry<String,Integer> e:map.entrySet()){
				fos.write(String.format("%04X=%s\n", e.getValue(),e.getKey()).getBytes("gbk"));
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tomba2.EncodingLarge;

public class Charset {
	Map<Integer,String> code_char = new HashMap<>();
	Map<String,Integer> char_code = new HashMap<>();
	
	public static Charset loadJp(){
		return new Charset(Thread.currentThread().getContextClassLoader().getResourceAsStream("jp.gbk.tbl"));
	}
	
	public static Charset loadEn(){
		return new Charset(Thread.currentThread().getContextClassLoader().getResourceAsStream("en.gbk.tbl"));
	}
	
	public static Charset loadSmallJp(){
		return new Charset(Thread.currentThread().getContextClassLoader().getResourceAsStream("jp-small.gbk.tbl"));
	}
	
	public static Charset load(File f){
		try {
			return new Charset(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Charset(InputStream is) {
		BufferedReader reader = null;
		try {
			reader  = new BufferedReader(new InputStreamReader(is, "gbk"));
			String l = null;
			while((l=reader.readLine())!=null){
				if(l.length()>0 && !l.startsWith("#")){
					String[] arr = l.split("=",2);
					int code = Integer.parseInt(arr[0], 16);
					code_char.put(code,arr[1]);
					char_code.put(arr[1], code);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException();
		} finally{
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}
	
	public String getChar(int code) {
		return code_char.get(code);
	}
	
	public boolean containChar(int code) {
		return code_char.containsKey(code);
	}
	
	public Integer getCode(String char_) {
		return char_code.get(char_);
	}
	
	public EncodingLarge getNonKanjiTable(){
		EncodingLarge ret = new EncodingLarge();
		for(Entry<String,Integer> e:char_code.entrySet()){
			if(e.getValue()<0x889f || e.getValue()>0x9872)
				ret.map.put(e.getKey(), e.getValue());
		}
		return ret;
	}
	
}

package common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.DirLooper.Callback;

public class HexSearcher {
	/**
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("searching...");
//		HexSearcher.searchDir(Conf.jpdiskdir, "90b082ea82bd");
		HexSearcher.searchDir("d:/hanhua/piaoliuji", "823d3282");
		System.out.println("finish...");
	}
	 
	public static void searchDir(String dir, String query){
		byte[] q = Util.decodeHex(query.replace(" ", ""));
		DirLooper.loop(dir, new Callback() {
			@Override
			public void handleFile(File f) {
				search(f, q);
			}
		});
	}
	
	public static void searchFile(File f, String query) {
		search(f, Util.decodeHex(query.replace(" ", "")));
	}
	
	public static void search(File f, byte[] query){
		try {
			PushbackInputStream is = new PushbackInputStream(new ByteArrayInputStream(Util.loadFile(f)), query.length);
			long addr=0;
			byte[] buf = new byte[query.length];
			while(is.read(buf)==buf.length){
				if(Arrays.equals(query, buf)){
					System.err.printf("%s : found!! 0x%08X\n", f, addr);
				}
				is.unread(buf, 1, buf.length-1);
				addr++;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<Integer> searchSingleFile(File f, byte[] query){
		try {
			PushbackInputStream is = new PushbackInputStream(new ByteArrayInputStream(Util.loadFile(f)), query.length);
			int addr=0;
			byte[] buf = new byte[query.length];
			List<Integer> ret = new ArrayList<>();
			while(is.read(buf)==buf.length){
				if(Arrays.equals(query, buf)){
					ret.add(addr);
				}
				is.unread(buf, 1, buf.length-1);
				addr++;
			}
			return ret;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

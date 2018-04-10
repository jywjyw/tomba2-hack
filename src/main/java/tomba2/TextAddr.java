package tomba2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TextAddr implements Iterable<Entry<Integer, Integer>> {
	
	Map<Integer,Integer> addr_size = new LinkedHashMap<>();
	
	public static TextAddr loadJp(){
		return new TextAddr("text_addr_jp.properties");
	}
	
	public static TextAddr loadEn(){
		return new TextAddr("text_addr_english.properties");
	}
	
	private TextAddr(String file){
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String s;
			while ((s = br.readLine()) != null) {
				addr_size.put(Integer.parseInt(s, 16), 0);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Iterator<Entry<Integer, Integer>> iterator() {
		return addr_size.entrySet().iterator();
	}
	
}

package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RscLoader {
	
	public static void load(String classpathRsc, String enc, Callback cb) {
		BufferedReader reader = null;
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathRsc);
		try {
			reader  = new BufferedReader(new InputStreamReader(is, enc));
			String l = null;
			while((l=reader.readLine())!=null){
				if(l.length()>0 && !l.startsWith("#")){
					cb.doInline(l);
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
	
	public interface Callback{
		void doInline(String line);
	}

}

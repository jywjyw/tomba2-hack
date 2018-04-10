package common;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Conf {
	
	public static String jpDat,enDat,desktop;
	
	static {
		InputStream is=null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("conf.prop");
			Properties conf = new Properties();
			conf.load(is);
			jpDat = conf.getProperty("jpdat");
			assertNotnull(jpDat);
			enDat = conf.getProperty("endat");
			desktop = conf.getProperty("desktop");
			assertNotnull(desktop);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
	
	private static void assertNotnull(Object o) {
		if(o==null)throw new RuntimeException("conf.prop初始化失败..");
	}

}

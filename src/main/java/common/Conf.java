package common;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Conf {
	
	public static String jpdiskdir,endiskdir,desktop, outdir;
	
	public static int UV_SPACE = 1380; //把MAIN.EXE中的UV对照表去掉后,可节省的ROM空间字节数
	
	static {
		InputStream is=null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("conf.properties");
			Properties conf = new Properties();
			conf.load(is);
			jpdiskdir = conf.getProperty("jp_disk_dir");
			endiskdir = conf.getProperty("en_disk_dir");
			desktop = conf.getProperty("desktop");
			outdir = conf.getProperty("out_dir");
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
	
	public static String getRawFile(String rawFile){
		return System.getProperty("user.dir")+"/raw/"+rawFile;
	}
	public static String getTranslationFile(String transFile){
		return System.getProperty("user.dir")+"/translation/"+transFile;
	}
	
	public static String getJpDat(){
		return jpdiskdir+"/CD/TOMBA2.DAT";
	}
	public static String getEnDat(){
		return endiskdir+"/CD/TOMBA2.DAT";
	}
	public static String getJpExe(){
		return jpdiskdir+"/MAIN.EXE";
	}
	public static String getEnExe(){
		return endiskdir+"/MAIN.EXE";
	}
	public static String getJpSop(){
		return jpdiskdir+"/BIN/SOP.BIN";
	}
	public static String getEnSop(){
		return endiskdir+"/BIN/SOP.BIN";
	}
	
	public static int getExeOffset(int addr){ //从exe的0x800开始,加载到80010000
		return addr-0x8000f800;
	}
	public static int getExeAddr(int offset){
		return 0x8000f800+offset;
	}
	public static void main(String[] args) {
		System.out.println(Integer.toHexString(getExeOffset(0x80017b08)));
		System.out.println(Integer.toHexString(getExeOffset(0x80017b0c)));
		System.out.println(Integer.toHexString(getExeAddr(0x451c)));
	}

}

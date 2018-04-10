package common;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Util {
	public static void main(String[] args) {
		System.out.println(Util.toMd5(new File("d:\\ps3\\musashiden-jp.ISO")));
	}
	
	public static String toMd5(File file) {  
    	InputStream fis = null;  
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);  
			byte[] buf = new byte[1024];  
			int len = 0;  
			while ((len = fis.read(buf)) > 0) {  
			    md.update(buf, 0, len);  
			}  
			byte[] bytes = md.digest();
			
			char[] hex = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};  
			StringBuffer sb = new StringBuffer(2 * bytes.length);  
	        for (int i = 0; i < bytes.length; i++) {  
	            char c0 = hex[(bytes[i] & 0xf0) >> 4];// 取字节中高 4 位的数字转换  
	            // 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同  
	            char c1 = hex[bytes[i] & 0xf];// 取字节中低 4 位的数字转换  
	            sb.append(c0).append(c1);  
	        }  
	        return sb.toString();  
        } catch (Exception e1) {
        	throw new RuntimeException(e1);
		} finally	{
			try {
				if(fis != null)	fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }  
	
	
	public static int hilo(int i) {
		return i>>>24|i>>>8&0xff00|i<<8&0xff0000|i<<24&0xff000000;
	}
	
	public static int hiloShort(int i) {
		return i>>>8&0xff|i<<8&0xff00;
	}
	
	private static String show(Set<String> set) {
		StringBuilder sb = new StringBuilder();
		for(String s : set) {
			sb.append(s+" ");
		}
		return sb.toString();
	}
	
	/** 
	 * MD5加密
	 * @param cont 要加密的字节数组 
	 * @return    加密后的字符串 
	 */  
	public static String toMd5(byte[] cont){  
	    try {  
	        MessageDigest md = MessageDigest.getInstance("MD5");  
	        md.update(cont);  
	        byte[] byteDigest = md.digest();  
	        int i;  
	        StringBuilder buf = new StringBuilder();  
	        for (int offset = 0; offset < byteDigest.length; offset++) {  
	            i = byteDigest[offset];  
	            if (i < 0)  i += 256;  
	            if (i < 16) buf.append("0"); 
	            buf.append(Integer.toHexString(i));
	        }
//	        return buf.toString().substring(8, 24);		//16位加密     
	        return buf.toString();						//32位加密    
	    } catch (NoSuchAlgorithmException e) {  
	        throw new RuntimeException(e); 
		}
	}
	
	/**
	 * 把request.getParameterMap()转成字符串, 适用于查看POST参数
	 * @param paramMap
	 * @param charset 是否要把参数urlencode
	 * @return
	 */
	public static String paramMapToString(Map paramMap, String charset)	{
		if(paramMap.size() == 0)	return "";
		StringBuilder sb = new StringBuilder();
		for(Object o : paramMap.entrySet())	{
			Entry e = (Entry)o;
			if(e.getValue() instanceof String[])	{
				for(String val : (String[])e.getValue())	{
					if(charset != null)	{
						try {
							val = URLEncoder.encode(val, charset);
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
					}
					sb.append(e.getKey()).append("=").append(val).append("&");
				}
			} else {
				String value = (String)e.getValue();
				if(charset != null)	{
					try {
						value = URLEncoder.encode(value, charset);
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}
				sb.append(e.getKey()).append("=").append(value).append("&");
			}
		}
		if(sb.toString().endsWith("&"))	{
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
	public static String join(List<String> list, String splitter)	{
		return join(list, splitter, null);
	}
	
	/**
	 * 将字符串集合分隔拼接成一条字符串
	 * @param list 集合
	 * @param splitter 分隔符
	 * @param replacement 将与分隔符有冲突的字符串替换
	 * @return
	 */
	public static String join(List<String> list, String splitter, String replacement)	{
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<list.size(); i++)	{
			if(replacement != null && list.get(i) != null)	{
				sb.append(list.get(i).replace(splitter, replacement));
			} else	{
				sb.append(list.get(i));
			}
			if(i < list.size() - 1)	{
				sb.append(splitter);
			}
		}
		return sb.toString();
	}
	
	public static String toHexString(byte[] bs) {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<bs.length;i++) {
			sb.append(String.format("%02X", bs[i]&0xff));
		}
		return sb.toString();
	}
	
	/**
	 * 关闭多个流
	 * @param closeable
	 */
	public static void close(Closeable...closeable)	{
		for(Closeable c : closeable)	{
			try {
				if(c != null)	c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static byte[] copyPartFile(String file, long startPos, int length) {
		byte[] buf = new byte[length];
		try {
			RandomAccessFile bin = new RandomAccessFile(new File(file), "r");
			bin.seek(startPos);
			bin.read(buf);
			bin.close();
			return buf;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void copyFile(String src, String to){
		try {
			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(to);
			FileChannel in = fis.getChannel();
			in.transferTo(0, in.size(), fos.getChannel());
			fis.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void overwriteFile(String file, long startPos, byte[] data) {
		try {
			RandomAccessFile bin = new RandomAccessFile(new File(file), "rw");
			bin.seek(startPos);
			bin.write(data);
			bin.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void writeFile(File target, byte[] data){
		try {
			FileOutputStream fos = new FileOutputStream(target);
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void append(RandomAccessFile parent, File child) throws IOException{
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(child);
			byte[] buf = new byte[1024];
			int len=0;
			while((len=fis.read(buf))!=-1){
				parent.write(buf, 0, len);
			}
		} finally{
			Util.close(fis);
		}
	}
	
	public static String getSuffix(String filename) {
		if(filename == null)	return null;
		int dot = filename.lastIndexOf('.');
		return dot == -1 ? "" : filename.substring(dot);
	}
	
	public static void mkdirs(File file){
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
	}
	
	public static int get0x800Multiple(int len){
		if(len%0x800==0){
			return len;
		} else{
			return (len/0x800+1)*0x800;
		}
	}
	public static int get0x800MultipleDiff(int len){
		int remainder=len%0x800;
		if(remainder==0){	
			return 0;
		} else{
			return 0x800-remainder;
		}
	}
	
	public static int getMultiple(int i, int multiple){
		if(i%multiple==0) return i;
		else
			return (i/multiple+1)*multiple;
	}
	
}

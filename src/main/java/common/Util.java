package common;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Util {
	
	public static String md5(File file) {  
    	InputStream fis = null;  
    	byte[] buf = new byte[1024];  
    	int len = 0;  
    	char[] hex = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};  
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);  
			while ((len = fis.read(buf)) > 0) {  
			    md.update(buf, 0, len);  
			}  
			byte[] bytes = md.digest();
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
	
	public static String md5(byte[] cont){  
	   return hexEncode(md5bytes(cont));
	}
	
	public static byte[] md5bytes(byte[] cont){
		try {  
	        MessageDigest md = MessageDigest.getInstance("MD5");  
	        md.update(cont);  
	        return md.digest();  
	    } catch (NoSuchAlgorithmException e) {  
	        throw new RuntimeException(e); 
		}
	}
	
	
	public static int hilo(int i) {
		return i>>>24|i>>>8&0xff00|i<<8&0xff0000|i<<24&0xff000000;
	}
	
	public static int hiloShort(int unsignedShort) {
		return unsignedShort>>>8&0xff|unsignedShort<<8&0xff00;
	}
	
	public static int toInt(byte a, byte b) {
		return (a&0xff)<<8|b&0xff;
	}
	
	public static int toInt(byte a, byte b, byte c, byte d) {
		return (a << 24) + (b << 16) + (c << 8) + (d << 0);
	}
	
	public static byte[] toBytes(int i){
		return new byte[]{
			(byte)(i>>>24),(byte)(i>>>16),(byte)(i>>>8),(byte)i
		};
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
	
	public static byte[] loadFile(File file) throws IOException{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while((len=bis.read(buf))!=-1){
			bos.write(buf, 0, len);
		}
		return bos.toByteArray();
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
	
	public static void appendToFileTail(RandomAccessFile parent, File child) throws IOException{
		byte[] buf = new byte[1024];
		int len=0;
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(child);
			while((len=fis.read(buf))!=-1){
				parent.write(buf, 0, len);
			}
		} finally{
			Util.close(fis);
		}
	}
	
	//contains dot
	public static String getSuffix(String filename) {
		if(filename == null)	return null;
		int dot = filename.lastIndexOf('.');
		return dot == -1 ? "" : filename.substring(dot);
	}
	
	public static void mkdirs(File file){
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
	}
	
	//对齐至0x800
	public static int align800H(int i){
		if(i%0x800==0){
			return i;
		} else{
			return (i/0x800+1)*0x800;
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
	
    public static String hexEncode(byte[] bs) {   
        StringBuilder ret = new StringBuilder(bs.length*2);
        String tmp=null;
        for(byte b:bs){
        	tmp=Integer.toHexString(b&0xff);//don't use String.format(), too slow
        	if(tmp.length()==1) ret.append("0");
        	ret.append(tmp);
        }
        return ret.toString();
    }
    
    public static byte[] decodeHex(String hex) {
    	hex=hex.replace(" ", "");
    	if(hex.length()%2!=0) 
    		throw new UnsupportedOperationException("hex length must be even number : "+hex);
    	int len = hex.length()/2;
        byte[] ret = new byte[len];
        for (int i=0; i<len; i++) {  
            ret[i] = (byte)Integer.parseInt(hex.substring(i*2, i*2+2),16);  
        }  
        return ret;
    }  

	public static boolean isNotEmpty(String s) {
		return s!=null && s.length()>0;
	}
	
	public static void assertTrue(boolean b){
		if(!b)throw new RuntimeException();
	}
	
	public static void reverseArray(byte[] data){
		for (int left = 0, right = data.length - 1; left < right; left++, right--) {
	        byte temp = data[left];
	        data[left]  = data[right];
	        data[right] = temp;
	    }
	}
	
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
	
	public static int getMultiple(int i, int multiple){
		if(i%multiple==0) return i;
		else
			return (i/multiple+1)*multiple;
	}
	
	/**
	 * 将String数组转换为Int数组
	 * @param s
	 * @return
	 */
	public static int[] toIntArray(String[] s)	{
		int[] ints = new int[s.length];
		for(int i=0; i<s.length; i++)	{
			ints[i] = Integer.parseInt(s[i]);
		}
		return ints;
	}
	
	
}

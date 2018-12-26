package common;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;


public class Palette {
	
	public static void main(String[] args) throws IOException {
//		Palette p = new Palette(16, Conf.getRawFile("clut/memcard.16"));
//		for(int[] i:p.rgba8888Matrix){
//			System.out.printf("%x,%x,%x,%x\n",i[0],i[1],i[2],i[3]);
//		}
		Palette.init16Grey();
	}
	
	private static final int FULL_TRANSPARENT=0,SEMI_TRANSPARENT=0x7E,NON_TRANSPARENT = 0xFF;
	
	private int colorCount;
	public int[][] rgba5551Matrix;	//PS原始色板数值:每个颜色范围是0~31
	public int[][] rgba8888Matrix;	//转换成RGB888每个颜色范围是0~255
	
	public static Palette init16Grey(){
		int count=16;
		ByteBuffer grey=ByteBuffer.allocate(count*2);
		grey.order(ByteOrder.LITTLE_ENDIAN);
		for(int i=0;i<count;i++){
			int c=i*16;
			short color=(short)(c<<11 | c<<6 | c<<1);  
			grey.putShort(color);
		}
		return new Palette(count, new ByteArrayInputStream(grey.array()));
	}
	
	/**
	 * generate 256 color palette, contains 256 different colors, all color's semi transparent is 0
	 */
	public static Palette init256(){
		int count=256;
		ByteBuffer pal=ByteBuffer.allocate(count*2);
		pal.order(ByteOrder.LITTLE_ENDIAN);
		for(int i=0;i<count;i++){
			short color=(short)(i<<1);
			pal.putShort(color);
		}
		return new Palette(count, new ByteArrayInputStream(pal.array()));
	}
	
	/**
	 * 调色板中每种色彩格式RGBA5551(即色彩深度为15位色，每种原色有2^5=32个层次，共32768种颜色)
	 * @param colorCount 调色板内所含颜色个数. 16 or 256
	 * @param data 从ROM中复制出色板数据,或使用PVV导出色板,导出方法:选择RAW(8bit)
	 */
	private Palette(int colorCount, InputStream data) {
		this.colorCount = colorCount;
		rgba5551Matrix = new int[colorCount][4];
		rgba8888Matrix = new int[colorCount][4];
		DataInputStream is = new DataInputStream(data);
		int buf=0;
		try {
			for(int i=0; i<colorCount; i++) {
				buf=is.readUnsignedShort();
				buf = buf<<8&0xff00|buf>>>8;
				int r = buf&0x1f, g = buf>>>5&0x1f, b= buf>>>10&0x1f, stp = buf>>>15&1;
				rgba5551Matrix[i] = new int[]{r,g,b,stp};
				rgba8888Matrix[i] = new int[]{_32to256(r),_32to256(g),_32to256(b),calculateAlpha(r,g,b,stp)};
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		
		for(int[] i:rgba5551Matrix){
			for(int[] j:rgba5551Matrix){
				if(i[0]==j[0] && i[1]==j[1] && i[2]==j[2] && i[3]!=j[3]){
					throw new UnsupportedOperationException("this palette has same rgb but different alpha color, this may cause bug");
				}
			}
		}
	}
	
	public Palette(int colorCount, String file) throws FileNotFoundException {
		this(colorCount, new FileInputStream(file));	// 因为导出的raw色板全都是512byte,无法用程序确定颜色数
	}
	public Palette(int colorCount, byte[] data) {
		this(colorCount, new ByteArrayInputStream(data));
	}
	
	/**
	 * 把调色板的15位色转化为32位真彩色(含半透明通道), 
	 * 32位PNG颜色格式为RGBA8888, 示例:BufferedImage.setPixel(x,y,new int[]{red,green,blue,alpha}) 
	 * RGB取值:00~FF, alpha取值: 00(全透明)~FF(不透明)
	 * 
	 * 影响颜色是否透明有三种控制元素: 1.半透明开关指令(图像外部控制) 2.颜色本身的半透明指示符  3.颜色是否为黑色
	 * BGR		STP		CMD_OFF				CMD_ON
	 * 0,0,0	0		Transparent			Transparent
	 * 0,0,0	1		Non-Transparent		Non-Transparent
	 * x,x,x	0		Non-Transparent		Non-Transparent
	 * x,x,x	1		Non-Transparent		Transparent
	 * 
	 * @param index
	 * @return
	 */
	private static final boolean TRANSPARENT_CMD=true;
	
	private int calculateAlpha(int r, int g, int b, int stp){
		if(r==0&&g==0&&b==0) {
			return (stp==0 ? FULL_TRANSPARENT:NON_TRANSPARENT);
		} else {
			if(stp==0){
				return NON_TRANSPARENT;
			} else {
				return TRANSPARENT_CMD?SEMI_TRANSPARENT:NON_TRANSPARENT;
			}
		}
	}
	
	private int _32to256(int _32) {
		return (int)_32*255/31;
	}
	
	public void export(String out) throws IOException {
		final BufferedImage img = new BufferedImage(colorCount, 1, BufferedImage.TYPE_INT_RGB);	
		for(int i=0;i<colorCount;i++) {
			int[] rgba = rgba5551Matrix[i];
			img.setRGB(i, 0, new Color((int)(rgba[0]*255/31),(int)(rgba[1]*255/31),(int)(rgba[2]*255/31)).getRGB());
		}
		File file = new File(out);
		file.delete();
		ImageIO.write(img, "bmp", file);
	}
	
	
	public int getExactColorIndex(int r256,int g256, int b256){
		for(int i=0;i<rgba8888Matrix.length;i++){
			int[] arr=rgba8888Matrix[i];
			if(arr[0]==r256 && arr[1]==g256 && arr[2]==b256){
				return i;
			}
		}
		throw new RuntimeException(String.format("not found color in palette:(%x,%x,%x)",r256,g256,b256));
	}
	
	/**
	 * 求BufferedImage.TYPE_INT_ARGB颜色点与本色板最相似的颜色索引
	 * @param r BufferedImage.TYPE_INT_ARGB颜色点, 0~255
	 * @param g BufferedImage.TYPE_INT_ARGB颜色点, 0~255
	 * @param b BufferedImage.TYPE_INT_ARGB颜色点, 0~255
	 * @param a BufferedImage.TYPE_INT_ARGB颜色点
	 * @param p
	 * @return
	 */
	public int getSimilarColorIndex(int r256,int g256, int b256) {
		if(r256==255&&g256==255&&b256==255)
			return 0;
		
		double min = Double.MAX_VALUE;
		Integer index = null;
		for(int i=0;i<rgba8888Matrix.length;i++) {
			int 
				_r = Math.abs(rgba8888Matrix[i][0]-r256), 
				_g=Math.abs(rgba8888Matrix[i][1]-g256), 
				_b=Math.abs(rgba8888Matrix[i][2]-b256);
			double distance = Math.sqrt(_r*_r+_g*_g+_b*_b);
			if(distance<min) {
				min=distance;
				index = i;
			}
		}
		return index;
	}
	
	public int[] getSimilarColor(int r256,int g256, int b256) {
		if(r256==255&&g256==255&&b256==255)
			return new int[]{r256,g256,b256};
		
		double min = Double.MAX_VALUE;
		int[] similar = new int[3];
		for(int i=0;i<rgba8888Matrix.length;i++) {
			int 
				_r = Math.abs(rgba8888Matrix[i][0]-r256), 
				_g=Math.abs(rgba8888Matrix[i][1]-g256), 
				_b=Math.abs(rgba8888Matrix[i][2]-b256);
			double distance = Math.sqrt(_r*_r+_g*_g+_b*_b);
			if(distance<min) {
				min=distance;
				similar = rgba8888Matrix[i];
			}
		}
//		System.out.printf("old=%d,%d,%d, new=%d,%d,%d\n",r32,g32,b32,similar[0],similar[1],similar[2]);
		return similar;
	}
	
	public IndexColorModel toPng8ColorModel() {//TODO not test
		final int[] colors = {   0xff00ff00, 0xff000000, 0xffffffff, 0xff353535, 0xff888888, 0xff969696, 0xff237fe9, 0xffff0000 };//argb8888
		int[] model = new int[colorCount];
		for(int i=0;i<rgba8888Matrix.length;i++){
			int[] c=rgba8888Matrix[i];
			model[i]=c[3]<<24 | c[0]<<16 | c[1]<<8 | c[2]; 
		}
		return new IndexColorModel(8, colors.length, colors, 0, true, 0, DataBuffer.TYPE_BYTE);
		//png8 create example: BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
	}

	public int getColorCount() {
		return colorCount;
	}
	
	public int[][] getRgba8888Matrix() {
		return rgba8888Matrix;
	}

}

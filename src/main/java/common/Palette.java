package common;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


public class Palette {
	
	public static void main(String[] args) throws IOException {
//		Palette p = new Palette(256, "448-255-8bit");
//		p.export(Conf.desktop+"256.bmp");
		Palette p = new Palette(16, "1008-200");
//		System.out.println(p.getSimilarColorIndex(122, 122, 122));
		
	}
	private int colorCount;
	public int[][] rgb32Matrix;	//每个颜色范围是0~31,不是0~255
	public int[][] rgb256Matrix;	//每个颜色范围是0~255
	
	/**
	 * ROM中用到的调色板的色彩深度为15位色，每种原色有2^5=32个层次，共32768种颜色
	 * @param colorCount 调色板内所含颜色个数. 16 or 256
	 * @param file
	 */
	private Palette(int colorCount, InputStream data) {
		this.colorCount = colorCount;
		rgb32Matrix = new int[colorCount][4];
		rgb256Matrix = new int[colorCount][4];
		DataInputStream is = new DataInputStream(data);
		int buf=0;
		try {
			for(int i=0; i<colorCount; i++) {
				buf=is.readUnsignedShort();
				buf = buf<<8&0xff00|buf>>>8;
				int red = buf&0x1f, green = buf>>>5&0x1f, blue= buf>>>10&0x1f;
				int stp = buf>>>15&1;	//影响颜色是否透明有三种控制元素: 1.半透明开关指令(图像外部控制) 2.颜色本身的半透明指示符 3.颜色是否为黑色
				rgb32Matrix[i] = new int[]{red, green, blue, stp};
				
				if(red==0&&green==0&&blue==0) {
					int blackTransparent = stp==0?0:0xff;
					rgb256Matrix[i] = new int[]{0,0,0,blackTransparent};
				} else {
					int stp256 = stp==0?0xff:0x7e;
					rgb256Matrix[i] = new int[]{_32to256(red), _32to256(green), _32to256(blue), stp256};
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
	
	public Palette(int colorCount, String file) throws FileNotFoundException {
		this(colorCount, new FileInputStream(System.getProperty("user.dir")+"/raw/"+file));
	}
	public Palette(int colorCount, byte[] data) {
		this(colorCount, new ByteArrayInputStream(data));
	}
	
	/**
	 * 把调色板的15位色转化为32位真彩色(含半透明通道)
	 * @param index
	 * @return
	 */
	public int[] to32Rgba(int index) {
		//32位PNG下alpha取值: 00~FF 全透明~不透明
		int[]rgba = rgb32Matrix[index];
		if(rgba[0]==0&&rgba[1]==0&&rgba[2]==0) {
			int blackTransparent = rgba[3]==0?0:0xff;
			return new int[]{0,0,0,blackTransparent};
		} else {
			int transparent = rgba[3]==0?0xff:0x7e;
			return new int[]{_32to256(rgba[0]),_32to256(rgba[1]),_32to256(rgba[2]),transparent};
		}
	}
	
	private int _32to256(int _32) {
		return (int)_32*255/31;
	}
	
	public void export(String out) throws IOException {
		final BufferedImage img = new BufferedImage(colorCount, 1, BufferedImage.TYPE_INT_RGB);	
		for(int i=0;i<colorCount;i++) {
			int[] rgba = rgb32Matrix[i];
			img.setRGB(i, 0, new Color((int)(rgba[0]*255/31),(int)(rgba[1]*255/31),(int)(rgba[2]*255/31)).getRGB());
		}
		File file = new File(out);
		file.delete();
		ImageIO.write(img, "bmp", file);
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
	public int getSimilarColorIndex(int r32,int g32, int b32) {
		if(r32==255&&g32==255&&b32==255)
			return 0;
		
		double min = Double.MAX_VALUE;
		Integer index = null;
		for(int i=0;i<rgb256Matrix.length;i++) {
			int 
				_r = Math.abs(rgb256Matrix[i][0]-r32), 
				_g=Math.abs(rgb256Matrix[i][1]-g32), 
				_b=Math.abs(rgb256Matrix[i][2]-b32);
			double distance = Math.sqrt(_r*_r+_g*_g+_b*_b);
			if(distance<min) {
				min=distance;
				index = i;
			}
		}
		return index;
	}
	
	public int[] getSimilarColor(int r32,int g32, int b32) {
		if(r32==255&&g32==255&&b32==255)
			return new int[]{r32,g32,b32};
		
		double min = Double.MAX_VALUE;
		int[] similar = new int[3];
		for(int i=0;i<rgb256Matrix.length;i++) {
			int 
				_r = Math.abs(rgb256Matrix[i][0]-r32), 
				_g=Math.abs(rgb256Matrix[i][1]-g32), 
				_b=Math.abs(rgb256Matrix[i][2]-b32);
			double distance = Math.sqrt(_r*_r+_g*_g+_b*_b);
			if(distance<min) {
				min=distance;
				similar = rgb256Matrix[i];
			}
		}
//		System.out.printf("old=%d,%d,%d, new=%d,%d,%d\n",r32,g32,b32,similar[0],similar[1],similar[2]);
		return similar;
	}
	
	public IndexColorModel toIndexColorModel() {
		byte[] r = new byte[colorCount]; 
		byte[] g = new byte[colorCount]; 
		byte[] b = new byte[colorCount]; 
		for(int i=0;i<rgb32Matrix.length;i++) {
			int[] rgb = rgb32Matrix[i];
			r[i] =(byte) _32to256(rgb[0]);
			g[i] =(byte) _32to256(rgb[1]);
			b[i] =(byte) _32to256(rgb[2]);
		}
		return new IndexColorModel(5, colorCount, r, g, b);
	}

	public int getColorCount() {
		return colorCount;
	}

}

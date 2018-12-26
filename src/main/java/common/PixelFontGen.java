package common;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class PixelFontGen {
	
	public static void main(String[] args) throws FontFormatException, IOException {
//		PixelFontGen.genSingleBmp("酪司馆翻箱以评筒螺丝", Conf.getRawFile("方正像素15.TTF"), 15, 1);
		BufferedImage img=PixelFontGen.genSingleBmp("流星锤", Conf.desktop+"Zfull-GB.ttf", 10, 0);
		ImageIO.write(img, "bmp", new File(Conf.desktop+"aaa.bmp"));
	}
	
	//not test yet
	public static VramImg gen4bitVramImg(String allChar, String fontFile, int fontsize, int marginRight){
		BufferedImage img = genSingleBmp(allChar, fontFile, fontsize, marginRight);
		return Img4bitUtil.toColorIndexData(img, pixelConverter);
	}
	
	public static byte[] genSingleColorFont(String allChar, String fontFile, int fontsize, int marginRight){
		BufferedImage img = genSingleBmp(allChar, fontFile, fontsize, marginRight);
		int charW=fontsize+marginRight, charH=fontsize;
		int charSpace=charW*charH/8;	//每个字符占多少字节
		ByteBuffer ret = ByteBuffer.allocate(charSpace*allChar.length());
		int[] point = new int[3];
		WritableRaster rast=img.getRaster();
		for(int y=0; y<img.getHeight(); y++){
			StringBuilder bits = new StringBuilder();
			for(int x=0; x<img.getWidth(); x++){
				rast.getPixel(x, y, point);
				if(point[0]==0) 
					bits.append(1);//非黑即白，判断第一个像素即可
				else 
					bits.append(0);
			}
			int i = Integer.parseInt(bits.toString(), 2);
			ret.putShort((short)i);
		}
		return ret.array();
	}
	
	static PixelConverter pixelConverter = new PixelConverter() {
		@Override
		public int toPalIndex(int[] pixel) {
			if(pixel[0]==0) 
				return 1;//非黑即白，判断第一个像素即可
			else 
				return 0;
		}
	};
	
	//not test yet
	public static Palette build4bitPalette(){
		ByteBuffer data=ByteBuffer.allocate(32);
		data.putShort((short) 0xfeff);
		data.putShort((short) 1);
		return new Palette(16, data.array());
	}
	
	
	public static int[] 
			BACKGROUND_COLOR=new int[]{255,255,255},
			FONT_COLOR=new int[]{0,0,0};
	
	public static List<BufferedImage> genBmpTiles(String allChar, String fontFile, int fontsize, int marginRight){
		BufferedImage single=genSingleBmp(allChar, fontFile, fontsize, marginRight);
		List<BufferedImage> ret=new ArrayList<>();
		for(int y=0;y<single.getHeight();y+=fontsize){
			ret.add(single.getSubimage(0, y, single.getWidth(), fontsize));
		}
		return ret;
	}
	
	private static BufferedImage genSingleBmp(String allChar, String fontFile, int fontsize, int marginRight) {
		char[] chars=allChar.toCharArray();
		int charW=fontsize+marginRight,charH=fontsize;
		int imgH=(chars.length)*charH;
		BufferedImage large = new BufferedImage(charW, imgH, BufferedImage.TYPE_INT_RGB);
		Font font=null;
		try {
			InputStream is = new FileInputStream(fontFile); 
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		font=font.deriveFont(Font.PLAIN, fontsize);
		for(char c : chars) {
			if(!font.canDisplay(c)) throw new RuntimeException("字库无法识别以下字符:" + c);
		}
		
		Graphics2D g = (Graphics2D) large.getGraphics();
		g.setColor(Color.WHITE);	
		g.fillRect(0, 0, charW, imgH);
		
		g.setColor(Color.BLACK);
		g.setFont(font);
		for(int i=0;i<chars.length;i++) {
			int y = (i+1)*(charH);
			g.drawString(chars[i]+"", 0, y-2);	//向上修正N个像素,因为书写起始位置向上偏了点
		}
		g.dispose();
		
//		try {
//			ImageIO.write(large, "bmp", new File(Conf.desktop+"myfont.bmp"));
//		} catch (IOException e) {}
		
	    return large;
	}
}

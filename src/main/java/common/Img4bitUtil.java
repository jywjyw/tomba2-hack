package common;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * 1. one pixel occupy 4 bit
 * 2. display width is 4 times of vram width
 */
public class Img4bitUtil {
	
	public static BufferedImage readRomToBmp(InputStream in, int vramW, int vramH, Palette pal) throws IOException {
		int displayW = vramW*4;	//4bit下显示宽度*4;
		BufferedImage out = new BufferedImage(displayW, vramH, BufferedImage.TYPE_INT_RGB);
		return readRomToImg(in, vramW, vramH, pal, displayW, out);
	}
	
	
	public static BufferedImage readRomToPng32(InputStream in, int vramW, int vramH, Palette pal) throws IOException {
		int displayW = vramW*4;	//4bit下显示宽度*4;
		BufferedImage out = new BufferedImage(displayW, vramH, BufferedImage.TYPE_INT_ARGB);
		return readRomToImg(in, vramW, vramH, pal, displayW, out);
	}
	
	
	public static BufferedImage readPartToBmp(VramImg vram, int x, int y, int w, int h, Palette pal) throws IOException {
		if(x%4!=0 || w%2!=0) throw new UnsupportedOperationException("x or width must be even number");
		Palette grey=Palette.init16Grey();
		BufferedImage fullImg = readRomToBmp(new ByteArrayInputStream(vram.data), vram.w, vram.h, grey);
		VramImg subImg = toColorIndexData(fullImg.getSubimage(x, y, w, h), grey);
		return readRomToBmp(new ByteArrayInputStream(subImg.data), subImg.w, subImg.h, pal);
	}
	
	
	/**
	 * 
	 * @param in
	 * @param vramW 在显存中的宽度
	 * @param vramH 在显存中的高度
	 * @param pal
	 * @return
	 * @throws IOException
	 */
	private static BufferedImage readRomToImg(InputStream in, int vramW, int vramH, Palette pal, int displayW, BufferedImage out) throws IOException {
		WritableRaster outRaster = out.getRaster();
		byte[] buf = new byte[vramW*2];
		int x=0,y=0;
		int[][] colorBuf = new int[2][4];
		boolean _break=false;
		while(true) {
			int len = in.read(buf);
			if(len==-1) break;
			for(byte b : buf) {
				int i1 = b>>>4&0xf, i2 = b&0xf;
				colorBuf[0]=pal.getRgba8888Matrix()[i2];
				colorBuf[1]=pal.getRgba8888Matrix()[i1];
				for(int[] i : colorBuf) {
					outRaster.setPixel(x++, y, i);	//rgba
					if(x>=displayW) {
						x=0;
						y++;
						if(y>=vramH) {
							_break=true;
							break;
						}
					}
				}
			}
			if(_break)break;
		}
		return out;
	}
	
	
	//从palette中查找精确的颜色
	public static VramImg toColorIndexData(BufferedImage img, Palette pal){
		return toColorIndexData(img, new PixelConverter(){
			@Override
			public int toPalIndex(int[] pixel) {
				return pal.getExactColorIndex(pixel[0], pixel[1], pixel[2]);
			}
		});
	}
	
	public static VramImg toColorIndexData(BufferedImage img, PixelConverter cb){
		ByteBuffer ret = ByteBuffer.allocate(img.getWidth()*img.getHeight()/2);	 //8 bit image = 1 BytePerPixel, 4 bit image=0.5BytePerPixel
		int[] pixel=new int[4];
		byte buf=0;
		boolean bufFlag=true;
		for(int y=0;y<img.getHeight();y++){
			for(int x=0;x<img.getWidth();x++){	
				img.getRaster().getPixel(x, y, pixel);
				int index=cb.toPalIndex(pixel);
				if(bufFlag){
					buf=(byte)index;
				} else {
					buf|=index<<4;
					ret.put(buf);
				}
				bufFlag=!bufFlag;
			}
		}
		return new VramImg(img.getWidth()/4, img.getHeight(), ret.array());//8bit模式下,图像显示宽度*2. 4bit下,图像显示宽度*4
	}
	
	public static List<BufferedImage> splitToTiles(BufferedImage src, int tileW, int tileH){
		List<BufferedImage> tiles = new ArrayList<>();
		for(int y=0;y<src.getHeight();y+=tileH){
			for(int x=0;x<src.getWidth();x+=tileW){
				tiles.add(src.getSubimage(x, y, tileW, tileH));
			}
		}
		return tiles;
	}
	
	public static void patch(BufferedImage patch, BufferedImage target, int targetX, int targetY){
		int[] buf=new int[4];
		for(int y=0;y<patch.getHeight();y++){
			for(int x=0;x<patch.getWidth();x++){
				patch.getRaster().getPixel(x, y, buf);
				target.getRaster().setPixel(targetX+x, targetY+y, buf);
			}
		}
	}
	
	/**
	 * 把小图块拼合成整张图片
	 * @param tiles wh=(w*bpp)*h
	 * @param column 每一行几张tile
	 * @throws IOException
	 */
	public static BufferedImage jointTiles(List<BufferedImage> tiles, int column) throws IOException{
		return jointTiles(tiles, column, 0, null);
	}
	
	public static BufferedImage jointTiles(List<BufferedImage> tiles, int column, int marginRight, int[] bgColor) {
		BufferedImage firstImg=tiles.get(0);
		int tileW=firstImg.getWidth(), tileH=firstImg.getHeight();//assert all tiles has same width and height
		int jointH = tiles.size()/column*tileH;
		if(tiles.size()%column!=0) jointH+=tileH;
		BufferedImage joint = new BufferedImage(tileW*column+marginRight, jointH, firstImg.getType());
		WritableRaster jointRas = joint.getRaster();
		if(bgColor!=null){	//init bg color
			for(int y=0;y<joint.getHeight();y++){
				for(int x=0;x<joint.getWidth();x++){
					jointRas.setPixel(x, y, bgColor);
				}
			}
		}
		int[] buf=new int[tileW*tileH*4];
		int x=0,y=0,tileI=0;
		while(true){
			tiles.get(tileI).getRaster().getPixels(0, 0, tileW, tileH, buf);
			try {
				jointRas.setPixels(x, y, tileW, tileH, buf);
			} catch (Exception e) {
				e.printStackTrace();
			}
			tileI++;
			if(tileI>=tiles.size()) break;
			x+=tileW;
			if(x>=column*tileW){
				x=0;
				y+=tileH;
			}
		}
		return joint;
	}
	
	
	public static void main(String[] args) throws IOException {
	}
	
}

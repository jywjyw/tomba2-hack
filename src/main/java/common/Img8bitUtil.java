package common;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 1. one pixel occupy 8 bit
 * 2. display width is 2 times of vram width
 */
public class Img8bitUtil {
	
	public static BufferedImage readRomToBmp(InputStream in, int vramW, int vramH, Palette pal) throws IOException {
		int displayW = vramW*2;	//8bit下显示宽度*2;
		BufferedImage out = new BufferedImage(displayW, vramH, BufferedImage.TYPE_INT_RGB);
		readRomToImg(in, vramW, vramH, pal, displayW, out);
		return out;
	}
	
	public static BufferedImage readRomToPng32(InputStream in, int vramW, int vramH, Palette pal) throws IOException {
		int displayW = vramW*2;	//8bit下显示宽度*2;
		BufferedImage out = new BufferedImage(displayW, vramH, BufferedImage.TYPE_INT_ARGB);
		readRomToImg(in, vramW, vramH, pal, displayW, out);
		return out;
	}
	
	public static BufferedImage readPartToBmp(VramImg vram, int x, int y, int w, int h, Palette pal) throws IOException {
		if(x%2!=0 || w%2!=0) throw new UnsupportedOperationException("x or width must be even number");
		Palette pal256=Palette.init256();
		BufferedImage fullImg = readRomToBmp(new ByteArrayInputStream(vram.data), vram.w, vram.h, pal256);
		VramImg subImg = toColorIndexData(fullImg.getSubimage(x, y, w, h), pal256);
		return readRomToBmp(new ByteArrayInputStream(subImg.data), subImg.w, subImg.h, pal);
	}
	
	private static void readRomToImg(InputStream in, int vramW, int vramH, Palette pal, int displayW, BufferedImage out) throws IOException {
		WritableRaster raster = out.getRaster();
		byte[] buf = new byte[vramW*2];
		int x=0,y=0;
		boolean _break=false;
		while(true) {
			in.read(buf);
			for(byte b : buf) {
				int[] color =pal.getRgba8888Matrix()[b&0xff];
				raster.setPixel(x++, y, color);	//rgba
				if(x>=displayW) {
					x=0;
					y++;
					if(y>=vramH) {
						_break=true;
						break;
					}
				}
			}
			if(_break)break;
		}
	}
	
	//从paletta中查找精确的颜色
	public static VramImg toColorIndexData(BufferedImage png, Palette pal){
		return toColorIndexData(png, new PixelConverter(){
			@Override
			public int toPalIndex(int[] pixel) {
				return pal.getExactColorIndex(pixel[0], pixel[1], pixel[2]);
			}
		});
	}
	
	public static VramImg toColorIndexData(BufferedImage png, PixelConverter cb){
		ByteBuffer ret = ByteBuffer.allocate(png.getWidth()*png.getHeight());	
		int[] pixel=new int[3];
		for(int y=0;y<png.getHeight();y++){
			for(int x=0;x<png.getWidth();x++){	//8 bit image = 1 BytePerPixel
				png.getRaster().getPixel(x, y, pixel);
				ret.put((byte)cb.toPalIndex(pixel));
			}
		}
		return new VramImg(png.getWidth()/2, png.getHeight(), ret.array());//8bit模式下,图像显示宽度*2. 4bit下,图像显示宽度*4
	}
	
	
	/**
	 * 把32*32的小图块拼合成整张图片
	 * @param tiles wh=(32*bpp)*32
	 * @param num 每一行几张tile
	 * @throws IOException
	 */
	public static BufferedImage jointTiles(BufferedImage[] tiles, int num) throws IOException{
		int tileW=32*2;
		BufferedImage img = new BufferedImage(tileW*num, 32*tiles.length/num, tiles[0].getType());
		WritableRaster raster = img.getRaster();
		int[] buf=new int[tileW*32*4];
		int x=0,y=0,tileI=0;
		
		while(true){
			tiles[tileI].getRaster().getPixels(0, 0, tileW, 32, buf);
			raster.setPixels(x, y, tileW, 32, buf);
			
			tileI++;
			if(tileI>=tiles.length) break;
			x+=tileW;
			if(x>=img.getWidth()){
				x=0;
				y+=32;
			}
		}
		return img;
	}
	
	public static List<VramImg> splitToTiles(BufferedImage src, Palette pal){
		int tileW=32*2,i=0;
		List<VramImg> ret = new ArrayList<>();
		for(int y=0;y<src.getHeight();y+=32){
			for(int x=0;x<src.getWidth();x+=tileW){
				BufferedImage sub = src.getSubimage(x, y, tileW, 32);
//				try {
//					ImageIO.write(sub, "png", new File(Conf.desktop+"mec/"+(i++)+".png"));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				ret.add(toColorIndexData(sub, pal));
			}
		}
		return ret;
	}
}

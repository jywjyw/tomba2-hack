package common;


import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import common.VramImg.VramImg4bitWriter;

public class MultiLayerFontGen {
	
	public static void main(String[] args)  {
//		build4LayerFont();
	}
	
	public static byte[] build4LayerFont(MultiLayerChars charLayers, String fontFile){
		int charcount=charLayers.getCharLayer(0).size()*charLayers.chars.size(),fontsize=10;
		int w=256,layer=4,
			marginRight=w%fontsize,
			column=w/fontsize,
			charPerLayer=charcount/layer,
			h=(charPerLayer%column>0 ? charPerLayer/column+1 : charPerLayer/column) * fontsize;
		VramImg vram = new VramImg4bitWriter(w, h).build();
		for(int i=0;i<layer;i++){
			String chars=Util.join(charLayers.getCharLayer(i),"",null);
			List<BufferedImage> tiles=PixelFontGen.genBmpTiles(chars, fontFile, fontsize, 0);
			BufferedImage charimg=Img4bitUtil.jointTiles(tiles, column, marginRight, PixelFontGen.BACKGROUND_COLOR);
			vram = addLayer(charimg, vram, i+1);
		}
		return vram.data;
	}
	
	public static byte[] build4Palette(short fontColor){
		ByteBuffer allPal=ByteBuffer.allocate(32*4);
		short bgColor=0;
		ByteBuffer buf=ByteBuffer.allocate(32);
		for(int i=0;i<8;i++){
			buf.putShort(bgColor);
		}
		for(int i=8;i<16;i++){
			buf.putShort(fontColor);
		}
		allPal.put(buf.array());
		buf.clear();
		
		for(int i=0;i<2;i++){
			for(int j=0;j<4;j++) buf.putShort(bgColor);
			for(int k=0;k<4;k++) buf.putShort(fontColor);
		}
		allPal.put(buf.array());
		buf.clear();
		
		for(int i=0;i<4;i++){
			buf.putShort((short) 0);
			buf.putShort((short) 0);
			buf.putShort(fontColor);
			buf.putShort(fontColor);
		}
		allPal.put(buf.array());
		buf.clear();
		
		for(int i=0;i<8;i++){
			buf.putShort(bgColor);
			buf.putShort(fontColor);
		}
		allPal.put(buf.array());
		buf.clear();
		
		return allPal.array();
	}
	
	private static VramImg addLayer(BufferedImage img, VramImg lastLayer, int layerNum){
		Iterator<Byte> it=lastLayer.get4bitIterator();
		VramImg4bitWriter ret = new VramImg4bitWriter(img.getWidth(), img.getHeight());
		int[] pixel=new int[4];
		for(int y=0;y<img.getHeight();y++){
			for(int x=0;x<img.getWidth();x++){	
				img.getRaster().getPixel(x, y, pixel);
				byte last=it.next();
				if(pixel[0]==0){ //字体色
					ret.addPixelIndex(last | 1<<(4-layerNum));	//layerNum start from 1
				}else if(pixel[0]==255){ //背景色
					ret.addPixelIndex(last);
				}else{
					throw new RuntimeException();
				}
			}
		}
		return ret.build();
	}
	
}

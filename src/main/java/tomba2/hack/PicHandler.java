package tomba2.hack;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import common.Conf;
import common.Img4bitUtil;
import common.Img8bitUtil;
import common.MultiLayerFontGen;
import common.Palette;
import common.VramImg;
import common.VramImgUtil;
import tomba2.EncodingMenu;
import tomba2.ImgRebuilder.Callback;
import tomba2.Picpack;

public class PicHandler implements Callback {
	
	EncodingMenu encMenu;
	
	public PicHandler(EncodingMenu encMenu) {
		this.encMenu = encMenu;
	}

	@Override
	public void handle(List<Picpack> pics) throws IOException {
		byte[] menuFonts=MultiLayerFontGen.build4LayerFont(encMenu.chars, Conf.desktop+"Zfull-GB.ttf");
		insertMenuFont(pics, menuFonts);
		patchIcons(pics);
	}
	
	
	public void insertMenuFont(List<Picpack> pics, byte[] fonts) throws IOException{
		pics.get(25).modify(6, null, 2, 1, new byte[100]);//remove unused pic
		Picpack mypack = pics.get(0);
		int picInd=0;
		VramImg oldpic=mypack.uncompressOnePic(picInd);
		oldpic=replaceCharTiles(oldpic);
		int palcount=9, palBytes=32*4; //max count=16
		ByteBuffer newpic=ByteBuffer.allocate(oldpic.data.length+palcount*palBytes);
		newpic.put(fonts);
		byte[] oldpart=new byte[oldpic.data.length-fonts.length];
		System.arraycopy(oldpic.data, fonts.length, oldpart, 0, oldpart.length);
		newpic.put(oldpart);
		newpic.put(MultiLayerFontGen.build4Palette((short)0x1863)); //992-496: 18 63 FF 7F 73 4E  
		newpic.put(MultiLayerFontGen.build4Palette((short)0xdf01)); //497:DF 01 BF 02 18 01 
		newpic.put(MultiLayerFontGen.build4Palette((short)0xeb6e)); //498:EB 6E 6D 77 C7 55  
		newpic.put(MultiLayerFontGen.build4Palette((short)0x9b49)); //499:9B 49 DE 51 D3 2C  
		newpic.put(MultiLayerFontGen.build4Palette((short)0x4e37)); //500:4E 37 F2 47 AB 2A  
		newpic.put(MultiLayerFontGen.build4Palette((short)0x654d)); //501:65 4D C7 55 E3 44  
		newpic.put(MultiLayerFontGen.build4Palette((short)0x1042)); //502:10 42 94 52 8C 31  
		newpic.put(MultiLayerFontGen.build4Palette((short)0x4700)); //503:47 00 AA 00 A9 08  
		newpic.put(MultiLayerFontGen.build4Palette((short)0x4002)); //504:40 02 A0 02 C0 01  
		mypack.modify(picInd, mypack.getY(picInd), mypack.getW(picInd), mypack.getH(picInd)+palcount, newpic.array());
	}
	
	
	private VramImg replaceCharTiles(VramImg oldpic){
		try {
			Palette grey = Palette.init16Grey();
			BufferedImage pic = Img4bitUtil.readRomToBmp(new ByteArrayInputStream(oldpic.data), oldpic.w, oldpic.h, grey);
			List<BufferedImage> tiles = Img4bitUtil.splitToTiles(pic, 8, 8).subList(112, 112+96);
			int targetX=0,targetY=72;
			for(int i=0;i<tiles.size();i++){
				Img4bitUtil.patch(tiles.get(i), pic, targetX, targetY);
				targetX+=8;
				if(targetX>=256) {
					targetX=0;
					targetY+=8;
				}
			}
			return Img4bitUtil.toColorIndexData(pic, grey);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void patchIcons(List<Picpack> pics) throws IOException{
		Picpack pack1=pics.get(1);
		VramImg vram = pack1.uncompressOnePic(0);
		patchIcon(vram,"item.bmp","480-246",1,160,40);
		patchIcon(vram, "event.bmp", "480-247", 1, 96, 64);
		patchIcon(vram, "status.bmp", "480-245", 1, 152,56);
		patchIcon(vram, "blank.bmp", "480-245", 1, 0,64);
		patchIcon(vram, "help.bmp", "480-244", 1, 96,136);
		patchIcon(vram, "load.bmp", "496-216", 1, 24,48);
		patchIcon(vram, "save.bmp", "496-216", 1, 72,48);
		patchIcon(vram, "clear.bmp", "496-217", 1, 72,32);
		pack1.modify(0, pack1.getY(0), vram.w, vram.h, vram.data);
		
		Picpack pack2=pics.get(2);
		vram=pack2.uncompressOnePic(0);
		patchIcon(vram, "newgame.bmp", "880-509", 3, 0,1);
		patchIcon(vram, "continue.bmp", "880-510", 3, 64,1);
		patchIcon(vram, "start.bmp", "880-507", 3, 128,1);
		patchIcon(vram, "option.bmp", "880-508", 3, 184,1);
		VramImg patch=Img8bitUtil.toColorIndexData(ImageIO.read(new File(Conf.getRawFile("titlebg.bmp"))), new Palette(256, Conf.getRawFile("clut/titlebg.256")));
		VramImgUtil.patch(vram, 0, 0, patch);
		
		pack2.modify(0, pack2.getY(0), vram.w, vram.h, vram.data);
	}
	
	
	private void patchIcon(VramImg target, String bmp, String clut, int tp, int x, int y){
		int vramX=(tp*256+x)/4;
		try {
			VramImg patch = Img4bitUtil.toColorIndexData(
					ImageIO.read(new File(Conf.getRawFile(bmp))), 
					new Palette(16, Conf.getRawFile("clut/"+clut)));
			VramImgUtil.patch(target, vramX, y, patch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

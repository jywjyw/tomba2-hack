package tomba2.tool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import javax.imageio.ImageIO;

import common.Conf;
import common.Img4bitUtil;
import common.Img8bitUtil;
import common.Palette;
import common.Util;
import common.VramImg;
import tomba2.ImgRebuilder;
import tomba2.ImgRebuilder.Callback;
import tomba2.Picpack;
import tomba2.PicpackHeader;
import tomba2.Uncompressor;

public class ImgDumper {
//	String imgFile=Conf.jpdiskdir+"CD/TOMBA2.IMG";
	String imgFile=Conf.outdir+"TOMBA2.IMG";
	
	public static void main(String[] args) throws Exception {
		new ImgDumper().dumpIcons();
//		new ImgDumper().dumpAll();
	}
	
	//titlbg: tomba2.img 0x34800~0x46510
	public VramImg drawSingle(int entrance, int index) throws Exception {
		RandomAccessFile in=new RandomAccessFile(Conf.jpdiskdir+"CD/TOMBA2.IMG", "r");
		PicpackHeader header=seekPicOffset(in, entrance, index);
		
		byte[] comp=new byte[header.size[index]];
		in.read(comp);
		ByteArrayOutputStream uncomp = new ByteArrayOutputStream();
		Uncompressor.uncompress(new ByteArrayInputStream(comp), header.w[index], uncomp);
		
		FileOutputStream fff=new FileOutputStream(Conf.desktop+"smoke-uncomp");
		fff.write(uncomp.toByteArray());
		fff.close();
		
//		Palette pal = new Palette(16, Conf.getRawFile("clut/grey.16"));
//		BufferedImage img = Img4bitUtil.readRomToPng32(new ByteArrayInputStream(uncomp.toByteArray()), header.w[index],header.h[index], pal);
//		ImageIO.write(img, "png", new File(Conf.desktop+"test.png"));
		
		return new VramImg(header.w[index],header.h[index], uncomp.toByteArray());
	}
	
	public static PicpackHeader seekPicOffset(RandomAccessFile img, int entrance, int index) throws IOException{
		img.seek(entrance);
		PicpackHeader header=PicpackHeader.load(img);
		int picOffset=entrance+0x800;
		for(int i=0;i<index;i++){
			picOffset+=header.size[i];
		}
		img.seek(picOffset);
		return header;
	}
	
	public void dumpAll() throws Exception  {
		RandomAccessFile in=new RandomAccessFile(imgFile, "r");
		Palette pal = new Palette(16, Conf.getRawFile("clut/grey.16"));
		Palette pal256 = new Palette(256, Conf.getRawFile("clut/titlebg.256"));
		int picIndex=1;
		while(true){
			int entrance=(int) in.getFilePointer();
			PicpackHeader header=PicpackHeader.load(in);
			for(int i=0;i<header.count;i++){
				String pic = String.format("%d_%06X_%d_%06X_%dx%d_%dx%d", 
							picIndex++,entrance,i,in.getFilePointer(),header.x[i],header.y[i],header.w[i],header.h[i]);
				ByteArrayOutputStream uncomp = new ByteArrayOutputStream();
				byte[] input=new byte[header.size[i]];
				in.read(input);
				Uncompressor.uncompress(new ByteArrayInputStream(input), header.w[i], uncomp);
				BufferedImage img = Img8bitUtil.readRomToPng32(new ByteArrayInputStream(uncomp.toByteArray()), 
						header.w[i],header.h[i], pal256);
				ImageIO.write(img, "png", new File(Conf.desktop+"dump/"+pic+".png"));
				in.seek(Util.align800H((int)in.getFilePointer()));
			}
		}
	}
	
	public void dumpIcons() throws IOException{
		ImgRebuilder.splitAndRebuild(new Callback() {
			@Override
			public void handle(List<Picpack> pics) throws IOException {
				String dir=Conf.desktop+"tomba2pic/";
				VramImg vram = pics.get(1).uncompressOnePic(0);
				dumpIcon(vram, 1, 160,40,64,16, "480-246", dir+"item.bmp");
				dumpIcon(vram, 1, 96,64,56,16, "480-247", dir+"event.bmp");
				dumpIcon(vram, 1, 152,56,72,16, "480-245", dir+"status.bmp");
				dumpIcon(vram, 1, 96,136,48,16, "480-244", dir+"help.bmp");
				
				dumpIcon(vram, 1,24,48,48,16, "496-216", dir+"load.bmp");
				dumpIcon(vram, 1,72,48,48,16, "496-216", dir+"save.bmp");
				dumpIcon(vram, 1,72,32,72,16, "496-217", dir+"clear.bmp");
				
				vram=pics.get(2).uncompressOnePic(0);
				dumpIcon(vram, 3,   0,1,64,16, "880-509", dir+"newgame.bmp");
				dumpIcon(vram, 3,  64,1,64,16, "880-510", dir+"continue.bmp");
				dumpIcon(vram, 3, 128,1,56,16, "880-507", dir+"start.bmp");
				dumpIcon(vram, 3, 184,1,64,16, "880-508", dir+"option.bmp");
				
				BufferedImage titlebg=Img8bitUtil.readPartToBmp(vram,0,0,320,240, new Palette(256,Conf.getRawFile("clut/titlebg.256")));
				ImageIO.write(titlebg, "bmp", new File(dir+"titlebg.bmp"));
			}
		});
	}
	
	private void dumpIcon(VramImg vram, int tp, int x, int y, int w, int h, String clut, String targetFile) throws IOException{
		int offsetX=tp*64*4;
		BufferedImage img=Img4bitUtil.readPartToBmp(vram, offsetX+x,y,w,h, new Palette(16, Conf.getRawFile("clut/"+clut)));
		ImageIO.write(img, "bmp", new File(targetFile));
	}
}

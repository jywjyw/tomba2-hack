package tomba2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import common.Util;
import common.VramImg;
import tomba2.hack.Compressor;

/**
 * 图片包, 包含0x800大小的图像头,和N个压缩图像体
 */
public class Picpack {
	private PicpackHeader header;
	private List<byte[]> compPics;
	
	public static Picpack load(RandomAccessFile imgFile, int filePos) throws IOException{
		imgFile.seek(filePos);
		Picpack p=new Picpack();
		p.header=PicpackHeader.load(imgFile);
		p.compPics=new ArrayList<>();
		for(int i=0;i<p.header.count;i++){
			byte[] comp=new byte[p.header.size[i]];
			imgFile.read(comp);
			p.compPics.add(comp);
		}
		return p;
	}
	
	public void modify(int index, Integer y, int w, int h, byte[] uncompPic){
		ByteArrayOutputStream comp=new ByteArrayOutputStream();
		try {
			new Compressor().compress(w, new ByteArrayInputStream(uncompPic), comp);
			comp.write(new byte[Util.get0x800MultipleDiff(comp.size())]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		compPics.set(index, comp.toByteArray());
//		if(x!=null) header.x[index]=x;
		if(y!=null) header.y[index]=y;
		header.w[index]=w;
		header.h[index]=h;
		header.size[index]=Util.align800H(comp.size());
	}
	
	public VramImg uncompressOnePic(int index){
		ByteArrayOutputStream uncomp = new ByteArrayOutputStream();
		Uncompressor.uncompress(new ByteArrayInputStream(compPics.get(index)), header.w[index], uncomp);
		return new VramImg(header.w[index],header.h[index], uncomp.toByteArray());
	}
	
	
	public byte[] rebuild(){
		int bodyCapacity=0;
		for(int i=0;i<header.count;i++){
			bodyCapacity+=header.size[i];
		}
		ByteBuffer compBodies=ByteBuffer.allocate(bodyCapacity);
		for(int i=0;i<compPics.size();i++){
			compBodies.put(compPics.get(i));
		}
		byte[] headerBs=header.rebuild();
		ByteBuffer ret=ByteBuffer.allocate(headerBs.length+bodyCapacity);
		ret.put(headerBs);
		ret.put(compBodies.array());
		return ret.array();
	}
	
	public int getH(int index){
		return header.h[index];
	}
	public int getW(int index) {
		return header.w[index];
	}
	public int getX(int index) {
		return header.x[index];
	}
	public int getY(int index) {
		return header.y[index];
	}
}

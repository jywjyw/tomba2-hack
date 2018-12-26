package common.gpu.primitive;

import java.io.IOException;
import java.io.RandomAccessFile;

import common.Conf;

public class GpuCmdParser {
	
	public static void main(String[] args) throws IOException {
		RandomAccessFile f=new RandomAccessFile(Conf.desktop+"menu_ram.bin", "r");
		f.seek(5);
//		System.out.println(Util.hexEncode(buf));
	}
	
	public static GpuCmd read(RandomAccessFile file) throws IOException {
		byte[] first=new byte[4];
		file.read(first);
		byte[] bs;
		switch(first[3]){
			case Sprite.CMD:
				bs = new byte[Sprite.SIZE];
				System.arraycopy(first, 0, bs, 0, first.length);
				file.read(bs, 3, bs.length-first.length);
				return new Sprite(bs);
			case DrawMode.CMD:
				return new DrawMode(first);
			default:
				throw new UnsupportedOperationException();
		}
	}
	
}

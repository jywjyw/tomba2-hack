

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import common.Conf;
import common.Img8bitUtil;
import common.Palette;
import common.Util;
import tomba2.Uncompressor;

public class UncompressorTest {
	
	//titlbg: tomba2.img 0x34800~0x46510
	public static void main(String[] args) throws Exception {
		File outfile=new File(Conf.desktop+"uncomp");
		FileOutputStream out = new FileOutputStream(outfile);
		FileInputStream in = new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("titlebg-256-comp").getFile());
		Uncompressor.uncompress(in, 256, out);
		out.close();
		in.close();
		System.out.println(Util.md5(outfile));
		System.out.println(Util.md5(new File(Thread.currentThread().getContextClassLoader().getResource("titlebg-256-uncomp").getFile())));
		
		FileInputStream uncompIs=new FileInputStream(outfile);
		BufferedImage img = Img8bitUtil.readRomToPng32(uncompIs, 256,240, new Palette(256, Conf.getRawFile("clut/titlebg.256")));
		ImageIO.write(img, "png", new File(Conf.desktop+"block.png"));
		uncompIs.close();
	}
	
}

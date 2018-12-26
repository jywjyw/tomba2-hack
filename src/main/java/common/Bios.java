package common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

public class Bios {
	
	private String biosFile;
	
	public Bios(String biosFile) {
		this.biosFile = biosFile;
	}
	
	//从日文汉字区开始擦除
	public void eraseFromKanji(byte[] data) throws IOException{
		RandomAccessFile outBiosFile = new RandomAccessFile(biosFile, "rw");
		outBiosFile.seek(0x69d68);
		outBiosFile.write(data);
		outBiosFile.close();
	}

	public void saveAsBmp(String bmpTarget) throws Exception {
		saveAsBmp(0x66000, bmpTarget);
	}
	
	public void saveAsBmp(int pos, String bmpTarget) throws Exception {
		RandomAccessFile bios = new RandomAccessFile(biosFile, "r");
		bios.seek(pos);
		int charW = 16, charH = 15;
		int charCount = 3489, imgh = charCount*charH;//从第524个字符开始为汉字区
		BufferedImage img = new BufferedImage(charW, imgh, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, charW, imgh);
		
		int buf=0;
		try {
			for(int i=0;i<imgh;i++) {
				buf = bios.readUnsignedShort();
				int x=0;
				for(int j=charW-1;j>=0;j--) {//每2个字节为字符的一行像素点，将其展开为二进制，1黑0白
					int point = buf>>>j&1;
					int color = point==1?Color.BLACK.getRGB() : Color.WHITE.getRGB();
					img.setRGB(x++, i, color);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		bios.close();
		ImageIO.write(img, "bmp", new File(bmpTarget));
	}
	
	//save as PSP bios font(used by CheatMaster Fusion e.g.)
	public void saveAsPSPFont(String fnt){
		byte[] font=new byte[3489*30]; //3489 chars
		try {
			FileInputStream bios=new FileInputStream(biosFile);
			bios.skip(0x66000);
			bios.read(font);
			bios.close();
			FileOutputStream fos=new FileOutputStream(fnt);
			fos.write(font);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

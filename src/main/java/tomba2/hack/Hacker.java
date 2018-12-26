package tomba2.hack;

import java.io.File;
import java.io.RandomAccessFile;

import common.Bios;
import common.Charset;
import common.Conf;
import common.PixelFontGen;
import common.Util;
import tomba2.EncodingLarge;
import tomba2.EncodingMenu;
import tomba2.ImgRebuilder;

public class Hacker {
	
	public static void main(String[] args) throws Exception {
		EncodingLarge encLarge = Charset.loadJp().getNonKanjiTable();
		File excel=ExcelFileFinder.getNewestTranslation("tomba2-jp");
		
		String sop=Conf.outdir+"SOP.BIN";
		Util.copyFile(Conf.getJpSop(), sop);
		new SOPImporter(encLarge).import_(excel, sop);
		
		String dat=Conf.outdir+"TOMBA2.DAT";
		Util.copyFile(Conf.getJpDat(), dat);
		new DatMultilineImporter(encLarge).import_(excel, dat);
		
		String exefile=Conf.outdir+"MAIN.EXE";
		Util.copyFile(Conf.getJpExe(), exefile);
		RandomAccessFile exe=new RandomAccessFile(exefile, "rw");
		new ExeLargeFontImporter(encLarge).import_(excel, exe);
		
		String bios = Conf.outdir+"tomba2-ZH-bios.BIN";
		Util.copyFile(Conf.getRawFile("SCPH1001.BIN"), bios);
		String chinese=encLarge.getAllChinese();
		if(chinese.length()>0){
			byte[] font=PixelFontGen.genSingleColorFont(chinese, Conf.getRawFile("方正像素15.TTF"), 15, 1);//bios font wh=16*15
			new Bios(bios).eraseFromKanji(font);
			new Bios(bios).saveAsPSPFont(Conf.outdir+"tomba2.fnt");
		}
		
		long curUVspaceOffset=MenuFontAsmHack.modify(exe);
		EncodingMenu encMenu=new EncodingMenu();
		new ExeMenuFontImporter(encMenu, curUVspaceOffset).import_(excel, exe);
		new DatAlertImporter(encMenu).import_(excel, dat);
//		encMenu.debug(Conf.desktop+"新菜单码表.tbl");
		if(encMenu.size()>600){
			encMenu.printOnceCharCount();
			throw new UnsupportedOperationException("菜单字库超了,"+encMenu.size());
		}
		ImgRebuilder.splitAndRebuild(new PicHandler(encMenu));
		
		exe.close();
		System.out.println("finished. use jpsxdec and isopatch next.");
	}

}

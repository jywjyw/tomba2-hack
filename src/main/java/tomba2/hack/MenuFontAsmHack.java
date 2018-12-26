package tomba2.hack;

import java.io.IOException;
import java.io.RandomAccessFile;

import common.Conf;
import common.instruction.MipsCompiler;

public class MenuFontAsmHack {
	
	public static long modify(RandomAccessFile exe) throws IOException{
		MipsCompiler mips = new MipsCompiler();
		
		exe.seek(Conf.getExeOffset(0x8004f374));
		exe.write(10); //change alert char width to 10
		exe.seek(Conf.getExeOffset(0x8004f410));
		exe.write(mips.compileResource("build_alert.asm"));
		
		exe.seek(Conf.getExeOffset(0x8004f534));
		exe.write(mips.compileResource("show_alert.asm"));
		
		//see 4layerfont.asm
		exe.seek(Conf.getExeOffset(0x80079304));
		exe.write(mips.compileLine("j 80079358"));
		exe.seek(Conf.getExeOffset(0x800792f0));
		exe.write(mips.compileLine("j 80079358"));
		exe.seek(Conf.getExeOffset(0x8007928c));
		exe.write(mips.compileLine("addiu t1,v1,0"));
		exe.seek(Conf.getExeOffset(0x8007943c));
		exe.write(mips.compileLine("j 800a6fbc"));
		
//		exe.seek(Conf.getExeOffset(0x80079558));
//		exe.write(0x1E);  //menu font TexturePage
		
		exe.seek(Conf.getExeOffset(0x80079C58));
		exe.write(0xff);  //小字库图片中, 从!到~的字符有2种,一种是内码+4f,另一种是内码+ff,由于第1种被新字体覆盖,需要修改成第2种,把4f改成ff
		
		exe.seek(Conf.getExeOffset(0x8003805c));
		exe.writeShort((short)0);	//align item title height. 物品界面中起始Y值有偏移量,要去掉
		exe.seek(Conf.getExeOffset(0x8003946c));
		exe.write(0x3f);	//modify event title height 1.
		exe.seek(Conf.getExeOffset(0x80039674));
		exe.write(0x3f);	//modify event title height 2.
		
		exe.seek(Conf.getExeOffset(0x800a6fbc));
		byte[] asm=mips.compileResource("4layerfont.asm");
		if(asm.length>Conf.UV_SPACE)
			throw new UnsupportedOperationException("asm is too large");
		exe.write(asm);
		
		return exe.getFilePointer();
	}

}

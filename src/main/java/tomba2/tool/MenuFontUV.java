package tomba2.tool;

import common.Util;
import common.gpu.primitive.ClutId;

public class MenuFontUV {
	public static void main(String[] args) {
		printNewUV();
	}
	
	public static void printUV(){
		int code=0x7e-0x4f+0xff; //uvcode=code+4f
		int u=(code&0x1f)*8;
		int v=(code>>>5)<<3;
		System.out.printf("(%d,%d)\n",u,v);
	}
	
	public static void printNewUV(){
		int code=0x8155;
		int clut=Util.hiloShort(0x3c78)+((code>>>8)&0xf);
		int u=(code&0x1f)*10;
		int v=((code&0xff)>>5)*10;
		System.out.printf("clut=%s,uv=(%d,%d)(%x,%x)\n",new ClutId((short)clut),u,v,u,v);
	}

}

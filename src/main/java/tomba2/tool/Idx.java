package tomba2.tool;

import java.io.IOException;
import java.io.RandomAccessFile;

import common.Conf;
import common.Util;

public class Idx {
	public static void main(String[] args) throws IOException {
		RandomAccessFile f=new RandomAccessFile(Conf.jpdiskdir+"CD/TOMBA2.IDX", "r");
		int i=0;
		while(true){
			try {
				f.seek(i*0x800);
				int a=Util.hilo(f.readInt());
				int b=Util.hilo(f.readInt());
				System.out.printf("%X,%X\n",a,b);
				i++;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		f.close();
	}

}

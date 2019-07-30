package tomba2.tool;

import java.io.IOException;
import java.io.RandomAccessFile;

import common.Conf;

public class PljTest {
	
	public static void main(String[] args) throws IOException {
		int[] a=new int[0x100],b=new int[0x100];
		for(int i=0;i<a.length;i++){
			a[i]=i;
			b[i]=i;
		}
		RandomAccessFile f=new RandomAccessFile(Conf.desktop+"plj/DATA/TALK0.BPE", "r");
		f.seek(0xe818);
		int offset=0;
		while(true){
			int r=f.read();
			if(r>=0x80){
				r-=0x7f;
				offset+=r;
				if(offset>=0x100) break;
				a[offset]=f.read();
				System.out.printf("%02x=%02x\n",offset,a[offset]);
				if(a[offset]!=offset){
					b[offset]=f.read();
				}
				offset++;
			}else{
				for(int i=0;i<=r;i++){
					a[offset]=f.read();
					System.out.printf("%02x____%02x\n",offset,a[offset]);
					if(a[offset]!=offset){
						b[offset]=f.read();
					}
					offset++;
				}
			}
		}
		f.close();
		
		for(int i=0;i<a.length;i++){
			System.out.printf(" %02X",a[i]);
		}
		System.out.println();
		for(int i=0;i<a.length;i++){
			System.out.printf(" %02X",b[i]);
		}
	}

}

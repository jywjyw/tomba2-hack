package tomba2;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import common.Util;

public class Uncompressor {
	
	public static void uncompress(InputStream comp, int imgW, OutputStream uncomp){
		BufferWindow win = new BufferWindow(imgW*2);
		byte[] next;
		while(true){
			try {
				int i=comp.read();
				int len=(i&0xff)>>>3;
				if(len==0) {
					break;
				}
				int posFlag=i&7;
//				System.out.printf("(%2d,%d) ",len,posFlag);
				if(posFlag==0){//不重编码,原样输出
					next=new byte[len];
					comp.read(next);
				} else if(posFlag==1){
					next = win.getRecent(len);
				} else {
					int pos=0;
					////if(posFlag<=0) branch 80045648
					if(posFlag==2) pos=-imgW*2;
					else if(posFlag==3) pos=-imgW*2-1;
					else if(posFlag==4) pos=-imgW*2-2;
					else if(posFlag==5) pos=-imgW*2-3;
					else if(posFlag==6) pos=-imgW*2+1;
					else if(posFlag==7) pos=-imgW*2+2;
					else {
						throw new UnsupportedOperationException();
					}
					next=win.get(pos, len);
				}
//				System.out.println(" => "+Util.hexEncode(next));
				win.push(next);
				uncomp.write(next);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	private static class BufferWindow {
		
		private LinkedList<Byte> q = new LinkedList<>();
		
		public BufferWindow(int bufferSize) {
			for(int i=0;i<bufferSize+3;i++){
				this.q.add(null);
			}
		}
		
		public void push(byte[] bs){
			for(byte b:bs){
				q.pop();
				q.addLast(b);
			}
		}
		
		public byte[] get(int offset, int len){
			int pos=offset+q.size();
			List<Byte> sub = q.subList(pos, pos+len);
			byte[] ret=new byte[sub.size()];
			for(int i=0;i<sub.size();i++){
				ret[i]=sub.get(i);
			}
			return ret;
		}
		
		public byte[] getRecent(int num){
			byte[] ret=new byte[num];
			Arrays.fill(ret, q.getLast());
			return ret;
		}

	}
}

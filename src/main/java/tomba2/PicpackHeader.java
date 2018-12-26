package tomba2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import common.Util;

public class PicpackHeader{
	public int count;
	public int[] x,y,w,h,size; //size必须是光盘扇区的整数倍,最终要转换为扇区号去光盘加载
	
	public static PicpackHeader load(RandomAccessFile in) throws IOException{
		PicpackHeader header=new PicpackHeader();
		header.count=Util.hilo(in.readInt());
		header.x=new int[header.count];
		header.y=new int[header.count];
		header.w=new int[header.count];
		header.h=new int[header.count];
		header.size=new int[header.count];
		for(int i=0;i<header.count;i++){
			header.x[i]=Util.hiloShort(in.readShort());
			header.y[i]=Util.hiloShort(in.readShort());
			header.w[i]=Util.hiloShort(in.readShort());
			header.h[i]=Util.hiloShort(in.readShort());
			header.size[i]=Util.hilo(in.readInt());
		}
		in.seek(Util.align800H((int) in.getFilePointer()));
		return header;
	}
	
	public byte[] rebuild(){
		ByteBuffer headerBuf=ByteBuffer.allocate(0x800);
		headerBuf.order(ByteOrder.LITTLE_ENDIAN);
		headerBuf.putInt(count);
		for(int i=0;i<count;i++){
			headerBuf.putShort((short)x[i]);
			headerBuf.putShort((short)y[i]);
			headerBuf.putShort((short)w[i]);
			headerBuf.putShort((short)h[i]);
			headerBuf.putInt(size[i]);
		}
		return headerBuf.array();
	}
	
}
package tomba2.hack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * variation from LZ77
 */
public class Compressor {
	int inputLen=0, outLen=0;
	
	/**
	 * 
	 * @param imgW img vram width
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public void compress(int imgW, InputStream is, OutputStream os) throws IOException{
		ForwardWindow forward=new ForwardWindow(is);
		BackWindow back = new BackWindow(imgW);
		TupleOutputStream tupleOs=new TupleOutputStream();
		while(forward.size()>0){
			compareTail(back, forward);
			
			Byte newest=back.getNewest();
			int maxHeadSameLen=0;
			if(newest!=null){
				maxHeadSameLen=compareHead(newest, forward);
			}
			
			if(tailLen<=1 && maxHeadSameLen<=1){
				byte[] raw=forward.load(is,1);
				back.load(raw);
				tupleOs.addRawByte(raw[0]);
			} else if(tailLen>maxHeadSameLen){
				tupleOs.addTailRepeat(tailLen,toPosFlag(tailOffset, imgW));
				back.load(forward.load(is,tailLen));
			} else if(tailLen<maxHeadSameLen){
				tupleOs.addRepeatByte(maxHeadSameLen, newest);
				back.load(forward.load(is,maxHeadSameLen));
			}
		}
		
		handleRawTuple(new TupleInputStream(tupleOs.toBytes()), os);
//		System.out.printf("compress result: before(%dB), after(%dB), rate(%d%%)\n",inputLen, outLen, (int)(outLen/(float)inputLen*100));
	}
	
	
	private int tailLen=0,tailOffset=0;
	private void compareTail(BackWindow back, ForwardWindow forward){
//		for(int offset=2;offset>=-3;offset--){
//			int backInd=offset,forwardInd=0;
//			int len=0;
//			while(forwardInd<forward.capacity){
//				Byte a=back.getByOffset(backInd++);
//				if(a==null) return;
//				Byte b=forward.get(forwardInd++);
//				if(a==b){
//					len++;
//				} else {
//					tailLen=len;
//					tailOffset=offset;
//					break;
//				}
//			}
//		}
	}
	
	private int toPosFlag(int tailOffset, int imgW){
		return 0;
//		if(posFlag==2) pos=-imgW*2;
//		else if(posFlag==3) pos=-imgW*2-1;
//		else if(posFlag==4) pos=-imgW*2-2;
//		else if(posFlag==5) pos=-imgW*2-3;
//		else if(posFlag==6) pos=-imgW*2+1;
//		else if(posFlag==7) pos=-imgW*2+2;
	}
	
	private int compareHead(byte newest, ForwardWindow forward){
		int len=0;
		for(Iterator<Byte> it=forward.iterator();it.hasNext();){
			Byte b=it.next();
			if(b==newest) 
				len++;
			else 
				break;
		}
		return len;
	}
	
	private void handleRawTuple(TupleInputStream tupleIs, OutputStream os) throws IOException{
		ByteBuffer rawBuf=ByteBuffer.allocate(31);
		Tuple t=tupleIs.readTuple();
		rawBuf.put(t.getRawByte());	//assert first is raw
		boolean lastIsRaw=true;
		while(true){
			try {
				t = tupleIs.readTuple();
			} catch (Exception e) {
				break;
			}
			if(t.isRaw()){
				if(!rawBuf.hasRemaining()){
					writeBuf(rawBuf, os);
				}
				rawBuf.put(t.getRawByte());
				lastIsRaw=true;
			} else {
				if(lastIsRaw){
					writeBuf(rawBuf, os);
				}
				os.write(t.getIndicator());
				outLen+=1;
				lastIsRaw=false;
			}
		}
		os.write(0);
		outLen+=1;
	}
	
	private void writeBuf(ByteBuffer rawBuf, OutputStream os) throws IOException{
		int indicator=rawBuf.position()<<3;
		if(indicator>>>3==0) {
//			System.out.println();
			throw new RuntimeException();
		}
		os.write(indicator);
		outLen+=1;
		os.write(Arrays.copyOf(rawBuf.array(),rawBuf.position()));
		outLen+=rawBuf.position();
		rawBuf.clear();
	}
	
	private class TupleOutputStream{
		ByteArrayOutputStream os=new ByteArrayOutputStream();

		public void addRawByte(byte b) {
			try {
				os.write(new byte[]{0,b});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void addRepeatByte(int len, byte b) {
			try {
				os.write(new byte[]{(byte)(len<<3|1&0xff), b});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void addTailRepeat(int len, int posFlag) {
			
		}
		
		public byte[] toBytes(){
			return os.toByteArray();
		}
	}
	
	private class TupleInputStream{
		ByteArrayInputStream is;
		
		public TupleInputStream(byte[] bs) {
			this.is = new ByteArrayInputStream(bs);
		}

		public Tuple readTuple() throws EOFException  {
			int read=is.read();
			if(read==-1) throw new EOFException();
			byte a = (byte)(read&0xff);
			int posFlag=a&7;
			if(posFlag==0 || posFlag==1){
				byte b=(byte)(is.read()&0xff);
				return new Tuple(new byte[]{a,b});
			} else {
				return new Tuple(new byte[]{a});
			}
		}
	}
	
	private class Tuple{
		private byte[] bs;
		public Tuple(byte[] bs) {
			this.bs = bs;
		}

		public boolean isRaw() {
			int pos=bs[0]&7;
			return pos==0;
		}
		
		public byte getIndicator(){
			return bs[0];
		}

		public byte getRawByte() {
			return bs[1];
		}
		
	}
	
	private class ForwardWindow implements Iterable<Byte>{
		LinkedList<Byte> q=new LinkedList<>();
		private int capacity=31, size=capacity;
		
		public ForwardWindow(InputStream is) throws IOException{
			byte[] buf=new byte[capacity];
			int len=is.read(buf);
			inputLen+=capacity;
			if(len!=capacity) throw new RuntimeException();
			for(int i=0;i<buf.length;i++){
				q.add(buf[i]);
			}
		}
		
		public byte[] load(InputStream is, int len) throws IOException {
			byte[] out=new byte[len];
			for(int i=0;i<len;i++){
				int read=is.read();
				out[i]=q.pop();
				if(read==-1){
					size--;
				} else {
					q.addLast((byte)(read&0xff));
					inputLen++;
				}
			}
			return out;
		}
		
		public byte get(int index){
			return q.get(index);
		}

		public int size() {
			return size;
		}
		
		@Override
		public Iterator<Byte> iterator() {
			return q.iterator();
		}
	}
	
	private class BackWindow{
		LinkedList<Byte> q=new LinkedList<>();
		
		public BackWindow(int imgW){
			for(int i=0;i<imgW*2+3;i++){	//back buffer is twice of img vram width
				q.add(null);
			}
		}
		public Byte getByOffset(int offset) {
			return q.get(offset+3);
		}
		
		public void load(byte[] bs){
			for(byte b:bs){
				q.pop();
				q.addLast(b);
			}
		}
		public Byte getNewest() {
			return q.getLast();
		}
	}

}

package common;

import java.nio.ByteBuffer;

public class VramImgUtil {
	
	public static void patch(VramImg target, int x, int y, VramImg patch){
		if(x+patch.w > target.w || y+patch.h > target.h){
			throw new UnsupportedOperationException("x or y is illegal");
		}
		int bpp=2;//Bytes Per Pixel
		byte[] buf = new byte[patch.w*bpp]; 
		ByteBuffer patchWrapper=ByteBuffer.wrap(patch.data);
		ByteBuffer targetWrapper=ByteBuffer.wrap(target.data);
		for(int i=0;i<patch.h;i++){
			patchWrapper.get(buf);
			int pos=((y+i)*target.w+x)*bpp;
			targetWrapper.position(pos);
			targetWrapper.put(buf);
		}
	}

}

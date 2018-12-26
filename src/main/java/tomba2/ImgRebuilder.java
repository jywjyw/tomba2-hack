package tomba2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import common.Conf;
import common.RscLoader;
import common.Util;

/**
 * rebuild .IDX & .IMG
 */
public class ImgRebuilder {
	
	public static void splitAndRebuild(Callback cb) throws IOException {
		List<int[]> entrances=new ArrayList<>();
		RscLoader.load("tomba2.idx", "ISO-8859-1", new RscLoader.Callback() {
			@Override
			public void doInline(String line) {
				String[] arr=line.split(",");
				entrances.add(new int[]{Integer.parseInt(arr[0],16), Integer.parseInt(arr[1],16)});
			}
		});
		
		RandomAccessFile f=new RandomAccessFile(Conf.jpdiskdir+"CD/TOMBA2.IMG", "r");
		List<Picpack> pics=new ArrayList<>();
		for(int[] ints:entrances){
			if(ints[0]!=0 || ints[1]!=0)
				pics.add(Picpack.load(f, ints[0]));
		}
		f.close();
		
		cb.handle(pics);
		
		String img=Conf.outdir+"TOMBA2.IMG";
		Util.copyFile(Conf.jpdiskdir+"CD/TOMBA2.IMG", img);
		build(entrances, pics, new File(img));
	}
	
	public interface Callback{
		void handle(List<Picpack> pics) throws IOException;
	}

	private static void build(List<int[]> entrances, List<Picpack> pics, File imgFile) throws IOException{
		String idxPath=Conf.outdir+"TOMBA2.IDX";
		Util.copyFile(Conf.jpdiskdir+"CD/TOMBA2.IDX", idxPath);
		RandomAccessFile idx=new RandomAccessFile(idxPath, "rw");
		FileOutputStream img=new FileOutputStream(imgFile);
		int picpackInd=0,lastAddr=0;
		for(int i=0;i<entrances.size();i++){
			int[] ints=entrances.get(i);
			if(ints[0]!=0 || ints[1]!=0){
				byte[] bs=pics.get(picpackInd++).rebuild();
				img.write(bs);
				idx.seek(i*0x800);
				idx.writeInt(Util.hilo(lastAddr));
				idx.writeInt(Util.hilo(lastAddr+=bs.length));
			}
		}
		idx.close();
		img.close();
		if(imgFile.length()>6291456)
			throw new RuntimeException("img超长");
	}
	
}

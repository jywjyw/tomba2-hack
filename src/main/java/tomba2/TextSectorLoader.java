package tomba2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import common.RscLoader;
import common.RscLoader.Callback;

public class TextSectorLoader implements Iterable<TextSector> {
	
	List<TextSector> sectors=new ArrayList<>();
	
	public static TextSectorLoader loadJp(){
		return new TextSectorLoader("text_addr_jp.properties");
	}
	
	public static TextSectorLoader loadEn(){
		return new TextSectorLoader("text_addr_en.properties");
	}
	
	private TextSectorLoader(String file){
		RscLoader.load(file, "ascii", new Callback() {
			@Override
			public void doInline(String line) {
				String[] arr=line.split(",");
				TextSector ts=new TextSector();
				ts.addr=Integer.parseInt(arr[0], 16);
				for(int i=1;i<arr.length;i++){
					ts.sameTextAddr.add(Integer.parseInt(arr[i], 16));
				}
				sectors.add(ts);
			}
		});
	}

	@Override
	public Iterator<TextSector> iterator() {
		return sectors.iterator();
	}
	
}

package common;

import java.util.ArrayList;
import java.util.List;

public class MultiLayerChars {
	public List<List<String>> chars=new ArrayList<>();
	int arrInd=0;
	
	public MultiLayerChars(){
		for(int i=0;i<4;i++){
			chars.add(new ArrayList<>());
		}
	}
	
	public void put(String c){
		chars.get(arrInd).add(c);
		arrInd++;
		if(arrInd==4){
			arrInd=0;
		}
	}
	
	public List<String> getCharLayer(int i){
		return chars.get(i);
	}

}

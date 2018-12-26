package tomba2;

import java.util.LinkedHashSet;
import java.util.Set;

public class TextSector{
	public int addr;
	public int size;	//byte size
	public byte[] md5;
	public Set<Integer> sameTextAddr=new LinkedHashSet<>();	//与之重复的文本区地址
}
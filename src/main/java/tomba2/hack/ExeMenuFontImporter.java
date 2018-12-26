package tomba2.hack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Conf;
import common.ExcelParser;
import common.ExcelParser.RowCallback;
import common.RscLoader;
import common.RscLoader.Callback;
import common.Util;
import tomba2.EncodingMenu;

public class ExeMenuFontImporter {
	
	StringBuilder err = new StringBuilder();
	Set<String> chars=new HashSet<>();
	EncodingMenu enc;
	Map<Integer,Integer> specialEvent=new HashMap<>();
	long smallEventTextExtensionOffset;
	
	public ExeMenuFontImporter(EncodingMenu enc, long smallEventTextExtensionOffset) {
		this.enc=enc;
		this.smallEventTextExtensionOffset=smallEventTextExtensionOffset;
		RscLoader.load("special_event.properties", "ascii", new Callback() {
			@Override
			public void doInline(String line) {
				String[] arr=line.split("=");
				int menuPointer=Integer.parseUnsignedInt(arr[1].split(",")[1],16);
				specialEvent.put(Integer.parseInt(arr[0],16), menuPointer);
			}
		});
	}

	public void import_(File excel, RandomAccessFile exe) throws IOException{
		new ExcelParser(excel).parse("MAIN.EXE", 2, new RowCallback() {
			@Override
			public void doInRow(List<String> strs, int rowNum) {
				boolean isSmallText=getCell(strs, 4).equals("2");
				int exeOffset=Integer.parseInt(strs.get(0),16);
				boolean isCommonText=getCell(strs, 4).equals("3");
				boolean notNull=Util.isNotEmpty(getCell(strs,3));
				if(notNull && (isSmallText || isCommonText)){
					String translation=getCell(strs,3);
					byte[] bs = toBytes(translation);
					if(bs.length>Integer.parseInt(strs.get(1))){
						err.append(String.format("%s文件第%d行超长,已达到%d字节: %s", excel.getName(), rowNum,bs.length,strs.get(3))).append("\n");
						return;
					}
					
					try {
						if(isSmallText){
							exe.seek(exeOffset);
							exe.write(bs);
						}
						if(isCommonText) {
							exe.seek(smallEventTextExtensionOffset);
							exe.write(bs);		//warning!! don't exceed UV_SPACE
							if(!specialEvent.containsKey(exeOffset)){
								throw new RuntimeException("special_event.props doesn't contains "+Integer.toHexString(exeOffset));
							}
							int pointerOffset=Conf.getExeOffset(specialEvent.get(exeOffset));
							exe.seek(pointerOffset);
							int newPointerAddr=Conf.getExeAddr((int)smallEventTextExtensionOffset);
							exe.writeInt(Util.hilo(newPointerAddr));
							smallEventTextExtensionOffset+=bs.length;
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
				}
			}
		});
		if(err.length()>0) {
			System.err.println(err);
			throw new RuntimeException();
		}
	}
	
	byte[] toBytes(String text) {
		ByteBuffer ret = ByteBuffer.allocate(5000);
		ret.order(ByteOrder.BIG_ENDIAN);	//SOP的字节序特殊
		for(String s:TranslateTextReader.readGBK(text)){
			Integer code=enc.get(s);
			if(code==null){
				if(s.startsWith("{")&&s.endsWith("}")) {
					code= Integer.parseInt(s.substring(1, s.length()-1), 16);
				}else{
					code=enc.put(s);
				}
			}
			ret.putShort(code.shortValue());
		}
		ret.putShort((short) 0);	
		return Arrays.copyOfRange(ret.array(), 0, ret.capacity()-ret.remaining());
	}
	
	private String getCell(List<String> strs,int i){
		if(strs.size()>i) return strs.get(i);
		return null;
	}

}

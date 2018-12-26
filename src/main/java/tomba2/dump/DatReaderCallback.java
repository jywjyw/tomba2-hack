package tomba2.dump;

public interface DatReaderCallback {
	
	void everyChar(String char_, int i, boolean isCtrl);
	void sentenceStart(String scriptId, String textId);
	void sentenceEnd(String scriptId, String textId);
	void finalTextSize(String scriptId, int textsize);

}

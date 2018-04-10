package tomba2.dump;

import java.io.IOException;

import common.Conf;

public class Dump {
	
	public static void main(String[] args) throws IOException {
		ScriptJpInlineExporter exporter = new ScriptJpInlineExporter();
		ScriptJpReader reader = new ScriptJpReader();
		reader.callback = exporter;
		reader.loopScripts();
		exporter.export(Conf.desktop+"tomba-jp.xlsx");
		
		ScriptEnInlineExporter exporter2 = new ScriptEnInlineExporter();
		ScriptEnReader reader2 = new ScriptEnReader();
		reader2.callback = exporter2;
		reader2.loopScripts();
		exporter2.export(Conf.desktop+"tomba-en.xlsx");
	}

}

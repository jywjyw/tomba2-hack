package common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DirLooper {
	
	public static void loop(String dir, Callback c) {
		new File(dir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory()) {
					loop(pathname.getAbsolutePath(), c);
				} else {
					c.handleFile(pathname);
				}
				return true;
			}
		});
	}
	
	public interface Callback{
		void handleFile(File f);
	}
	
}

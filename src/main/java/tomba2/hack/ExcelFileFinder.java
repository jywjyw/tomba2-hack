package tomba2.hack;

import java.io.File;
import java.io.FileFilter;

public class ExcelFileFinder {
	
	public static File getNewestTranslation(String rscName){
		File dir = new File(System.getProperty("user.dir")+"/translation/");
		File[] allVersion = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				return name.endsWith(".xlsx") 
						&& !name.startsWith("~$")
						&& pathname.getName().contains(rscName);
			}
		});
		
		int maxVersion = 0;
		File maxVerFile = null;
		for(File f:allVersion){
			String noprefix = f.getName().substring(0, f.getName().lastIndexOf("."));
			char version = noprefix.charAt(noprefix.length()-1);
			if(Integer.parseInt(version+"")>=maxVersion){
				maxVerFile = f;
			}
		}
		System.out.println("using "+maxVerFile.getName());
		return maxVerFile;
	}

}

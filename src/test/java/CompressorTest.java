import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import common.Conf;
import common.Util;
import tomba2.Uncompressor;
import tomba2.hack.Compressor;

public class CompressorTest {
	public static void main(String[] args) throws IOException {
		File compBenchmark=new File(Thread.currentThread().getContextClassLoader().getResource("smoke-64-comp").getPath());
		System.out.printf("comp benchmark size: %d, md5:%s\n", compBenchmark.length(), Util.md5(compBenchmark));
		
		File uncompBenchmark=new File(Thread.currentThread().getContextClassLoader().getResource("smoke-64-uncomp").getPath());
		System.out.printf("uncomp benchmark size: %d, md5:%s\n", uncompBenchmark.length(), Util.md5(uncompBenchmark));
		
		
		FileInputStream is=new FileInputStream(uncompBenchmark);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		new Compressor().compress(64, is, os);
		is.close();
		byte[] osbytes=os.toByteArray();
		System.out.printf("my comp size: %d, md5:%s\n", osbytes.length, Util.md5(osbytes));
		
		
		File reuncomp=new File(Conf.desktop+"reuncomp");
		FileOutputStream reuncompS=new FileOutputStream(reuncomp);
		Uncompressor.uncompress(new ByteArrayInputStream(osbytes), 256, reuncompS);
		System.out.println(Util.md5(uncompBenchmark).equals(Util.md5(reuncomp)));
	}

}

package jazsync;

import java.io.File;
import jazsync.jazsync.FileMaker;
import jazsync.jazsync.LocalFileRangeLoader;
import jazsync.jazsyncmake.MetaFileMaker;

import org.junit.Before;
import org.junit.Test;



/**
 *
 * @author brad
 */
public class Scratch {

	MetaFileMaker metaFileMaker;
	FileMaker fileMaker;
	File fIn;
	File fLocal;
	
	@Before
	public void setUp() {
		fIn = new File("src/test/resources/jazsync/source.txt"); // this represents the remote file we want to download
		System.out.println("fin: " + fIn.getAbsolutePath());
		System.out.println(fIn.getAbsolutePath());

		
		fLocal = new File("src/test/resources/jazsync/dest.txt"); // this represents the current version of the local file we want to update
	}

	@Test
	public void test1() {
		metaFileMaker = new MetaFileMaker("/test", 32, fIn); //blocksize should be 300
		//metaFileMaker = new MetaFileMaker("/test", 256, fIn);
		File metaFile = metaFileMaker.make();		
		LocalFileRangeLoader rangeLoader = new LocalFileRangeLoader(fIn);		
		fileMaker = new FileMaker(rangeLoader, metaFile); 
		System.out.println("local: " + fLocal.getAbsolutePath());		
		fileMaker.make(fLocal);
		
		System.out.println("----------------------------------------------");
		System.out.println("Bytes downloaded: " + rangeLoader.getBytesDownloaded());
		
		
//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		metaFileMaker.write(bout);
//		System.out.println(bout.toString());
//
//		InputStream metaIn = new ByteArrayInputStream(bout.toByteArray());
//		
//		
//		File dest = new File("src/test/resources/jazsync/output.txt"); // the merged file
//		fileMaker.make(dest, metaIn);
		
	}
}

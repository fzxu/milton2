package jazsync;

import java.io.File;
import jazsync.jazsync.FileMaker;
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
	
	@Before
	public void setUp() {
		File fIn = new File("src/test/resources/jazsync/source.txt"); // this represents the remote file we want to download
		System.out.println("fin: " + fIn.getAbsolutePath());
		System.out.println(fIn.getAbsolutePath());
		metaFileMaker = new MetaFileMaker("/test", 300, fIn);
		
		File fLocal = new File("src/test/resources/jazsync/dest.txt"); // this represents the current version of the local file we want to update
		fileMaker = new FileMaker(fLocal); 
	}

	@Test
	public void test1() {
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

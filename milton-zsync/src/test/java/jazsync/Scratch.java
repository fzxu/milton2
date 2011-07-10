package jazsync;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.io.StreamUtils;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.StreamReceiver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import com.ettrema.zsync.FileMaker;
import com.ettrema.zsync.HttpRangeLoader;
import com.ettrema.zsync.LocalFileRangeLoader;
import com.ettrema.zsync.SHA1;
import com.ettrema.zsync.MetaFileMaker;
import java.io.FileOutputStream;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.junit.Assert;

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

		metaFileMaker = new MetaFileMaker();
		fileMaker = new FileMaker();
	}

	/**
	 * So this basically works for downloading deltas to update a local file. But
	 * what about uploading?
	 * 
	 * 1. Client calculates meta data (basically crc's for each block) and POST's
	 * to the server
	 * 2a. client LOCK's the file
	 * 2b. Server does a dry-run make on its version of the file, but instead of getting 
	 * and merging blocks it just returns the blocks it would read to the client
	 * 2c. The client then does a PUT with partial ranges of the requested blocks, including
	 * the original metadata
	 * 2d. The server does a make again, but this time with the ranges already available,
	 * and does the merge
	 * 2e. The client unlocks the file
	 * 
	 * So this means we need to decompose jaz into seperate operations
	 *  - get meta data for any inputstream
	 *  - apply meta data to a local file (make), with a seperable updater which might
	 * actualy update the file, or might be a NOOP
	 * 
	 */
	@Test
	public void test_LocalOnly() throws FileNotFoundException, Exception {
		System.out.println("--------------------- test1 -----------------------");
		System.out.println("source file: " + fIn.getAbsolutePath());
		SHA1 sha = new SHA1(fIn);
		String actual = sha.SHA1sum();
		System.out.println("checksum of source file: " + actual + " - length: " + fIn.length());

		File metaFile = metaFileMaker.make("/test", 32, fIn);
		System.out.println("generated meta file: " + metaFile.getAbsolutePath());
		LocalFileRangeLoader rangeLoader = new LocalFileRangeLoader(fIn);
		System.out.println("local: " + fLocal.getAbsolutePath());
		fileMaker.make(fLocal, metaFile, rangeLoader);

		System.out.println("----------------------------------------------");
		System.out.println("Bytes downloaded: " + rangeLoader.getBytesDownloaded());
		System.out.println("----------------------------------------------");
		System.out.println("----------------------------------------------");
	}

	/**
	 * This is to simulate usage in a CMS, where we don't have a physical file
	 * to work with
	 * 
	 * @throws FileNotFoundException 
	 */
	@Test
	public void test2_NotWorking() throws FileNotFoundException, IOException {
//		System.out.println("--------------------- test2 -----------------------");
//		FileInputStream dataIn = new FileInputStream(fIn);
//		MetaData metaData = metaFileMaker.make("/test", 32, fIn.length(), new Date(fIn.lastModified()), dataIn);
//		dataIn.close();
//
//		System.out.println("metaData ----------------");
//		System.out.println(metaData.getHeaders().toString());
//
//		LocalFileRangeLoader rangeLoader = new LocalFileRangeLoader(fIn);
//		System.out.println("local: " + fLocal.getAbsolutePath());
//		fileMaker.make(fLocal, metaData, rangeLoader); 
////		
////		System.out.println("----------------------------------------------");
////		System.out.println("Bytes downloaded: " + rangeLoader.getBytesDownloaded());		
//		System.out.println("----------------------------------------------");
//		System.out.println("----------------------------------------------");

	}

	/**
	 * For this test to work you must be running milton-ajax-demo (which has
	 * the ZSyncResourceFactory integrated) and you must have the file "source.txt"
	 * in the root directory
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws HttpException
	 * @throws Exception 
	 */
	@Test
	public void test_Download_Update_OverHTTP() throws FileNotFoundException, IOException, HttpException, Exception {
		// Get metadata from http server
		System.out.println("--------------------- test3 -----------------------");
		Host host = new Host("localhost", "webdav", 8080, "me", "pwd", null, null);
		final File fRemoteMeta = File.createTempFile("milton-zsync-remotemeta", null);
		String url = host.getHref(Path.path("/source.txt/.zsync"));
		host.doGet(url, new StreamReceiver() {

			public void receive(InputStream in) throws IOException {
				FileOutputStream fout = new FileOutputStream(fRemoteMeta);
				StreamUtils.readTo(in, fout, true, true);
			}
		}, null);
		System.out.println("meta file: " + fRemoteMeta.getAbsolutePath());
		// Now build local file
		com.ettrema.httpclient.File remoteFile = (com.ettrema.httpclient.File) host.find("/source.txt");
		Assert.assertNotNull(remoteFile);
		HttpRangeLoader rangeLoader = new HttpRangeLoader(remoteFile);

		System.out.println("local: " + fLocal.getAbsolutePath());
		fileMaker.make(fLocal, fRemoteMeta, rangeLoader);

		System.out.println("----------------------------------------------");
		System.out.println("Bytes downloaded: " + rangeLoader.getBytesDownloaded());
		System.out.println("----------------------------------------------");
		System.out.println("----------------------------------------------");

	}

	@Test
	public void test_Upload_OverHTTP() throws FileNotFoundException, HttpException {
		System.out.println();
		System.out.println("--------------------- test4 -----------------------");
		Host host = new Host("localhost", "webdav", 8080, "me", "pwd", null, null);
		
		File metaFile = metaFileMaker.make("/test", 32, fIn);
		Part[] parts = {new FilePart("meta", metaFile)};
		String url = host.getHref(Path.path("/source.txt/.zsync"));
		String ranges = host.doPost(url, null, parts);
		System.out.println("ranges: " + ranges);
	}
}

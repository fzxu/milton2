package com.ettrema.zsyncclient;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.io.StreamUtils;
import com.ettrema.httpclient.BadRequestException;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.StreamReceiver;
import com.ettrema.httpclient.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import com.ettrema.zsync.FileMaker;
import com.ettrema.httpclient.zsyncclient.HttpRangeLoader;
import com.ettrema.zsync.LocalFileRangeLoader;
import com.ettrema.zsync.SHA1;
import com.ettrema.zsync.MetaFileMaker;
import com.ettrema.zsync.UploadMakerEx;
import java.io.FileOutputStream;
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
//	@Test
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
	 * For this test to work you must be running milton-ajax-demo (which has
	 * the ZSyncResourceFactory integrated) and you must have the file "source.txt"
	 * in the root directory
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws HttpException
	 * @throws Exception 
	 */
//	@Test
	public void test_Download_Update_OverHTTP() throws FileNotFoundException, IOException, HttpException, Exception {
		// Get metadata from http server
		System.out.println("--------------------- test3 -----------------------");
		Host host = new Host("localhost", "webdav", 8080, "me", "pwd", null, null);
		final File fRemoteMeta = File.createTempFile("milton-zsync-remotemeta", null);
		String url = host.getHref(Path.path("/source.txt/.zsync"));
		boolean notExisting = false;
		try {
			host.doGet(url, new StreamReceiver() {

				@Override
				public void receive(InputStream in) throws IOException {
					FileOutputStream fout = new FileOutputStream(fRemoteMeta);
					StreamUtils.readTo(in, fout, true, true);
				}
			}, null, null);
		} catch (HttpException e) {
			if (e instanceof BadRequestException) {
				notExisting = true;
			}
		}
		com.ettrema.httpclient.File remoteFile = (com.ettrema.httpclient.File) host.find("/source.txt");
		if (notExisting) {
			throw new RuntimeException("Remote file doesnt exist");
		} else {
			System.out.println("meta file: " + fRemoteMeta.getAbsolutePath());
			// Now build local file			
			Assert.assertNotNull(remoteFile);
			HttpRangeLoader rangeLoader = new HttpRangeLoader(remoteFile, null);

			System.out.println("local: " + fLocal.getAbsolutePath());
			fileMaker.make(fLocal, fRemoteMeta, rangeLoader);

			System.out.println("----------------------------------------------");
			System.out.println("Bytes downloaded: " + rangeLoader.getBytesDownloaded());
			System.out.println("----------------------------------------------");
			System.out.println("----------------------------------------------");
		}
	}

//	@Test
	public void test_Upload_OverHTTP_TextDoc() throws FileNotFoundException, HttpException, IOException {
		System.out.println();
		System.out.println("--------------------- test4 -----------------------");
		Host host = new Host("localhost", "webdav", 8080, "me", "pwd", null, null);
		final File fRemoteMeta = File.createTempFile("milton-zsync-remotemeta", null);
		String baseUrl = host.getHref(Path.path("/source.txt"));
		String url = baseUrl + "/.zsync";
		boolean notExisting = false;
		try {
			host.doGet(url, new StreamReceiver() {

				@Override
				public void receive(InputStream in) throws IOException {
					FileOutputStream fout = new FileOutputStream(fRemoteMeta);
					StreamUtils.readTo(in, fout, true, true);
				}
			}, null, null);
		} catch (HttpException e) {
			if (e instanceof BadRequestException) {
				notExisting = true;
			}
		}
		if (notExisting) {
			System.out.println("remote file does not exist, so will upload completely");
			int result = host.doPut(baseUrl, fIn, null);
			Utils.processResultCode(result, url);
			System.out.println("done full upload!!  result: " + result);
		} else {
			System.out.println("meta file: " + fRemoteMeta.getAbsolutePath());

			UploadMakerEx umx = new UploadMakerEx(fIn, fRemoteMeta);
			File uploadFile = umx.getUploadFile();
			int result = host.doPut(url, uploadFile, null);
			Utils.processResultCode(result, url);
			System.out.println("done!!  result: " + result);
		}
	}

	@Test
	public void test_Upload_OverHTTP_WordDoc() throws FileNotFoundException, HttpException, IOException, Exception {
		doUploadTest("testfiles/word-local-copy.doc", "/word-server-copy.doc");
		System.gc();
		doUploadTest("testfiles/word-local-copy.doc", "/word-server-copy.doc");
		System.gc();
		doUploadTest("testfiles/homepage-local.psd", "/homepage-server.psd");
	}

	private void doUploadTest(String localFile, String remotePath) throws Exception {
		System.out.println("--------------------- test4 -----------------------");
		fIn = new File(localFile);
		System.out.println("sync local file: " + fIn.getAbsolutePath() + " with remote file: " + remotePath);
		Host host = new Host("localhost", "webdav", 8080, "me", "pwd", null, null);
		final File fRemoteMeta = File.createTempFile("milton-zsync-remotemeta-wordDoc", null);
		String baseUrl = host.getHref(Path.path(remotePath));
		String url = baseUrl + "/.zsync";
		boolean notExisting = false;
		try {
			host.doGet(url, new StreamReceiver() {

				@Override
				public void receive(InputStream in) throws IOException {
					FileOutputStream fout = new FileOutputStream(fRemoteMeta);
					long size = StreamUtils.readTo(in, fout, true, true);
					System.out.println("Downloaded remote meta file of size: " + formatBytes(size));
				}
			}, null, null);
		} catch (HttpException e) {
			if (e instanceof BadRequestException) {
				notExisting = true;
			}
		}
		Runtime rt = Runtime.getRuntime();
		long startUsed = 0;
		if (notExisting) {
			System.out.println("remote file does not exist, so will upload completely");
			int result = host.doPut(baseUrl, fIn, null);
			Utils.processResultCode(result, url);
			System.out.println("done full upload!!  result: " + result);
		} else {
			System.out.println("meta file: " + fRemoteMeta.getAbsolutePath());

			UploadMakerEx umx = new UploadMakerEx(fIn, fRemoteMeta);
			System.gc();
			System.out.println("Memory stats: " + formatBytes(rt.maxMemory()) + " - " + formatBytes(rt.totalMemory()) + " - " + formatBytes(rt.freeMemory()));
			startUsed = rt.totalMemory() - rt.freeMemory();
			System.out.println("mem: " + startUsed);

			//UploadMakerEx umx = new UploadMakerEx(fIn, fRemoteMeta);
			File uploadFile = umx.getUploadFile();
			System.out.println("Created upload file of size: " + formatBytes(uploadFile.length()));

			System.gc();
			long endUsed = (rt.totalMemory() - rt.freeMemory());
			System.out.println("Memory change1: " + formatBytes(endUsed - startUsed));

			System.out.println("Uploading: " + uploadFile.getAbsolutePath());
			int result = host.doPut(url, uploadFile, null);
			Utils.processResultCode(result, url);
			System.out.println("done!!  result: " + result);
		}
		System.gc();
		System.out.println("Memory stats: " + formatBytes(rt.maxMemory()) + " - " + formatBytes(rt.totalMemory()) + " - " + formatBytes(rt.freeMemory()));
		long endUsed = (rt.totalMemory() - rt.freeMemory());
		System.out.println("Start used memory: " + formatBytes(startUsed) + " end used memory: " + formatBytes(endUsed));
		System.out.println("Memory change2: " + formatBytes(endUsed - startUsed));
	}

	private String formatBytes(long l) {
		if (l < 1000) {
			return l + " bytes";
		} else if (l < 3000000) {
			return l / 1000 + "KB";
		} else {
			return l / 1000000 + "MB (" + l + ")";
		}
	}
}

package jazsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.bradmcevoy.common.Path;
import com.ettrema.httpclient.Host;
import com.ettrema.zsync.MetaFileMaker;
import com.ettrema.zsync.SHA1;
import com.ettrema.zsync.UploadMaker;
import com.ettrema.zsync.UploadReader;

/**
 * Tests the complete ZSync upload procedure. 
 * 
 * @author Nick
 *
 */
public class IntegrationTests {

	File localcopy;
	File servercopy;
	String filepath;
	
	int blocksize;
	
	/**
	 * Initializes localcopy and servercopy. These should be changed to reflect actual location of the client
	 * and server files.
	 */
	@Before
	public void setUp() {
		
		//Change to the correct locations of local file and server file
		filepath = "testfiles" + File.separator; 
		//localcopy = new File( filepath + "word-local-copy.doc" );
		//servercopy = new File( filepath + "word-server-copy.doc" );
		localcopy = new File( filepath + "large-text-local.txt" );
		servercopy = new File( filepath + "large-text-server.txt" );
		
		blocksize = 1024;
		//blocksize = 64;
		//blocksize = 1024 * 8;
		
		System.out.println("local file: " + localcopy.getAbsolutePath());
		System.out.println("server file: " + servercopy.getAbsolutePath());
	}

	/**
	 * Sends a ZSync PUT request and asserts whether the response is 204.
	 * 
	 * @throws IOException
	 */
	//@Test
	public void testFullUpload() throws IOException {
		
		Host host = new Host("localhost", "webdav", 8080, "user1", "pwd1", null, null);
		File zsyncFile = createMetaFile( "servercopy.zsync", blocksize, servercopy );
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "localcopy2.UPLOADZS" );
	
		//Change to correct url of servercopy.txt
		String url = host.getHref( Path.path( servercopy.getName() + "/.zsync" ) );
		System.out.println("uploading to: " + url);
		InputStream uploadIn = new FileInputStream( uploadFile );
		
		int result = host.doPut(url, uploadIn, uploadFile.length(), null );
		System.out.println( "Response: " + result );
		uploadIn.close();
		System.out.println("Upload file: " + uploadFile.getAbsolutePath());
		System.out.println("Upload size: " + formatBytes(uploadFile.length()));
		Assert.assertEquals( 204, result );
		
	}
	
	private String formatBytes(long l) {
		if( l < 1000 ) {
			return l + " bytes";
		} else if( l < 1000000) {
			return l/1000 + "KB";
		} else {
			return l/1000000 + "MB";
		}
	}	
	
	/**
	 * Writes/reads the upload stream to/from a File, and asserts whether the assembled File
	 * has the same checksum as the client File.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	//@Test
	public void testMakeAndReadUpload() throws IOException, ParseException{
		
		File zsyncFile = createMetaFile("serverfile.zsync", blocksize, servercopy );
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "localcopy.UPLOADZS" );
		File assembledFile = readSavedUpload( uploadFile, filepath + "assembledcopy.pdf", servercopy );
		
		String localSha1 =  new SHA1( localcopy ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
	}
	
	//@Test
	public void testMakeAndReadSmallTextUpload() throws IOException, ParseException{
		System.out.println("------------------- testMakeAndReadSmallTextUpload -------------------------");
		File serverSmallText = new File("testfiles/small-text-server.txt");
		File localSmallText = new File("testfiles/small-text-local.txt");
		if( !serverSmallText.exists()) {
			throw new RuntimeException("Couldnt find: " + serverSmallText.getAbsolutePath());
		}
		if( !localSmallText.exists()) {
			throw new RuntimeException("Couldnt find: " + localSmallText.getAbsolutePath());
		}
		
		File zsyncFile = createMetaFile("small-text.zsync", 16, serverSmallText ); // use small blocksize
		File uploadFile = makeAndSaveUpload( localSmallText, zsyncFile, filepath + "small-text-local.UPLOADZS" );
		System.out.println("Created upload file: " + uploadFile.getAbsolutePath());
		File assembledFile = readSavedUpload( uploadFile, filepath + "small-text-assembled.txt", serverSmallText );
		System.out.println("Assesmbling to: " + assembledFile.getAbsolutePath());
		
		String localSha1 =  new SHA1( localSmallText ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
		System.out.println("---------------------- End testMakeAndReadSmallTextUpload ------------------------");
	}	
	
	@Test
	public void testMakeAndRead_Large_CSV() throws IOException, ParseException{
		System.out.println("------------------- testMakeAndRead_Large_CSV -------------------------");
		File serverSmallText = new File("testfiles/large-csv-server.csv");
		File localSmallText = new File("testfiles/large-csv-local.csv");
		if( !serverSmallText.exists()) {
			throw new RuntimeException("Couldnt find: " + serverSmallText.getAbsolutePath());
		}
		if( !localSmallText.exists()) {
			throw new RuntimeException("Couldnt find: " + localSmallText.getAbsolutePath());
		}
		
		File zsyncFile = createMetaFile("large-excel.zsync", 256, serverSmallText ); // use small blocksize
		File uploadFile = makeAndSaveUpload( localSmallText, zsyncFile, filepath + "large-csv.UPLOADZS" );
		System.out.println("Created upload file: " + uploadFile.getAbsolutePath() + " of " + formatBytes(uploadFile.length()) );
		File assembledFile = readSavedUpload( uploadFile, filepath + "large-csv-assembled.xls", serverSmallText );
		System.out.println("Assesmbling to: " + assembledFile.getAbsolutePath());
		
		String localSha1 =  new SHA1( localSmallText ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
		System.out.println("---------------------- End testMakeAndRead_Large_CSV ------------------------");
	}		
	
	/**
	 * Reads the ZSync upload data that was saved to uploadFile, constructs an UploadReader
	 * to assemble the new file, and saves (and returns) the assembled file as fileName
	 * 
	 * @param uploadFile A file containing the data from an upload
	 * @param fileName The pathname String (with file name included)
	 * @return The assembled File
	 * @throws IOException
	 * @throws ParseException
	 */
	private File readSavedUpload( File uploadFile, String fileName, File serverFile ) throws IOException, ParseException {
		
		InputStream uploadIn = new FileInputStream( uploadFile );
		UploadReader um = new UploadReader( serverFile, uploadIn );
		
		File assembledFile = new File( fileName );
		if( assembledFile.exists() ) {
			if( !assembledFile.delete() ) {
				throw new RuntimeException("Couldnt delete previous assembled file: " + assembledFile.getAbsolutePath());
			}
		}
		File tempAssembled = um.assemble();
		FileUtils.moveFile( tempAssembled, assembledFile );
		
		
		uploadIn.close();
		return assembledFile;
		
	}
	
	/**
	 * Constructs an UploadMaker/UploadMakerEx, saves the Upload stream to a new File with
	 * name uploadFileName, and returns that File.
	 * 
	 * @param localFile The local file to be uploaded
	 * @param zsFile The zsync of the server file
	 * @param uploadFileName The name of the File in which to save the upload stream
	 * @return
	 * @throws IOException
	 */
	private File makeAndSaveUpload(File localFile, File zsFile, String uploadFileName) throws IOException {
		
		UploadMaker umx = new UploadMaker( localFile, zsFile );
		InputStream uploadIn = umx.makeUpload();
		
		File uploadFile = new File( uploadFileName );

		if( uploadFile.exists()) {
			if( !uploadFile.delete()) {
				throw new RuntimeException("Couldnt delete: " + uploadFile.getAbsolutePath());
			}
		}
		FileOutputStream uploadOut = new FileOutputStream( uploadFile );
		
		IOUtils.copy( uploadIn, uploadOut );
		uploadIn.close();
		uploadOut.close();
		
		return uploadFile;
		
		
	}
	
	/**
	 * Creates the zsync File for servercopy, and saves it to a File with name fileName
	 * @param fileName The name of the file in which to save the zsync data
	 * @param blocksize The block size to use in MetaFileMaker
	 * @return The created zsync File
	 * @throws FileNotFoundException
	 */
	private File createMetaFile(String fileName, int blocksize, File serverFile) throws FileNotFoundException{
		System.out.println("createMetaFile: " + serverFile.getAbsolutePath());
		MetaFileMaker mkr = new MetaFileMaker();
		File zsfile = mkr.make( null , blocksize, serverFile );
		File dest = new File ( filepath + fileName );
		if( dest.exists() ) {
			if( !dest.delete()) {
				throw new RuntimeException("Failed to delete previous meta file: " + dest.getAbsolutePath());
			}
		}
		System.out.println("rename meta file to: " + dest.getAbsolutePath());
		if( !zsfile.renameTo( dest ) ) {
			throw new RuntimeException("Failed to rename to: " + dest.getAbsolutePath());
		}
		System.out.println("Created meta file of size: " + formatBytes(dest.length()));
		return dest;
	}
	
	
	
	
	
}

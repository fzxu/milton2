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
import com.ettrema.zsync.UploadMakerEx;
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
		filepath = "testfiles\\"; 
		localcopy = new File( filepath + "localcopy.txt" );
		servercopy = new File( filepath + "servercopy.txt" );
		System.out.println("local file: " + localcopy.getAbsolutePath());
		System.out.println("server file: " + servercopy.getAbsolutePath());
		blocksize = 128;
	}

	/**
	 * Sends a ZSync PUT request and asserts whether the response is 204.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFullUpload() throws IOException {
		
		Host host = new Host("localhost", "webdav", 8080, "user1", "pwd1", null, null);
		File zsyncFile = createMetaFile( "servercopy.zsync", blocksize );
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "localcopy2.UPLOADZS" );
	
		//Change to correct url of servercopy.txt
		String url = host.getHref( Path.path( "servercopy.txt/.zsync" ) );
		InputStream uploadIn = new FileInputStream( uploadFile );
		
		int result = host.doPut(url, uploadIn, uploadFile.length(), null );
		System.out.println( "Response: " + result );
		uploadIn.close();
		
		Assert.assertEquals( 204, result );
		
	}
	
	/**
	 * Writes/reads the upload stream to/from a File, and asserts whether the assembled File
	 * has the same checksum as the client File.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	@Test
	public void testMakeAndReadUpload() throws IOException, ParseException{
		
		File zsyncFile = createMetaFile("serverfile.zsync", blocksize );
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "localcopy.UPLOADZS" );
		File assembledFile = readSavedUpload( uploadFile, filepath + "assembledcopy.txt" );
		
		String localSha1 =  new SHA1( localcopy ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
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
	private File readSavedUpload( File uploadFile, String fileName ) throws IOException, ParseException {
		
		InputStream uploadIn = new FileInputStream( uploadFile );
		UploadReader um = new UploadReader( servercopy, uploadIn );
		uploadIn.close();
		
		File assembledFile = new File( fileName );
		FileUtils.moveFile( um.getUploadedFile() , assembledFile );
		
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
		
		UploadMakerEx umx = new UploadMakerEx( localcopy, zsFile );
		InputStream uploadIn = umx.getUploadStream();
		
		File uploadFile = new File( uploadFileName );
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
	private File createMetaFile(String fileName, int blocksize) throws FileNotFoundException{
		
		MetaFileMaker mkr = new MetaFileMaker();
		File zsfile = mkr.make( null , blocksize, servercopy );
		File dest = new File ( filepath + fileName );
		System.out.println("rename meta file to: " + dest.getAbsolutePath());
		zsfile.renameTo( dest );
		return zsfile;
	}
	
	
	
	
	
}

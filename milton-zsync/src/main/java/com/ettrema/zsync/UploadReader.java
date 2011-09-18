package com.ettrema.zsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.http11.PartialGetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * An object that performs the server side operations needed to assemble the file from a ZSync PUT. <p/>
 * 
 * These operations consist of copying byte ranges into the new file. The {@link #moveBlocks}
 * method copies ranges from the previous file according to a list of RelocateRanges, while
 * the {@link #sendRanges} method copies the new data received in the upload. Both of these
 * methods are overloaded with versions that accept File rather than byte[] arguments for dealing
 * with large files that should not be loaded into memory all at once.<p/>
 * 
 * To assemble the file from a ZSync upload, the server should construct an UploadReader, passing to
 * the constructor the file to be updated and an InputStream containing the upload data. It should then invoke the 
 * {@link #getUploadedFile()} method, which will return a temporary file that was created.
 * 
 * @author Nick
 *
 */
public class UploadReader {

	private static final Logger log = LoggerFactory.getLogger(UploadReader.class);
	
	
	/**
	 * Copies blocks of data from the in array to the out array. 
	 * 
	 * @param in The byte array containing the server's file being replaced
	 * @param rlist The List of RelocateRanges received from the upload
	 * @param blockSize The block size used in rlist
	 * @param out The byte array of the file being assembled
	 */
	public static void moveBlocks(byte[] in, List <RelocateRange> rlist, int blockSize, byte[] out){
		
		for (RelocateRange reloc: rlist){
			
			int startBlock = (int) reloc.getBlockRange().getStart();
			int finishBlock = (int) reloc.getBlockRange().getFinish();
			
			int startByte = startBlock*blockSize;
			int newOffset = (int) reloc.getOffset();
			int numBytes = (finishBlock - startBlock)*blockSize;
			
			System.arraycopy(in, startByte, out, newOffset, numBytes);
			
		}
	}
	
	/**
	 * Copies blocks of data from the input File to the output File. For each RelocateRange A-B/C in rlist, 
	 * the block starting at A and ending at B-1 is copied from inFile and written to byte C of outFile.
	 * 
	 * @param inFile The server's File being replaced
	 * @param relocRanges The List of RelocateRanges received from the upload
	 * @param blocksize The block size used in rlist
	 * @param outFile The File being assembled
	 * @throws IOException
	 */
	public static void moveBlocks(File inFile, 
			List <RelocateRange> relocRanges, int blocksize, File outFile) throws IOException{
		/*
		 * Because transferFrom can supposedly throw Exceptions when copying large Files,
		 * this method invokes moveRange to copy incrementally
		 */
		
		
		/*The FileChannels should be obtained from a RandomAccessFile rather than a 
		 *Stream, or the position() method will not work correctly
		 */
		FileChannel rc = null;
		FileChannel wc = null;
		try {
			rc = new RandomAccessFile(inFile, "r").getChannel();
			wc = new RandomAccessFile(outFile, "rw").getChannel();
			
			
			for (RelocateRange reloc : relocRanges) {
				
				
				moveRange(rc, reloc, blocksize, wc);
			}
		} finally {
			Util.close(rc);
			Util.close(wc);
		}

	}
	
	/**
	 * Copies a Range of blocks from rc into a new offset of wc
	 * 
	 * @param rc A FileChannel for the input File
	 * @param reloc The RelocateRange specifying the Range to be copied and its new offset
	 * @param blockSize The block size used by reloc
	 * @param wc The FileChannel for the output File
	 * @throws IOException
	 */
	private static void moveRange(FileChannel rc, RelocateRange reloc, 
			int blockSize, FileChannel wc) throws IOException{

		long MAX_BUFFER = 16384; 
		
		int startBlock = ( int ) reloc.getBlockRange().getStart();
		int finishBlock = ( int ) reloc.getBlockRange().getFinish();
		
		long bytesLeft = ( finishBlock - startBlock ) * blockSize; //bytes left to copy
		long readAtOnce = 0; //number of bytes to attempt to read
		long bytesRead = 0; //number of bytes actually read
		long currOffset = reloc.getOffset(); //current write position
		
		if ( finishBlock * blockSize > rc.size() || startBlock < 0 ) {
			
			throw new RuntimeException( "Invalid RelocateRange: Source file does not contain blocks " +
					reloc.getBlockRange().getRange() );
		} 
		
		rc.position( startBlock * blockSize ); 
		while ( bytesLeft > 0 ) {
			readAtOnce = Math.min( bytesLeft, MAX_BUFFER );
			
			/*Because transferFrom does not update the write channel's position, 
			 * it needs to be set manually
			 */
			bytesRead = wc.transferFrom( rc, currOffset, readAtOnce);
			bytesLeft -= bytesRead;
			currOffset += bytesRead;
		}
		
	}
	
	/**
	 * Copies bytes from the in array into Ranges of the out array. The in array is expected to 
	 * contain the queued bytes in the same order as the ranges List.
	 * 
	 * @param in An array containing the queued bytes corresponding to the ranges List
	 * @param ranges The List of target Ranges
	 * @param out The byte array for the file being assembled
	 */
	public static void sendRanges(byte[] in, List<Range> ranges, byte[] out){
		
		int pos = 0;
		for (Range r: ranges){
			
			int length = (int) (r.getFinish() - r.getStart());
			System.arraycopy(in, pos, out, (int) r.getStart(), length);
			pos += length;
		}
	}
	
	/**
	 * Inserts the data from each DataRange into the output File, at the appropriate offset
	 * 
	 * @param dataRanges The List of Range/byte stream pairs received in the upload
	 * @param outFile The output File being assembled
	 * @throws IOException
	 */
	public static void sendRanges( List<DataRange> dataRanges, File outFile ) 
		throws IOException{
		
		int BUFFER_SIZE = 16384;
		byte[] buffer = new byte[BUFFER_SIZE];
		
		RandomAccessFile randAccess = null;
		try {
			randAccess = new RandomAccessFile( outFile, "rw" );
			for (DataRange dataRange : dataRanges) {
				
				Range range = dataRange.getRange();
				InputStream data = dataRange.getInputStream();
				sendBytes(data, range, buffer, randAccess);
				data.close();
			}
		} finally {
			Util.close(randAccess);
		}
		
	
	}
	
	/**
	 * Reads a number of bytes from the InputStream equal to the size of the specified Range and
	 * writes them into that Range of the RandomAccessFile.
	 * 
	 * @param dataIn The InputStream containing the data to be copied
	 * @param range The target location in the RandomAccessFile
	 * @param buffer A byte array used to transfer data from dataIn to fileOut
	 * @param fileOut A RandomAccessFile for the File being assembled
	 * @throws IOException
	 */
	private static void sendBytes( InputStream dataIn, Range range, byte[] buffer, 
			 RandomAccessFile fileOut )  throws IOException {
		
		int bytesLeft = (int) (range.getFinish() - range.getStart());
		int readAtOnce = 0;
		int bytesRead = 0;
		
		fileOut.seek(range.getStart());
		
		while ( bytesLeft > 0 ) {
			
			readAtOnce = Math.min( buffer.length, bytesLeft );
			bytesRead = dataIn.read( buffer, 0, readAtOnce );
			fileOut.write( buffer, 0, bytesRead );
			bytesLeft -= bytesRead;
			
			if ( bytesLeft > 0 && bytesRead < 0 ) {
				
				throw new RuntimeException( "Unable to copy byte Range: " + range.getRange() + 
						". End of InputStream reached with " + bytesLeft + " bytes left.");
			}

		}
	
	}


	private File serverCopy;
	private File uploadedCopy;
	private Upload uploadData;
	
	/**
	 * Constructor that parses the InputStream into an Upload and automatically assembles the
	 * uploaded data, which it saves to a temporary file
	 * 
	 * @param destFile The server file to be updated
	 * @param in A stream containing the ZSync PUT data
	 * @throws IOException 
	 */
	public UploadReader(File serverFile, InputStream uploadIn) throws IOException{

		this.serverCopy = serverFile;
		this.uploadData = Upload.parse( uploadIn );
		this.uploadedCopy = File.createTempFile( "zsync-upload", "newFile" );
	}

	/**
	 * Invokes the methods to put together the uploaded file.
	 * 
	 * @throws IOException
	 */
	public File assemble() throws IOException{
		
		if ( uploadData.getBlocksize() <= 0 ) {
			throw new RuntimeException( "Invalid blocksize specified: " + uploadData.getBlocksize() );
		}
		
		if ( uploadData.getFilelength() <= 0 ) {
			throw new RuntimeException( "Invalid file length specified: " + uploadData.getFilelength() );
		}
		
		if ( StringUtils.isBlank( uploadData.getSha1() )) {
			throw new RuntimeException( "No SHA1 checksum provided." );
		}
		copyFile( serverCopy, uploadedCopy, uploadData.getFilelength() );
		
		sendRanges( uploadData.getDataList(), uploadedCopy);
		
		moveBlocks( serverCopy, uploadData.getRelocList(), 
				(int) uploadData.getBlocksize(), uploadedCopy);	

		return uploadedCopy;
	}
	
	/**
	 * Copies the contents of the source file to the destination file and sets the destination
	 * file's length.
	 * 
	 * @param inFile The source file	
	 * @param outFile The destination file
	 * @param length The desired length of the destination file
	 * @throws IOException
	 */
	private static void copyFile ( File inFile, File outFile, long length ) throws IOException{
	
		InputStream fIn = new FileInputStream( inFile );
		OutputStream fOut = new FileOutputStream ( outFile );
		
		PartialGetHelper.sendBytes( fIn, fOut, inFile.length() );
		
		fIn.close();
		fOut.close();
	
		RandomAccessFile randAccess = new RandomAccessFile( outFile, "rw" );
		randAccess.setLength( length );
		randAccess.close();
		
	}



	/**
	 * Returns the expected SHA1 checksum String received in the upload
	 * 
	 * @return A SHA1 checksum
	 */
	public String getChecksum() {
		
		return uploadData.getSha1();
	}

}

package com.ettrema.zsync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import com.bradmcevoy.http.Range;
import java.io.FileOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * A slight variation of <code>UploadMaker</code> that accommodates updating of potentially
 * redundant files, ie files containing blocks that repeat at multiple offsets. <p/>
 * 
 * It is not necessary to use this class rather than UploadMaker. This class simply 
 * decreases the amount of data that needs to be transmitted for certain cases of redundant files. The
 * sole difference from UploadMaker is that this class uses methods that expect a reverse block-matching in the
 * form of an array where f[serverIndex] = localOffset (actually implemented as a List of OffsetPairs), 
 * whereas UploadMaker expects f[localOffset] = serverIndex.<p/>  
 * 
 * The main advantage of the reverse mapping is that it allows multiple identical block ranges in the local
 * file to be mapped to a single range in the server file. If a given block occurs more times on the local file than
 * on the server file, the f[serverIndex] array will not identify all of those local occurences, and the unmatched ones
 * will be transmitted needlessly.<p/>
 * 
 *
 * @author Nick
 * 
 */
public class UploadMakerEx {

	
	/**
	 * The local file that will replace the server file
	 */
	public final File localCopy;
	
	/**
	 * The .zsync of the server file to be replaced
	 */
	public final File serversMetafile;

	private MetaFileReader metaFileReader;
	private MakeContextEx uploadContext;
	private Upload upload;
	
	/**
	 * Constructor that invokes methods to map block-matches, and creates and fills in
	 * an internal Upload object
	 * 
	 * @param sourceFile The client file to be uploaded
	 * @param zsFile The zsync of the server's file
	 * @throws IOException
	 */
	public UploadMakerEx(File sourceFile, File zsFile) throws IOException{
		
		this.localCopy = sourceFile;
		this.serversMetafile = zsFile;
		this.upload = new Upload();

		this.initMetaData();
		this.initUpload();
	}
	
	private void initMetaData(){
		
		this.metaFileReader = new MetaFileReader( serversMetafile );
		this.uploadContext = new MakeContextEx( metaFileReader.getHashtable(), 
				new long[metaFileReader.getBlockCount()] );
		Arrays.fill( uploadContext.fileMap, -1 );
		
		MapMatcher matcher = new MapMatcher();
		matcher.mapMatcher( localCopy, metaFileReader, uploadContext );
	}
	
	private void initUpload() throws IOException{
	
		
		List<Range> ranges = serversMissingRangesEx( uploadContext.getReverseMap(),
				localCopy.length(), metaFileReader.getBlocksize() );
		List<DataRange> dataRanges = getDataRanges( ranges, localCopy );
		
		List<RelocateRange> relocRanges = serversRelocationRangesEx( uploadContext.getReverseMap(), 
				metaFileReader.getBlocksize(), localCopy.length(), true );

		upload.setVersion( "testVersion" );
		upload.setBlocksize( metaFileReader.getBlocksize() );
		upload.setFilelength( localCopy.length() );
		upload.setSha1(  new SHA1( localCopy ).SHA1sum()  );
		upload.setRelocList( relocRanges );
		upload.setDataList ( dataRanges );
	
	}
	
	/**
	 * Determines the byte ranges from the client file that need to be uploaded to the server.
	 * 
	 * @param reverseMap The List of block-matches obtained from MakeContextEx
	 * @param fileLength The length of the local file being uploaded
	 * @param blockSize The block size used in reverseMap
	 * @return The List of Ranges that need to be uploaded 
	 * @see UploadMaker#serversMissingRanges
	 */
	public static List<Range> serversMissingRangesEx(List<OffsetPair> reverseMap, long fileLength, int blockSize){
		
		List <Range> rangeList = new ArrayList<Range>(); 
		Collections.sort(reverseMap, new OffsetPair.LocalSort()); 
		reverseMap.add(new OffsetPair(fileLength, -1)); 
		
		
		long prevEnd = 0;
		
		for (OffsetPair pair: reverseMap){
			
			long offset = pair.localOffset;
			if (offset - prevEnd > 0){
				
				rangeList.add(new Range(prevEnd, offset));
			}
			prevEnd = offset + blockSize;
			
		}
		
		return rangeList;
		
	}
	
	/**
	 * Determines the instructions needed by the server to relocate blocks of data already contained
	 * in its version of the file.
	 * 
	 * @param reverseMap The List of block-matches obtained from MakeContextEx
	 * @param blockSize The block size used to generate reverseMap
	 * @param fileLength The length of the client file being uploaded
	 * @param combineRanges Whether to combine consecutive block matches into a single RelocateRange
	 * @return The List of RelocateRanges that need to be sent to the server
	 * @see {@link UploadMaker#serversRelocationRanges}
	 */
	public static List<RelocateRange> serversRelocationRangesEx(List<OffsetPair> reverseMap, int blockSize, long fileLength, boolean combineRanges){
		
		List<RelocateRange> ranges = new ArrayList<RelocateRange>();
		Collections.sort(reverseMap, new OffsetPair.RemoteSort());
		
		for (ListIterator<OffsetPair> iter = reverseMap.listIterator()
				; iter.hasNext(); ){
			
			OffsetPair pair = iter.next();
			long localOffset = pair.localOffset;
			long blockIndex = pair.remoteBlock;
			
			/*If the local offset and server offset of a given matching block 
			 * are the same, then no instruction is sent.
			 */
			if (localOffset >= 0 && localOffset != blockIndex*blockSize){
				
				if (localOffset > fileLength - blockSize){
					//out of range
					continue;
				}
				
				Range blockRange;
				if (combineRanges == true){
					
					blockRange = consecMatchesEx(iter, localOffset, blockIndex, blockSize);
				} else {
					
					blockRange = new Range(blockIndex, blockIndex + 1);
				}
				
				RelocateRange relocRange = new RelocateRange(blockRange, localOffset);
				ranges.add(relocRange);
			}
		}
		return ranges;
	}
	
	/**
	 * Returns a Range representing a sequence of contiguous server blocks, beginning at blockIndex, that 
	 * are to be relocated as a single chunk.
	 * 
	 * @param iter An iterator positioned immediately after the first match of the sequence
	 * @param localOffset The local byte offset of the first matching block of the sequence
	 * @param blockIndex The server block index of the first matching block of the sequence
	 * @param blockSize The number of bytes in a block
	 * @return A Range of contiguous blocks that are to be relocated to localOffset
	 */
	private static Range consecMatchesEx(ListIterator<OffsetPair> iter, long localOffset,
			long blockIndex, int blockSize){

		long currBlock = blockIndex;
		long currByte = localOffset;

		while (iter.hasNext()){ 
			
			OffsetPair pair = iter.next();
			
			currByte += blockSize;
			currBlock++;
			
			if (pair.localOffset != currByte || 
					pair.remoteBlock != currBlock){
				
				iter.previous();
				return new Range( blockIndex, currBlock );
				
			} 
			
		}
		return new Range(blockIndex, currBlock + 1 );
	}
	
	/**
	 * Constructs the List of DataRange objects containing the portions of the client file
	 * to be uploaded to the server.
	 * 
	 * @param ranges The List of Ranges from the client file needed by the server, which can be 
	 * obtained from {@link #serversMissingRangesEx(List, long, int)}
	 * @param local The client file to be uploaded
	 * @return The List of DataRange objects containing client file portions to be uploaded
	 * @throws IOException
	 */
	public static List<DataRange> getDataRanges (List<Range> ranges, File local) throws IOException{
		
		List <DataRange> dataRanges = new ArrayList <DataRange>();
		RandomAccessFile randAccess = new RandomAccessFile( local, "r" );

		for ( Range range : ranges ) {
			
			dataRanges.add( new DataRange( range, randAccess ) );
		}
	
		return dataRanges;
	}
	
	/**
	 * Returns the stream of bytes to be used as the body of a ZSync PUT.<p/>
	 * 
	 * Note: Any temporary files used to store the data for the stream will be deleted after
	 * the stream is closed, so a second invocation of this method may not work.
	 * 
	 * @return The InputStream containing the data for a ZSync PUT
	 * @throws UnsupportedEncodingException 
	 * @throws IOException
	 */
	public InputStream getUploadStream() throws UnsupportedEncodingException, IOException{
		
		return upload.getInputStream();
	}
	
	
	/**
	 * Generates the upload content to a temp file.
	 * 
	 * @return
	 * @throws IOException 
	 */
	public File getUploadFile() throws IOException {
		
		InputStream uploadIn = getUploadStream();
		
		File uploadFile = File.createTempFile("zsync-upload", localCopy.getName());
		FileOutputStream uploadOut = new FileOutputStream( uploadFile );
		
		IOUtils.copy( uploadIn, uploadOut );
		uploadIn.close();
		uploadOut.close();
		
		return uploadFile;				
	}	
}

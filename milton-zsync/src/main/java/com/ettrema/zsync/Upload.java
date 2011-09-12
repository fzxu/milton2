package com.ettrema.zsync;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.bradmcevoy.http.Range;

/**
 * A container for the information transmitted in a ZSync PUT upload. The information currently consists of some
 * headers (file length, block size, etc...), a list of RelocateRanges for relocating matching blocks, 
 * and a sequence of data chunks (along with their ranges). The Upload class also contains methods for 
 * translating to/from a stream (getInputStream and parse, respectively).
 * 
 * @author Administrator
 *
 */
public class Upload {

	/**
	 * The character encoding used to convert Strings to bytes. The default is US-ASCII.
	 * 
	 */
	public final static String CHARSET = "US-ASCII";
	/**
	 * The character marking the end of a line. The default is '\n'
	 */
	public final static char LF = '\n';
	/**
	 * A String that marks the beginning of a range of uploaded bytes. Currently unused.
	 */
	public String DIV = "--DIVIDER";

	public final static String VERSION = "zsync";
	
	public final static String BLOCKSIZE = "Blocksize";

	public final static String FILELENGTH = "Length";
	/**
	 * The total number of bytes of new data to be transmitted. Currently Unused.
	 */
	public final static String NEWDATA = "ContentLength";

	public final static String SHA_1 = "SHA-1";

	public final static String RELOCATE = "Relocate";
	 
	public final static String RANGE = "Range";
	
	private String version;
	private String contentLength;
	private String sha1;
	private long blocksize;
	private long filelength;
	
	private List<RelocateRange> relocList;
	private List<DataRange> dataList;

	
	/**
	 * Returns the list of headers in String format, in the proper format for upload.
	 *
	 * @return A String containing the headers
	 */
	public String getParams(){
		
		StringBuilder sbr = new StringBuilder();
		
		sbr.append( paramString( VERSION, version ) );
		sbr.append( paramString( FILELENGTH, filelength ) );
		sbr.append( paramString( BLOCKSIZE, blocksize ) );
		sbr.append( paramString( SHA_1, sha1 ) );
		
		return sbr.toString();
	}
	
	private static String paramString( String key, Object value ){
		
		return key + ": " + value + LF;
	}
	
	/**
	 * Returns the list of RelocateRanges as a KEY:VALUE String, where the VALUE is a 
	 * comma separated List, e.g.<p/>
	 * 
	 * "Relocate: 10-20/1234, 50-100/6789"
	 * 
	 * @return A String containing the RELOCATE key: value pair
	 */
	public String getRelocates(){
		
		StringBuilder relocString = new StringBuilder();
		for ( Iterator<RelocateRange> iter = relocList.iterator(); iter.hasNext();){
			
			relocString.append( iter.next().getRelocation() );
			
			if (iter.hasNext()){
				relocString.append(", ");
			}
		}
		
		return paramString( RELOCATE, relocString );
	}
	
	/**
	 * Constructs an empty Upload object. Its fields need to be set individually.
	 */
	public Upload(){
		
		this.relocList = new ArrayList<RelocateRange>();
		this.dataList = new ArrayList<DataRange>();

	}

	/**
	 * Parses the InputStream into an Upload object.<p/>
	 * 
	 * The method initially reads from the InputStream one line at a time, invoking parseParam
	 * on each line, until it reaches a "blank" line, ie a line that is null or contains only whitespace.
	 * It then switches over to parsing data ranges, which means reading a line, parsing the "Range" key value pair, 
	 * and then reading the exact number of bytes specified in the range. This continues until another "blank"
	 * line is reached, which marks the end of the upload.
	 * 
	 * @param in The InputStream containing the ZSync upload
	 * @return A filled in Upload object
	 */
	public static Upload parse(InputStream in) {
	
		Upload um = new Upload();
		int bytesRead = 0; //Enables a ParseException to specify the offset
		
		try{
			//Maximum number of bytes allowed in a line (Needs to accommodate long RelocateRange Lists).
			int MAX_SEARCH = 2000000; 
			//Byte value of the LF character. Assumes that LF is one byte.
			int NEWLINE = new String( Character.toString( LF ) ).getBytes( CHARSET )[0];
			
			String line;
			while ( !StringUtils.isBlank( line = readLine( in, NEWLINE, MAX_SEARCH ) ) ) {
				
				um.parseParam( line );
				bytesRead += line.length();
			}

			while ( !StringUtils.isBlank( line = readLine( in, NEWLINE, MAX_SEARCH ) ) ) {

				KeyValue kv = KeyValue.parseKV( line );
				if ( !kv.KEY.equals( RANGE ) ) {
					
					throw new ParseException( "Could not find 'Range' value in \""
							+ line +"\". ", 0 );
				} 
				
				DataRange dataRange = new DataRange( Range.parse( kv.VALUE ), in );
				um.dataList.add( dataRange );
				bytesRead += line.length() + dataRange.getLength();
				in.skip(1); //Skips the LF at the end of the data chunk. Assumes LF is one byte.
			}
			
		} catch ( IOException e ) {
			throw new RuntimeException( "Couldn't parse upload, IOException.", e );
			
		} catch( ParseException e ){
			
			//Set the offset of the ParseException to bytesRead
			ParseException ex = new ParseException( e.getMessage(), bytesRead );
			throw new RuntimeException( "Couldn't parse upload, ParseException at byte " 
					+ bytesRead + "\n" + ex.getMessage(), ex );
		} 

		return um;
	}

	/**
	 * Parses a String header by invoking KeyValue.parseKV() on the input String. It sets the appropriate field in upload
	 * if the KEY is recognized and ignores KEYs that are not recognized.
	 * 
	 * @param s The String to be parsed into a 
	 * @throws ParseException if the input String can't be split into a (key, value), or if the value of a recognized
	 * key cannot be properly parsed
	 */
	private void parseParam(String s) throws ParseException{
		
		KeyValue kv = KeyValue.parseKV(s);
		
		String key = kv.KEY;
		String value = kv.VALUE;
	
		if (StringUtils.isBlank( key ) || StringUtils.isBlank( value )) {
			
			return;
		}
		try{
			if (key.equalsIgnoreCase(VERSION)){
				this.setVersion(value);
			} else if (key.equalsIgnoreCase(FILELENGTH)){
				this.setFilelength(Long.parseLong(value));
			} else if (key.equalsIgnoreCase(BLOCKSIZE)){
				this.setBlocksize(Long.parseLong(value));
			} else if (key.equalsIgnoreCase(SHA_1)){
				this.setSha1( value );
			} else if (key.equalsIgnoreCase(RELOCATE)){
				this.relocList = parseRelocs( value );
			}
		} catch (NumberFormatException ex) {
			
			throw new ParseException( "Cannot parse " + s + " into a long.", -1 );
		}

		
	}
	
	/**
	 * Reads a line of text from an InputStream without buffering. <p/>
	 * 
	 * This method simply reads the bytes from in one at a time, up to maxSearch, until it either reads
	 * a byte equaling newLine or reaches the end of the stream. It uses the CHARSET encoding to translate 
	 * the bytes read into a String, which it returns with newLine included, or it throws a ParseException
	 * if maxSearch bytes are read without reaching a newLine.<p/>
	 * 
	 * A non-buffering method is used because a buffering reader would likely pull in part of the binary data
	 * from the InputStream. An alternative is to use a BufferedReader with a given buffer size and use
	 * mark and reset to get back binary data pulled into the buffer.
	 * 
	 * @param in The InputStream to read from
	 * @param newLine The byte value of the character marking the end of a line
	 * @param maxSearch The maximum number of bytes allowed in a line
	 * @return The String containing the CHARSET decoded String with newLine included
	 * @throws IOException
	 * @throws ParseException If a newLine byte is not found within maxSearch reads
	 */
	public static String readLine( InputStream in, int newLine, int maxSearch ) throws IOException, ParseException {

		CharBuffer paramChars = null;
		ByteBuffer paramBytes = ByteBuffer.allocate( maxSearch );
		
		int nextByte;
		while ( ( nextByte = in.read() ) > -1 ){
			
			try {
				
				paramBytes.put( (byte) nextByte );
				if ( nextByte == newLine ) {
					
					break;
				}
			
			} catch (BufferOverflowException ex){
				
				throw new ParseException( "Could not find newline within " +  
						maxSearch + " bytes.", 0 );
			}
		}

		paramBytes.flip();
		paramChars =  Charset.forName( CHARSET ).decode( paramBytes );
		return paramChars.toString();

	}
	
	/**
	 * Parses a comma separated list of the form "A-B/C, L-M/N,..." into a List of RelocateRanges
	 * 
	 * @param relocString A String of comma separated RelocateRanges
	 * @return The parsed List of RelocateRange objects
	 * @throws ParseException If any of the listed items cannot be parsed into a RelocateRange
	 */
	private static List<RelocateRange> parseRelocs( String relocString ) throws ParseException{
		
		if (StringUtils.isBlank( relocString )) {
			
			return new ArrayList<RelocateRange>();
		}
		String[] rArray = relocString.split(",");
		
		List<RelocateRange> rList = new ArrayList<RelocateRange>();
		for ( String reloc : rArray ){
			
			rList.add(  RelocateRange.parse( reloc )  );
		}
		
		return rList;
	}
	/**
	 * Returns an InputStream containing the properly formatted data portion of the ZSync upload.
	 * This data portion consists of a sequence of chunks of binary data, each preceded by a Range: start-finish
	 * key value String indicating the target location of the chunk. The chunk of data contains exactly 
	 * finish - start bytes.
	 * 
	 * Note: This method should only be invoked once per Upload object, since the data may be flushed after
	 * the returned stream is closed.
	 * 
	 * @return A stream containing the data portion of the upload
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	
	public InputStream getDataRanges() throws UnsupportedEncodingException, IOException{
		
		List<InputStream> streamList = new ArrayList<InputStream>();
		
		InputStream rangeStream;
		InputStream dataStream;
		
		for ( DataRange dataRange: dataList ){
			
			String rangeString = dataRange.getRange().getRange();
			String rangeKV = LF + paramString( RANGE, rangeString );
			rangeStream = new ByteArrayInputStream( rangeKV.getBytes( CHARSET ) );
			dataStream = dataRange.getInputStream();
			streamList.add( new SequenceInputStream( rangeStream, dataStream ) );
		}
		
		return new SequenceInputStream( new IteratorEnum<InputStream>( streamList ) );
		
	}
	
	/**
	 * Returns an InputStream containing a complete ZSync upload (Params, RelocateRanges, and DataRanges), 
	 * ready to be sent as the body of a PUT request. <p/>
	 * 
	 * Note: In this implementation, any temporary files used to store the DataRanges will be automatically deleted when this stream
	 * is closed, so a second invocation of this method on the same Upload object is likely to throw an exception.
	 * Therefore, this method should be used only once per Upload object.
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public InputStream getInputStream() throws UnsupportedEncodingException, IOException{

		List<InputStream> streamList = new ArrayList<InputStream>();
		streamList.add( IOUtils.toInputStream( getParams() , CHARSET ) );
		streamList.add( IOUtils.toInputStream( getRelocates() , CHARSET ));
		streamList.add( getDataRanges() );
		return new SequenceInputStream( new IteratorEnum<InputStream>( streamList ) );
	}

	/**
	 * Gets the zsync version of the upload sender (client)
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the zsync version of the upload sender (client)
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Gets the checksum for the entire source file
	 */
	public String getSha1() {
		return sha1;
	}

	/**
	 * Sets the checksum for the entire source file, which allow the server to validate the new file
	 * after assembling it.
	 */
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	/**
	 * Gets the blocksize used in the upload. 
	 */
	public long getBlocksize() {
		return blocksize;
	}

	/**
	 * Sets the blocksize used in the upload. The server needs this to translate block ranges into byte ranges
	 */
	public void setBlocksize(long blocksize) {
		this.blocksize = blocksize;
	}

	/**
	 * Gets the length of the (assembled) source file being uploaded
	 */
	public long getFilelength() {
		return filelength;
	}

	/**
	 * Sets the length of the (assembled) source file being uploaded
	 */
	public void setFilelength(long filelength) {
		this.filelength = filelength;
	}
	
	/**
	 * Gets the list of RelocateRanges, which tells the server which blocks of the previous
	 * file to keep, and where to place them in the new file.
	 */
	public List<RelocateRange> getRelocList() {
		return relocList;
	}
	
	/**
	 * Sets the list of RelocateRanges, which tells the server which blocks of the previous
	 * file to keep, and where to place them in the new file.
	 */
	public void setRelocList(List<RelocateRange> relocList) {
		this.relocList = relocList;
	}
	
	/**
	 * Gets the list of uploaded data chunks ( byte Ranges and their associated data )
	 * 
	 */
	public List<DataRange> getDataList() {
		return dataList;
	}
	
	/**
	 * Sets the list of data chunks to be uploaded ( byte Ranges and their associated data )
	 * 
	 */
	public void setDataList(List<DataRange> dataList) {
		this.dataList = dataList;
	}

	/**
	 * An <code>Enumeration</code> wrapper for an Iterator. This is needed in order to construct
	 * a <code>SequenceInputStream</code> (used to concatenate upload sections), which takes an <code>Enumeration</code> argument.
	 * 
	 * @author Nick
	 *
	 * @param <T> The type of object being enumerated
	 */
	private static class IteratorEnum <T> implements Enumeration<T>{

		Iterator<T> iter;
		
		public IteratorEnum( List<T> list ) {
			
			this.iter = list.iterator();
		}

		@Override
		public boolean hasMoreElements() {
			
			return iter.hasNext();
		}

		@Override
		public T nextElement() {
			
			return iter.next();
		}
		
		
	}
	
	/**
	 * An object representing a (Key, Value) pair of Strings.
	 * 
	 * @author Nick
	 *
	 */
	public static class KeyValue {
		
		public String KEY;
		public String VALUE;
		
		public KeyValue ( String key, String value ) {
			
			this.KEY = key;
			this.VALUE = value;
		}
		
		/**
		 * Parses a String of the form "foo: bar" into a KeyValue object whose KEY is the
		 * String preceding the first colon and VALUE is the String following the first colon
		 * ( leading and trailing whitespaces are removed from KEY and VALUE ). A ParseException is
		 * thrown if the input String does not contain a colon.
		 * 
		 * @param kv A String of the form "foo: bar"
		 * @return A KeyValue object with a KEY of "foo" and a VALUE of "bar"
		 * @throws ParseException If no colon is found in <b>kv</b>
		 */
		public static KeyValue parseKV( String kv ) throws ParseException  {
			
			int colonIndex = kv.indexOf(':');
			if (colonIndex == -1){
				
				throw new ParseException("No colon found in \"" + kv + "\"", colonIndex);
			}
			
			String key = kv.substring(0, colonIndex).trim();
			String value = kv.substring(colonIndex + 1).trim();
			
			return new KeyValue( key, value );
		}
	}


}

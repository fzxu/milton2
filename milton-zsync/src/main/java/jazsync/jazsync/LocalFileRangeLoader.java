package jazsync.jazsync;

import com.bradmcevoy.io.StreamUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *
 * @author brad
 */
public class LocalFileRangeLoader implements RangeLoader{

	private File file;

	public LocalFileRangeLoader(File file) {
		this.file = file;
	}
		
	
	public byte[] get(ArrayList<DataRange> rangeList, int range, int blockSize) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for(DataRange r : rangeList) {
			writeRange(r, bout);
		}
		return bout.toByteArray();
	}

	private void writeRange(DataRange r, ByteArrayOutputStream bout) {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			BufferedInputStream bufIn = new BufferedInputStream(fin);
			StreamUtils.readTo(bufIn, bout, true, false, r.getStart(), r.getEnd());						
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		} finally {
			StreamUtils.close(fin);			
		}
	}
	
}

package com.ettrema.zsync;

import com.bradmcevoy.http.Range;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 *
 * @author brad
 */
public class LocalFileRangeLoader implements RangeLoader{

	private File file;
	
	private long bytesDownloaded;

	public LocalFileRangeLoader(File file) {
		this.file = file;
	}
		
	
	public byte[] get(List<Range> rangeList) { 
		System.out.println("LocalFileRangeLoader: get: rangeList: " + rangeList.size());
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for(Range r : rangeList) {
			System.out.println("  get range: " + r.getRange());
			writeRange(r, bout);
		}
		return bout.toByteArray();
	}

	private void writeRange(Range r, ByteArrayOutputStream bout) {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			BufferedInputStream bufIn = new BufferedInputStream(fin);
			bytesDownloaded += (r.getFinish() - r.getStart());
			StreamUtils.readTo(bufIn, bout, true, false, r.getStart(), r.getFinish());						
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		} catch(ReadingException e) {
			throw new RuntimeException(e);
		} catch(WritingException e) {
			throw new RuntimeException(e);
		} finally {
			StreamUtils.close(fin);			
		}
	}

	public long getBytesDownloaded() {
		return bytesDownloaded;
	}
	
	
	
}

package com.ettrema.zsync;

import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.http11.PartialGetHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This is for use when the data available is the exact sequence of ranges
 * that will be requested of the range loader.
 * 
 * For example, when uploading to a http server the client initially sends its
 * metadata to discover the required ranges, and then uploads those ranges.
 * 
 * In this case the server will then use a rangeloader, but the data has
 * already been provided chopped into the requested ranges.
 *
 * @author brad
 */
public class PreChunkedRangeLoader implements RangeLoader {

	private final InputStream in;

	public PreChunkedRangeLoader(InputStream in) {
		this.in = in;
	}
		
	
	@Override
	public byte[] get(List<Range> rangeList) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for( Range r : rangeList ) {
			read(in, out, r.getFinish() - r.getStart());
		}
		return out.toByteArray();
	}

	private void read(InputStream in, ByteArrayOutputStream out, long l) throws IOException {
		PartialGetHelper.sendBytes(in, out, l);
	}
	
}

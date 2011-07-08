package com.ettrema.zsync;

import com.ettrema.httpclient.File;
import com.ettrema.http.DataRange;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.Utils.CancelledException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author HP
 */
public class HttpRangeLoader implements RangeLoader {

	private static final Logger log = LoggerFactory.getLogger(HttpRangeLoader.class);
	
    private final File file;

    public HttpRangeLoader(File file) {
        this.file = file;
    }

    public byte[] get(List<DataRange> rangeList) throws HttpException, CancelledException {
		log.info("get: rangelist: " + rangeList.size());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        file.download(out, null, rangeList);

        byte[] bytes = out.toByteArray();
		int expectedLength = calcExpectedLength(rangeList);
		System.out.println("expected length = " + expectedLength + " actual length: " + bytes.length);
		if( expectedLength != bytes.length) {
			throw new RuntimeException("Got an invalid data size");
		}
		return bytes;
    }

	private int calcExpectedLength(List<DataRange> rangeList) {
		int l = 0;
		for( DataRange r : rangeList) {
			l += (r.getEnd() - r.getStart());
		}
		return l;
	}
}

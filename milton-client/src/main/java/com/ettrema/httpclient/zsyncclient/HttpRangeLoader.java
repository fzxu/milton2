package com.ettrema.httpclient.zsyncclient;

import com.bradmcevoy.http.Range;
import com.ettrema.httpclient.File;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.Utils.CancelledException;
import com.ettrema.zsync.RangeLoader;
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
	
	private long numBytes;

    public HttpRangeLoader(File file) {
        this.file = file;
    }

	@Override
    public byte[] get(List<Range> rangeList) throws HttpException, CancelledException {
		log.info("get: rangelist: " + rangeList.size());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        file.download(out, null, rangeList);

        byte[] bytes = out.toByteArray();
		int expectedLength = calcExpectedLength(rangeList);
//		if( expectedLength != bytes.length) {
//			log.warn("Got an unexpected data size!!");
//		}
		numBytes += bytes.length;
		return bytes;
    }

	public static  int calcExpectedLength(List<Range> rangeList) {
		int l = 0;
		for( Range r : rangeList) {
			l += (r.getFinish() - r.getStart());
		}
		return l;
	}

	public long getBytesDownloaded() {
		return numBytes;
	}
}

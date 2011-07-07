package com.ettrema.zsync;

import com.ettrema.httpclient.File;
import com.ettrema.http.DataRange;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.Utils.CancelledException;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 *
 * @author HP
 */
public class HttpRangeLoader implements RangeLoader {

    private final File file;

    public HttpRangeLoader(File file) {
        this.file = file;
    }

    public byte[] get(List<DataRange> rangeList) throws HttpException, CancelledException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        file.download(out, null, rangeList);

        return out.toByteArray();
    }
}

package com.ettrema.httpclient;

/**
 *
 * @author j2ee
 */
public interface ProgressListener {

    void onProgress( int percent, String fileName, int bytesPerSec );

    void onComplete( String fileName );
}

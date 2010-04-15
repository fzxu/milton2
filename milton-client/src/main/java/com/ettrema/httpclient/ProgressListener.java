package com.ettrema.httpclient;

/**
 *
 * @author j2ee
 */
public interface ProgressListener {

    void onProgress( int percent, String fileName );

    void onComplete( String fileName );
}

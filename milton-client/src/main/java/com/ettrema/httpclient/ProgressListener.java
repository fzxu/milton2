package com.ettrema.httpclient;

/**
 *
 * @author j2ee
 */
public interface ProgressListener {

    void onProgress( int percent, String fileName );

    void onComplete( String fileName );

    /**
     * This is a means for the UI to inform that process that the user has
     * cancelled the optioation
     *
     * @return
     */
    boolean isCancelled();
}

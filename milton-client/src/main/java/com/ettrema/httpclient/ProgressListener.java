package com.ettrema.httpclient;

/**
 *
 * @author j2ee
 */
public interface ProgressListener {

	/**
	 * Called on every read operation. If you implement any logic in here
	 * is must be fast!
	 * 
	 * @param bytes 
	 */
	void onRead(long bytes);
	
	/**
	 * Called occasionally, after a reasonable period has passed so is suitable
	 * for GUI updates
	 * 
	 * @param percent
	 * @param fileName 
	 */
    void onProgress( int percent, String fileName );

    void onComplete( String fileName );

    /**
     * This is a means for the UI to inform that process that the user has
     * cancelled the optioation
	 * 
	 * If the implementation returns true the operation will abort
     *
     * @return
     */
    boolean isCancelled();
}

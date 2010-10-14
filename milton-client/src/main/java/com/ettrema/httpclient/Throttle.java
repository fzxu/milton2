package com.ettrema.httpclient;

/**
 *
 * @author brad
 */
public interface Throttle {
    
    /**
     * Called when reading data to be sent to the server.
     *
     * This can be used to implement a throttle
     *
     * @param len - the length of data read
     */
    public void onRead(int len);
}

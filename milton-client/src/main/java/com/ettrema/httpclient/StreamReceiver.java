package com.ettrema.httpclient;

import java.io.InputStream;

/**
 *
 * @author mcevoyb
 */
public interface StreamReceiver {
    void receive( InputStream in );
}

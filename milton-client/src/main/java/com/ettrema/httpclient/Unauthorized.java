package com.ettrema.httpclient;

/**
 *
 * @author mcevoyb
 */
public class Unauthorized extends HttpException {
    private static final long serialVersionUID = 1L;

    public Unauthorized( int result, String href ) {
        super( result, href );
    }
}

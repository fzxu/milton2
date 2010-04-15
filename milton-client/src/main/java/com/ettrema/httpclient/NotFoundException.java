package com.ettrema.httpclient;

/**
 *
 * @author mcevoyb
 */
public class NotFoundException extends HttpException {
    private static final long serialVersionUID = 1L;

    public NotFoundException( int result, String href ) {
        super( result, href );
    }
}

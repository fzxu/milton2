package com.ettrema.httpclient;

/**
 *
 * @author mcevoyb
 */
public class ConflictException extends HttpException {
    private static final long serialVersionUID = 1L;

    public ConflictException( int result, String href ) {
        super( result, href );
    }
}

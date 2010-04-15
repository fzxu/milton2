package com.ettrema.httpclient;

/**
 *
 * @author mcevoyb
 */
public class BadRequestException extends HttpException {
    private static final long serialVersionUID = 1L;

    public BadRequestException( int result, String href ) {
        super( result, href );
    }
}

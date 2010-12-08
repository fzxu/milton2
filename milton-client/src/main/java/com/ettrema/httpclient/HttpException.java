package com.ettrema.httpclient;

/**
 *
 * @author mcevoyb
 */
public abstract class HttpException extends Exception {
    private static final long serialVersionUID = 1L;

    final int result;
    final String href;

    public HttpException( int result, String href ) {
        super( "http error: " + result + " - " + href );
        this.result = result;
        this.href = href;
    }
}

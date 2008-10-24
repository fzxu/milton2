package com.bradmcevoy.http;

public class RequestParseException extends Exception {
    public RequestParseException(String msg, Throwable cause) {
        super(msg,cause);
    }
    
}

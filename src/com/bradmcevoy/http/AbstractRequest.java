package com.bradmcevoy.http;

import java.util.Date;

public abstract class AbstractRequest implements Request{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractRequest.class);
    
    public static final int INFINITY = 3; // To limit tree browsing a bit
    
    protected abstract String getRequestHeader(Request.Header header);
    
    @Override
    public Date getIfModifiedHeader() {
        String s = getRequestHeader( Request.Header.IF_MODIFIED );
        if( s == null || s.length() == 0 ) return null;
        
        try {
            return DateUtils.parseDate(s);
        } catch (DateUtils.DateParseException ex) {
            log.error("Unable to parse date: " + s,ex);
            return null;
        }
    }

    @Override
    public String getAcceptHeader() {
        return getRequestHeader(Request.Header.ACCEPT);
    }

    @Override
    public String getRefererHeader() {
        return getRequestHeader(Request.Header.REFERER);
    }

    @Override
    public String getContentTypeHeader() {
        return getRequestHeader(Request.Header.CONTENT_TYPE);
    }

    @Override
    public String getAcceptEncodingHeader() {
        return getRequestHeader(Request.Header.ACCEPT_ENCODING);
    }

    
    

    @Override
    public int getDepthHeader() {
        String depthStr = getRequestHeader( Request.Header.DEPTH );
        if (depthStr == null) {
            return INFINITY;
        } else {
            if (depthStr.equals("0")) {
                return 0;
            } else if (depthStr.equals("1")) {
                return 1;
            } else if (depthStr.equals("infinity")) {
                return INFINITY;
            } else {
                log.warn("Unknown depth value: " + depthStr);
                return INFINITY;
            }
        }        
    }
    
    @Override
    public String getHostHeader() {
        return getRequestHeader(Header.HOST);
    }

    @Override
    public String getDestinationHeader() {
        return getRequestHeader(Header.DESTINATION);
    }

    @Override
    public Long getContentLengthHeader() {
        String s = getRequestHeader(Header.CONTENT_LENGTH);
        if( s == null || s.length() == 0 ) return null;
        try {
            long l = Long.parseLong(s);
            return l;
        } catch (NumberFormatException ex) {
            log.warn("Couldnt parse content length header: " + s);
            return null;
        }
    }

    @Override
    public String getTimeoutHeader() {
        return getRequestHeader(Header.TIMEOUT);
    }

    @Override
    public String getIfHeader() {
        return getRequestHeader(Header.IF);
    }

    @Override
    public String getLockTokenHeader() {
        return getRequestHeader(Header.LOCK_TOKEN);
    }

    
    
    
    @Override
    public final String getAbsolutePath() {
        return stripToPath(getAbsoluteUrl());
    }
    
    public static String stripToPath(String url) {
        int i = url.indexOf("/",8);
        return url.substring(i);
    }

}

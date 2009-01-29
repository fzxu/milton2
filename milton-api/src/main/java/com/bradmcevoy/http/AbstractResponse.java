package com.bradmcevoy.http;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResponse implements Response {

    private static final Logger log = LoggerFactory.getLogger(AbstractResponse.class);


    protected DateFormat hdf;
    protected Long contentLength;
    

    public AbstractResponse() {
        hdf = new SimpleDateFormat(DateUtils.PATTERN_RESPONSE_HEADER);
        hdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }



    public void setResponseHeader(Response.Header header, String value) {
        //log.debug("setResponseHeader: " + header.code + " - " + value);
        setNonStandardHeader(header.code, value);
    }

    public String getResponseHeader(Response.Header header) {
        return getNonStandardHeader(header.code);
    }

    public void setContentEncodingHeader(ContentEncoding encoding) {
        setResponseHeader(Response.Header.CONTENT_ENCODING, encoding.code);
    }

    public Long getContentLength() {
        return contentLength;
    }
    
    
    public void setDateHeader(Date date) {
        //Date: Tue, 15 Nov 1994 08:12:31 GMT
        if (date == null)
            return;
        String fmt = hdf.format(date);
        setResponseHeader(Header.DATE, fmt);
    }

    public void setAuthenticateHeader(String realm) {
        setResponseHeader(Header.WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
    }

    public void setContentRangeHeader(long start, long finish, Long totalLength) {
        String l = totalLength == null ? "*" : totalLength.toString();
        String s = "bytes " + start + "-" + finish + "/" + l;
        setResponseHeader(Header.CONTENT_RANGE, s);
    }

    public void setContentLengthHeader(Long totalLength) {
        String s = totalLength == null ? "" : totalLength.toString();
        log.debug("setting content length: " + s );
        setResponseHeader(Header.CONTENT_LENGTH, s);
        this.contentLength = totalLength;
        
    }

    public void setContentTypeHeader(String type) {
        setResponseHeader(Header.CONTENT_TYPE, type);
    }

    public String getContentTypeHeader() {
        return getResponseHeader(Header.CONTENT_TYPE);
    }

    public void setCacheControlMaxAgeHeader(Long delta) {
        setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.MAX_AGE.code + "=" + delta);
    }

    public void setExpiresHeader(Date expiresAt) {
        if (expiresAt == null) {
            setResponseHeader(Header.EXPIRES, null);
        } else {
            String fmt = hdf.format(expiresAt);
            setResponseHeader(Header.EXPIRES, fmt);
        }
    }

    public void setEtag(String uniqueId) {
        setResponseHeader(Header.ETAG, uniqueId);
    }

    public void setLastModifiedHeader(Date date) {
        if (date == null)
            return;
        String fmt = hdf.format(date);
        setResponseHeader(Header.LAST_MODIFIED, fmt);
    }

    public void setCacheControlNoCacheHeader() {
        setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.NO_CACHE.code);
    }

    public void setLocationHeader(String redirectUrl) {
        setResponseHeader(Header.LOCATION, redirectUrl);
    }

    public void setAllowHeader(List<Request.Method> methodsAllowed) {
        if (methodsAllowed == null || methodsAllowed.size() == 0)
            return;
        StringBuffer sb = null;
        for (Request.Method m : methodsAllowed) {
            if (sb == null) {
                sb = new StringBuffer();
            } else {
                sb.append(",");
            }
            sb.append(m.code);
        }
        setResponseHeader(Header.ALLOW, sb.toString());
    }

    public void setLockTokenHeader(String s) {
        setResponseHeader(Header.LOCK_TOKEN, s);
    }

    public void setDavHeader(String supportedLevels) {
        setResponseHeader(Header.DAV, supportedLevels);
    }

    public void close() {
    }

    public void sendRedirect(String url) {
        setStatus(Response.Status.SC_MOVED_TEMPORARILY);
        setLocationHeader( url );        
    }

    public void write(String s) {
        try {
            this.getOutputStream().write(s.getBytes());
        } catch (IOException ex) {
            log.warn("Exception writing to output. Probably client closed connection", ex);
        }
    }    
    
}

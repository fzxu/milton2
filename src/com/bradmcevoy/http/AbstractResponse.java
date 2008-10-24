package com.bradmcevoy.http;

import com.bradmcevoy.http.Response.CacheControlResponse;
import com.bradmcevoy.http.Response.Header;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public abstract class AbstractResponse implements Response{
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResponse.class);
    
    protected DateFormat hdf;
        
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

    @Override
    public void setContentEncodingHeader(ContentEncoding encoding) {
        setResponseHeader(Response.Header.CONTENT_ENCODING, encoding.code);
    }
    
    
    @Override
    public void setDateHeader(Date date) {
        //Date: Tue, 15 Nov 1994 08:12:31 GMT
        if( date == null ) return ;
        String fmt = hdf.format(date);
        setResponseHeader( Header.DATE,fmt );
    }
    
    @Override
    public void setAuthenticateHeader(String realm) {
        setResponseHeader( Header.WWW_AUTHENTICATE,"Basic realm=\"" + realm + "\"" );
    }

    @Override
    public void setContentRangeHeader(long start, long finish, Long totalLength) {
        String l = totalLength==null ? "*" : totalLength.toString();
        String s = "bytes " + start + "-" + finish + "/" + l;
        setResponseHeader(Header.CONTENT_RANGE,s);
    }

    @Override
    public void setContentLengthHeader(Long totalLength) {
        String s = totalLength==null ? "*" : totalLength.toString();
        setResponseHeader( Header.CONTENT_LENGTH,s);
    }

    @Override
    public void setContentTypeHeader(String type) {
        setResponseHeader( Header.CONTENT_TYPE,type);
    }

    @Override
    public String getContentTypeHeader() {
        return getResponseHeader( Header.CONTENT_TYPE );
    }
        
    @Override
    public void setCacheControlMaxAgeHeader(Long delta) {
        setResponseHeader(Header.CACHE_CONTROL,CacheControlResponse.MAX_AGE.code + "=" + delta);
    }

    @Override
    public void setExpiresHeader(Date expiresAt) {
        if(expiresAt == null ) {
            setResponseHeader(Header.EXPIRES, null);
        } else {
            String fmt = hdf.format(expiresAt);
            setResponseHeader(Header.EXPIRES, fmt);
        }
    }

    @Override
    public void setEtag(String uniqueId) {
        setResponseHeader(Header.ETAG, uniqueId);
    }
    
    
    
    
    @Override
    public void setLastModifiedHeader(Date date) {
        if( date == null ) return ;
        String fmt = hdf.format(date);
        setResponseHeader(Header.LAST_MODIFIED, fmt);
    }   
    
    @Override
    public void setCacheControlNoCacheHeader() {
        setResponseHeader(Header.CACHE_CONTROL,CacheControlResponse.NO_CACHE.code);
    }

    @Override
    public void setLocationHeader(String redirectUrl) {
        setResponseHeader(Header.LOCATION,redirectUrl);
    }   

    @Override
    public void setAllowHeader(List<Request.Method> methodsAllowed) {
        if( methodsAllowed == null || methodsAllowed.size() == 0 ) return ;
        StringBuffer sb = null;
        for( Request.Method m : methodsAllowed ) {
            if( sb == null ) {
                sb = new StringBuffer();
            } else {
                sb.append(",");
            }
            sb.append(m.code);
        }
        setResponseHeader(Header.ALLOW,sb.toString());
    }

    @Override
    public void setLockTokenHeader(String s) {
        setResponseHeader(Header.LOCK_TOKEN,s);
    }    
    
    @Override
    public void setDavHeader(String supportedLevels) {
        setResponseHeader(Header.DAV,supportedLevels);
    }   

    @Override
    public void close() {
    }    
}

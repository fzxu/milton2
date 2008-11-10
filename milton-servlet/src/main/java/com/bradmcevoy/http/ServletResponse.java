package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletResponse extends AbstractResponse {

    private Logger log = LoggerFactory.getLogger(GetHandler.class);
    
    private static ThreadLocal<HttpServletResponse> tlResponse = new ThreadLocal<HttpServletResponse>();
    
    public static HttpServletResponse getResponse() {
        return tlResponse.get();
    }
    
    private final HttpServletResponse r;
//    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private Long contentLength;
    private Response.Status status;
    private Map<String,String> headers = new HashMap<String, String>();
    
    public ServletResponse(HttpServletResponse r) {
        this.r = r;
        tlResponse.set(r);
    }

    public String getNonStandardHeader(String code) {
        return headers.get(code);
    }
    
    public void setNonStandardHeader(String name, String value) {
        r.addHeader(name,value);
        headers.put(name, value);
    }

    public void setStatus(Response.Status status) {
        r.setStatus(status.code);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
    
    @Override
    public void setContentLengthHeader(Long totalLength) {
        contentLength = totalLength;
        super.setContentLengthHeader(totalLength);
    }
    
    public OutputStream getOutputStream() {        
        try {
//        return out;
            return r.getOutputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        log.debug("closing: contentlength: " + this.contentLength);
        try {
            r.flushBuffer();
            r.getOutputStream().flush();
//        try {
//            byte[] arr = out.toByteArray();
//            long length = (long)arr.length;
//            if( contentLength == null ) setContentLengthHeader(length);
//            OutputStream o = r.getOutputStream();
//            o.write( arr );
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
//        try {
//            byte[] arr = out.toByteArray();
//            long length = (long)arr.length;
//            if( contentLength == null ) setContentLengthHeader(length);
//            OutputStream o = r.getOutputStream();
//            o.write( arr );
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }        
    }
}

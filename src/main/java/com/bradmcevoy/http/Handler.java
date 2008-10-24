package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Handler {
    
    private Logger log = LoggerFactory.getLogger(Handler.class);
    
    public static final String METHOD_NOT_ALLOWED_HTML = "<html><body><h1>Method Not Allowed</h1></body></html>";
    
    protected final HttpManager manager;
    
    public abstract void process(HttpManager httpManager, Request request, Response response);
    
    protected abstract boolean isCompatible(Resource r);
    
    /** The method that this handler handles
     */
    abstract Request.Method method();

    
    public Handler(HttpManager manager) {
        this.manager = manager;                
    }
    
    protected boolean checkAuthorisation(Resource handler, Request request) {
        Auth auth = request.getAuthorization();
        if( auth != null ) {
            Object authTag = handler.authenticate(auth.user,auth.password);
            if( authTag == null ) {
                log.warn("failed to authenticate");
                auth = null;
            } else {
                auth.setTag(authTag);
            }
        } else {
            auth = manager.getSessionAuthentication(request);
        }
        
        
        boolean authorised = handler.authorise(request,request.getMethod(),auth);
        if( !authorised ) {
            log.warn("Not authorised, requesting basic authentication");
            return false;
        } else {
            return true;
        }
    }

    
    
    protected void respondUnauthorised(Resource resource, Response response) {
        log.debug("requesting authorisation");
        response.setStatus(Response.Status.SC_UNAUTHORIZED);
        response.setAuthenticateHeader( resource.getRealm() );        
    }

    protected void respondMethodNotAllowed(Resource res, Response response) {
        log.debug("method not allowed. handler: " + this.getClass().getName() + " resource: " + res.getClass().getName());
        try {
            response.setStatus(Response.Status.SC_METHOD_NOT_ALLOWED);
            OutputStream out = response.getOutputStream();
            out.write(METHOD_NOT_ALLOWED_HTML.getBytes());
        } catch (IOException ex) {
            log.warn("exception writing content");
        }
    }

    protected void respondConflict(Resource resource, Response response) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO        
    }
    
    protected void respondRedirect(Response response, String redirectUrl) {
        if( redirectUrl == null ) throw new NullPointerException("redirectUrl cannot be null");
        response.setStatus(Response.Status.SC_MOVED_TEMPORARILY);
        response.setLocationHeader( redirectUrl );
    }

    
    
    protected  String generateNamespaceDeclarations() {
//            return " xmlns:" + nsWebDav.abbrev + "=\"" + nsWebDav.url + "\"";
        return " xmlns:D" + "=\"DAV:\"";
    }        

    protected void output(final Response response, final String s) {
        PrintWriter pw = new PrintWriter(response.getOutputStream(),true);
        pw.print(s);
        pw.flush();
    }

    
    protected class Namespace {
        String abbrev;
        String url;
    }        
    
    protected void _respondWithContent(Request request, Response response, GetableResource resource,Map<String,String> params) {
        setStatus(resource, response, request);
        response.setDateHeader(new Date());
        String etag = resource.getUniqueId();
        if( etag != null ) {
            response.setEtag(etag);
        }
        Long contentLength = resource.getContentLength();
        if( contentLength != null ) { // often won't know until rendered
            response.setContentLengthHeader( contentLength ); 
        }
        String acc = request.getAcceptHeader();
        response.setContentTypeHeader( resource.getContentType(acc) );
        setCacheControl(resource, response);        
        response.setLastModifiedHeader(resource.getModifiedDate());
        long t = System.currentTimeMillis();
        sendContent(request, response,resource,params);
        t = System.currentTimeMillis() - t;
//        log.debug("sendContent: " + t + "ms");
    }
    
    protected void sendContent(Request request, Response response, GetableResource resource,Map<String,String> params) {
        sendContent(request, response, resource, params, null);
    }
    
    protected void sendContent(Request request, Response response, GetableResource resource,Map<String,String> params, Range range) {
        OutputStream out = outputStreamForResponse(request, response, resource);
        try {
            resource.sendContent(out,null,params);
            if( out != response.getOutputStream() ) {
                out.flush();
                out.close();
                response.getOutputStream().flush();
            } else {
                out.flush();                
            }
        } catch (IOException ex) {
            log.warn("IOException sending content");
        }                
    }   
    
    protected OutputStream outputStreamForResponse(Request request, Response response, GetableResource resource) {
        OutputStream outToUse = response.getOutputStream();
        String acc = request.getAcceptHeader();
        String contentType = resource.getContentType(acc);
//        log.debug("outputStreamForResponse: accepts: " + acc + " contentType: " + contentType);
        if( contentType != null ) {
            contentType = contentType.toLowerCase();
            boolean contentIsCompressable = contentType.contains("text") || contentType.contains("css") || contentType.contains("js") || contentType.contains("javascript");
            if( contentIsCompressable ) {
                String accepts = request.getAcceptEncodingHeader();
                boolean supportsGzip = (accepts != null && accepts.toLowerCase().indexOf("gzip") > -1);
                if( supportsGzip ) {
//                    log.debug("..responding with GZIPed content");
                    try {
                        response.setContentEncodingHeader(Response.ContentEncoding.GZIP);
                        outToUse = new GZIPOutputStream(outToUse);
                    } catch (IOException ex) {
                        throw new RuntimeException("Exception wrapping outputstream with GZIP output stream", ex);
                    }
                }
            }
        }
        return outToUse;
    }
    
    
    protected void setStatus(final GetableResource resource, final Response response, final Request request) {
        response.setStatus( Response.Status.SC_OK );
    }
    
    
    protected void setCacheControl(final GetableResource resource, final Response response) {        
        Long delta = resource.getMaxAgeSeconds();
        if( delta != null ) {
            response.setCacheControlMaxAgeHeader( resource.getMaxAgeSeconds() );
            Date expiresAt = calcExpiresAt(resource.getModifiedDate(), delta.longValue());
            response.setExpiresHeader(expiresAt);
        } else {
            response.setExpiresHeader(null);
            response.setCacheControlNoCacheHeader( );
        }        
    }    
    
    protected Date calcExpiresAt(Date modifiedDate, long deltaSeconds) {
        long deltaMs = deltaSeconds*1000;
        long expiresAt = System.currentTimeMillis() + deltaMs;
        return new Date(expiresAt);        
    }
    
}
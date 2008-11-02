package com.bradmcevoy.http;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetHandler extends ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(GetHandler.class);
    
    public GetHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        GetableResource r = (GetableResource)resource;
        if( checkConditional(r,request) ) {
            respondNotModified(r,response,request);
            return;
        }
        
        Map<String, String> params = new HashMap<String,String>();
        Map<String, FileItem> files = new HashMap<String,FileItem>();

        try {
            request.parseRequestParameters(params,files);
        } catch (RequestParseException ex) {
            log.warn("exception parsing request. probably interrupted upload",ex);
            return ;
        }
        manager.onGet(request, response, resource, params);
        respondWithContent(request,response,r, params);
    }
        
    public Range getRange(Request requestInfo) {
        return null; // TODO: parse range header. Note we don't support multiple
    }
    
    
    /** Return true if the resource has not been modified
     */
    protected boolean checkConditional(GetableResource resource, Request request) {
        if( checkIfMatch(resource,request) ) {
            return true;
        }
        if( checkIfModifiedSince(resource,request)) {
            return true;
        }
        if( checkIfNoneMatch(resource,request)) {
            return true;
        }
        return false;
    }
    
    protected void respondNotModified(GetableResource resource, Response response, Request request) {
        log.debug("not modified");
        response.setDateHeader(new Date());
        String acc = request.getAcceptHeader();
        response.setContentTypeHeader( resource.getContentType(acc) );
        response.setStatus(Response.Status.SC_NOT_MODIFIED);
    }
    
    protected boolean checkIfMatch(GetableResource handler, Request requestInfo) {
        return false;   // TODO: not implemented
    }
    
    protected boolean checkIfModifiedSince(GetableResource handler, Request requestInfo) {
        Date dtRequest = requestInfo.getIfModifiedHeader();
        if( dtRequest == null ) return false;
        Date dtCurrent = handler.getModifiedDate();
        if( dtCurrent == null ) return true;
        return ( dtCurrent.compareTo(dtRequest) < 0 );
    }
    
    protected boolean checkIfNoneMatch(GetableResource handler, Request requestInfo) {
        return false;   // TODO: not implemented
    }
    

    @Override
    protected Request.Method method() {
        return Request.Method.GET;
    }
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof GetableResource);
    }                


    protected void respondWithContent(Request request, Response response, GetableResource resource, Map<String,String> params) {        
        _respondWithContent(request,response,resource,params);
    }

    @Override
    public void setStatus(final GetableResource resource, final Response response, final Request request) {
        Range range = getRange(request);
        if (range == null) {
            response.setStatus( Response.Status.SC_OK );
        }  else {
            response.setStatus( Response.Status.SC_PARTIAL_CONTENT );
            response.setContentRangeHeader(range.start, range.finish, resource.getContentLength());
        }
    }

    @Override
    protected void sendContent(Request request, Response response, GetableResource resource,Map<String,String> params) {
        Range range = getRange(request);
        sendContent(request, response, resource, params, range);
    }
    
    @Override
    protected boolean doCheckRedirect(Request request, Response response,Resource resource) {
        String redirectUrl = resource.checkRedirect(request);
        if( redirectUrl != null ) {
//            log.debug("redirecting to: " + redirectUrl);
            respondRedirect( response, redirectUrl );
            return true;
        } else {
            return false;
        }
    }    
}
package com.bradmcevoy.http;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetHandler extends ExistingEntityHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GetHandler.class);
    
    public GetHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        log.debug("process: " + request.getAbsolutePath());
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
        sendContent(request, response, r, params);
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
        getResponseHandler().respondNotModified(resource, response, request);
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


    protected void sendContent(Request request, Response response, GetableResource resource,Map<String,String> params) {
        Range range = getRange(request);
        if( range != null ) {
            getResponseHandler().respondPartialContent(resource, response, request, params, range);
        } else {
            getResponseHandler().respondContent(resource, response, request, params);
        }
    }
    
    @Override
    protected boolean doCheckRedirect(Request request, Response response,Resource resource) {
        String redirectUrl = resource.checkRedirect(request);
        if( redirectUrl != null ) {
            respondRedirect( response, request, redirectUrl );
            return true;
        } else {
            return false;
        }
    }    
}
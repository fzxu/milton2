package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PutHandler extends NewEntityHandler {
    
    private static final Logger log = LoggerFactory.getLogger(PutHandler.class);
    
    public PutHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    public Request.Method method() {
        return Method.PUT;
    }       
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof PutableResource);
    }        

    @Override
    protected void process(HttpManager milton, Request request, Response response, CollectionResource resource, String newName) {
        PutableResource r = (PutableResource) resource;        
        log.debug("process: putting to: " + r.getName() );
        try {
            Long l = request.getContentLengthHeader();
            String ct = request.getContentTypeHeader();
            log.debug("PutHandler: creating resource of type: " + ct);
            r.createNew(newName, request.getInputStream(), l, ct );
            log.debug("PutHandler: DONE creating resource");
        } catch (IOException ex) {
            log.warn("IOException reading input stream. Probably interrupted upload: " + ex.getMessage());
            return;
        }
        getResponseHandler().respondCreated(resource, response, request);
        
        log.debug("process: finished");
    }
}
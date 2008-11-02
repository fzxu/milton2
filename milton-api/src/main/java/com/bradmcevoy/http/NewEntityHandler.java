package com.bradmcevoy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.Path;

public abstract class NewEntityHandler extends Handler {
    
    private Logger log = LoggerFactory.getLogger(NewEntityHandler.class);
    
    public NewEntityHandler(HttpManager manager) {
        super(manager);
    }
    
    /** Implement method specific processing. The resource can be safely cast as
     *  the appropriate method specific interface if isCompatible has been implemented
     *  correctly
     */
    protected abstract void process(HttpManager milton, Request request, Response response, CollectionResource resource, String newName);
    
    
    @Override
    public void process(HttpManager manager, Request request, Response response) {
        String host = request.getHostHeader();
        String url = HttpManager.decodeUrl(request.getAbsolutePath());
        String name;
        log.debug("process request: host: " + host + " url: " + url);
        
        Path path = Path.path(url);
        name = path.getName();
        path = path.getParent();
        url = path.toString();
        
        Resource r = manager.getResourceFactory().getResource(host, url);
        if( r != null ) {
            log.debug("process: resource: " + r.getClass().getName());
            process(request,response,r, name);
        } else {
            response.setStatus(Response.Status.SC_NOT_FOUND);
        }
    }
    
    protected void process(Request request, Response response, Resource resource, String name) {        
        if( !checkAuthorisation(resource,request) ) {
            respondUnauthorised(resource,response);
            return ;
        }
        
        if( !isCompatible(resource) ) {
            respondMethodNotAllowed(resource,response);
            return ;
        }
        
//        String redirectUrl = resource.checkRedirect(request);
//        if( redirectUrl != null ) {
//            respondRedirect( response, redirectUrl );
//            return ;
//        }
        
        if( resource instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource)resource;
            process(manager,request,response,col, name);
        } else {
            respondConflict(resource, response);
        }
    }
}

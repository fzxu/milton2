package com.bradmcevoy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyHandler extends ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(CopyHandler.class);
    
    public CopyHandler(HttpManager manager) {
        super(manager);
    }

    @Override
    protected Request.Method method() {
        return Request.Method.COPY;
    }

    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof CopyableResource);
    }
    
    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        CopyableResource r = (CopyableResource) resource;
        String sDest = request.getDestinationHeader();  
        sDest = HttpManager.decodeUrl(sDest);
        Dest dest = new Dest(request.getHostHeader(),sDest);
        Resource rDest = manager.getResourceFactory().getResource(dest.host, dest.url);        
        log.debug("process: copying from: " + r.getName() + " -> " + dest.url + "/" + dest.name);

        if( rDest == null ) {
            log.debug("process: destination parent does not exist: " + sDest);
            response.setStatus( Response.Status.SC_CONFLICT );
            output(response, "Destination does not exist: " + sDest);
        } else if( !(rDest instanceof CollectionResource) ) {
            log.debug("process: destination exists but is not a collection");
            response.setStatus( Response.Status.SC_CONFLICT );
            output(response, "Destination exists but is not a collection: " + sDest);
        } else { 
            log.debug("process: moving resource to: " + rDest.getName());
            r.copyTo( (CollectionResource)rDest, dest.name );
            response.setStatus( Response.Status.SC_CREATED );
        }
        log.debug("process: finished");
    }
    
}

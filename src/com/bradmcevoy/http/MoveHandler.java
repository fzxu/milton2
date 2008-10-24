package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;


public class MoveHandler extends ExistingEntityHandler {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MoveHandler.class);
    
    public MoveHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    public Request.Method method() {
        return Method.MOVE;
    }
        
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof MoveableResource);
    }        

    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {        
        MoveableResource r = (MoveableResource) resource;
        String sDest = request.getDestinationHeader();   
        sDest = HttpManager.decodeUrl(sDest);
        Dest dest = new Dest(request.getHostHeader(),sDest);
        Resource rDest = manager.getResourceFactory().getResource(dest.host, dest.url);        
        log.debug("process: moving from: " + r.getName() + " -> " + dest.url + "/" + dest.name);
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
            r.moveTo( (CollectionResource)rDest, dest.name );
            response.setStatus( Response.Status.SC_CREATED );
        }
        log.debug("process: finished");
    }
}
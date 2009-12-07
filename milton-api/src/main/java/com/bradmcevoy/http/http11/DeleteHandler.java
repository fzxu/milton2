package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;


public class DeleteHandler implements ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(DeleteHandler.class);

    private final Http11ResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;
    private final ResourceHandlerHelper resourceHandlerHelper;

    public DeleteHandler( Http11ResponseHandler responseHandler, HandlerHelper handlerHelper ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        this.resourceHandlerHelper = new ResourceHandlerHelper( handlerHelper, responseHandler );
    }

    public DeleteHandler( Http11ResponseHandler responseHandler ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = new HandlerHelper();
        this.resourceHandlerHelper = new ResourceHandlerHelper( handlerHelper, responseHandler );
    }

    public String[] getMethods() {
        return new String[]{Method.DELETE.code};
    }
    
    @Override
    public boolean isCompatible(Resource handler) {
        return (handler instanceof DeletableResource);
    }        

    @Override
    public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.process( manager, request, response, this );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this );
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        log.debug("DELETE: " + request.getAbsoluteUrl());

        //check that no children are locked
        //checkForLock(resource, request);
        if( handlerHelper.isLockedOut(request, resource)) {
        	log.info("Could not delete. Is locked");
            response.setStatus(Status.SC_LOCKED);
            return;
        }
        
        
        DeletableResource r = (DeletableResource) resource;
        try {
            delete( r );
            response.setStatus(Response.Status.SC_NO_CONTENT);
            log.debug("deleted ok");
        } catch(CantDeleteException e) {
            log.error("failed to delete: " + request.getAbsoluteUrl(),e);
            responseHandler.respondDeleteFailed(request, response, e.resource, e.status);
        }
        
    }

    private void delete(DeletableResource r) throws CantDeleteException {
        if( r instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource)r;
            List<Resource> list = new ArrayList<Resource>();
            list.addAll( col.getChildren() );
            for( Resource rChild : list ) {
                if( rChild instanceof DeletableResource ) {
                    DeletableResource rChildDel = (DeletableResource)rChild;
                    delete( rChildDel );
                } else {
                    throw new CantDeleteException(rChild, Response.Status.SC_LOCKED);
                }
            }
        }
        r.delete();
    }


    
    public static class CantDeleteException extends Exception {
        
        private static final long serialVersionUID = 1L;
        public final Resource resource;
        public final Response.Status status;
        
        CantDeleteException(Resource r,Response.Status status) {
            this.resource = r;
            this.status = status;
        }
    }
}

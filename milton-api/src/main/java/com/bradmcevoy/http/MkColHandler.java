package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.Request.Method;

    
public class MkColHandler extends NewEntityHandler {
    public MkColHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    public Method method() {
        return Method.MKCOL;
    }
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof MakeCollectionableResource);
    }

    @Override
    protected void process(HttpManager milton, Request request, Response response, CollectionResource resource, String newName) throws ConflictException, NotAuthorizedException{
        MakeCollectionableResource existingCol = (MakeCollectionableResource)resource;
        Resource r = existingCol.child(newName);
        if( r == null ) {
            existingCol.createCollection(newName);
            response.setStatus(Response.Status.SC_CREATED);
        } else {
            throw new ConflictException(resource);
        }
    }
}
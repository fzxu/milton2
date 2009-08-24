package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MkColHandler extends NewEntityHandler {

    private static final Logger log = LoggerFactory.getLogger(MkColHandler.class);

    public MkColHandler( HttpManager manager ) {
        super( manager );
    }

    @Override
    public Method method() {
        return Method.MKCOL;
    }

    @Override
    protected boolean isCompatible( Resource handler ) {
        return ( handler instanceof MakeCollectionableResource );
    }

    @Override
    protected void process( HttpManager milton, Request request, Response response, CollectionResource resource, String newName ) throws ConflictException, NotAuthorizedException {
        MakeCollectionableResource existingCol = (MakeCollectionableResource) resource;
        try {
            //For litmus test and RFC support
            if( request.getInputStream().read() > -1 ) //This should be empty
            {
                response.setStatus( Response.Status.SC_UNSUPPORTED_MEDIA_TYPE );
                return;
            }
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
        Resource existingChild = existingCol.child( newName );
        if( existingChild != null ) {
            log.debug("item already exists: " + existingChild.getName());
            throw new ConflictException( existingChild );
        }
        CollectionResource made = existingCol.createCollection( newName );
        if( made == null ) {
            response.setStatus( Response.Status.SC_METHOD_NOT_ALLOWED );
        } else {
            response.setStatus( Response.Status.SC_CREATED );
        }
    }
}

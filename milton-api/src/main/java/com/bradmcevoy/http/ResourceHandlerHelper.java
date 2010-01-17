package com.bradmcevoy.http;

import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.Http11ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ResourceHandlerHelper {

    private Logger log = LoggerFactory.getLogger( ResourceHandlerHelper.class );
    private final HandlerHelper handlerHelper;
    private final Http11ResponseHandler responseHandler;

    public ResourceHandlerHelper( HandlerHelper handlerHelper, Http11ResponseHandler responseHandler ) {
        if( responseHandler == null ) throw new IllegalArgumentException( "responseHandler may not be null");
        if( handlerHelper == null ) throw new IllegalArgumentException( "handlerHelper may not be null");
        this.handlerHelper = handlerHelper;
        this.responseHandler = responseHandler;
    }

    public void process( HttpManager manager, Request request, Response response, ResourceHandler handler ) throws NotAuthorizedException, ConflictException, BadRequestException {
        if( !handlerHelper.checkExpects( responseHandler, request, response ) ) {
            return;
        }
        String host = request.getHostHeader();
        String url = HttpManager.decodeUrl( request.getAbsolutePath() );
        log.debug( "find resource: path: " + url + " host: " + host );
        Resource r = manager.getResourceFactory().getResource( host, url );
        if( r == null ) {
            responseHandler.respondNotFound( response, request );
            return;
        }
        handler.processResource( manager, request, response, r );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource resource, ExistingEntityHandler handler ) throws NotAuthorizedException, ConflictException, BadRequestException {
        processResource( manager, request, response, resource, handler, false );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource resource, ExistingEntityHandler handler, boolean allowRedirect ) throws NotAuthorizedException, ConflictException, BadRequestException {
        long t = System.currentTimeMillis();
        try {

            manager.onProcessResourceStart( request, response, resource );

            if( allowRedirect ) {
                if( handlerHelper.doCheckRedirect( responseHandler, request, response, resource ) ) {
                    return;
                }
            }

            if( !handler.isCompatible( resource ) ) {
                log.debug( "resource not compatible. Resource class: " + resource.getClass() + " handler: " + handler.getClass() );
                responseHandler.respondMethodNotImplemented( resource, response, request );
                return;
            }

            if( !handlerHelper.checkAuthorisation( manager, resource, request ) ) {
                responseHandler.respondUnauthorised( resource, response, request );
                return;
            }

            if( request.getMethod().isWrite ) {
                if( handlerHelper.isLockedOut( request, resource ) ) {
                    response.setStatus( Status.SC_LOCKED ); // replace with responsehandler method
                    return;
                }
            }

            handler.processExistingResource( manager, request, response, resource );
        } finally {
            t = System.currentTimeMillis() - t;
            manager.onProcessResourceFinish( request, response, resource, t );
        }
    }
}

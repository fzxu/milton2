package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.BadRequestException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * Response Handler which wraps another
 *
 * @author brad
 */
public abstract class AbstractWrappingResponseHandler implements ResponseHandler {

    private static final Logger log = LoggerFactory.getLogger( AbstractWrappingResponseHandler.class );

    /**
     * The underlying respond handler which takes care of actually generating
     * content
     */
    protected ResponseHandler wrapped;

    public AbstractWrappingResponseHandler() {
    }


    public AbstractWrappingResponseHandler( ResponseHandler wrapped ) {
        this.wrapped = wrapped;
    }


    public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException, BadRequestException {
        wrapped.respondContent( resource, response, request, params );
    }


    public void setWrapped( ResponseHandler wrapped ) {
        this.wrapped = wrapped;
    }

    public ResponseHandler getWrapped() {
        return wrapped;
    }

    public void respondNoContent( Resource resource, Response response, Request request ) {
        wrapped.respondNoContent( resource, response, request );
    }

    public void respondPartialContent( GetableResource resource, Response response, Request request, Map<String, String> params, Range range ) throws NotAuthorizedException, BadRequestException {
        wrapped.respondPartialContent( resource, response, request, params, range );
    }

    public void respondCreated( Resource resource, Response response, Request request ) {
        wrapped.respondCreated( resource, response, request );
    }

    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        wrapped.respondUnauthorised( resource, response, request );
    }

    public void respondMethodNotImplemented( Resource resource, Response response, Request request ) {
        wrapped.respondMethodNotImplemented( resource, response, request );
    }

    public void respondMethodNotAllowed( Resource res, Response response, Request request ) {
        wrapped.respondMethodNotAllowed( res, response, request );
    }

    public void respondConflict( Resource resource, Response response, Request request, String message ) {
        wrapped.respondConflict( resource, response, request, message );
    }

    public void respondRedirect( Response response, Request request, String redirectUrl ) {
        wrapped.respondRedirect( response, request, redirectUrl );
    }

    public void responseMultiStatus( Resource resource, Response response, Request request, List<HrefStatus> statii ) {
        wrapped.responseMultiStatus( resource, response, request, statii );
    }

    public void respondNotModified( GetableResource resource, Response response, Request request ) {
        wrapped.respondNotModified( resource, response, request );
    }

    public void respondNotFound( Response response, Request request ) {
        wrapped.respondNotFound( response, request );
    }

    public void respondWithOptions( Resource resource, Response response, Request request, List<Method> methodsAllowed ) {
        wrapped.respondWithOptions( resource, response, request, methodsAllowed );
    }

    public void respondHead( Resource resource, Response response, Request request ) {
        wrapped.respondHead( resource, response, request );
    }

    public void respondExpectationFailed( Response response, Request request ) {
        wrapped.respondExpectationFailed( response, request );
    }

    public void respondBadRequest( Resource resource, Response response, Request request ) {
        wrapped.respondBadRequest( resource, response, request );
    }



}

package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostHandler implements ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger( PostHandler.class );

    private final Http11ResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;
    private final ResourceHandlerHelper resourceHandlerHelper;

    public PostHandler( Http11ResponseHandler responseHandler, HandlerHelper handlerHelper ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        this.resourceHandlerHelper = new ResourceHandlerHelper( handlerHelper, responseHandler );
    }

    @Override
    public String[] getMethods() {
        return new String[]{Request.Method.POST.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return ( handler instanceof PostableResource );
    }

    @Override
    public void process( HttpManager manager, Request request, Response response ) throws NotAuthorizedException, ConflictException, BadRequestException {
        this.resourceHandlerHelper.process( manager, request, response, this );
    }

    @Override
    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this, true );
    }

    @Override
    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException {
        PostableResource r = (PostableResource) resource;
        // need a linked hash map to preserve ordering of params
        Map<String, String> params = new LinkedHashMap<String, String>();
        Map<String, FileItem> files = new HashMap<String, FileItem>();
        try {
            request.parseRequestParameters( params, files );
        } catch( RequestParseException ex ) {
            log.warn( "exception parsing request. probably interrupted upload", ex );
            return;
        }
        manager.onPost( request, response, resource, params, files );
        String url = r.processForm( params, files );
        if( url != null ) {
            responseHandler.respondRedirect( response, request, url );
        } else {
            responseHandler.respondContent( resource, response, request, params );
        }
    }
}

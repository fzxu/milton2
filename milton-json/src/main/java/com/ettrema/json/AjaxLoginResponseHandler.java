package com.ettrema.json;

import com.bradmcevoy.http.AbstractWrappingResponseHandler;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * To enable ajax authentication we MUST NOT return 401 unauthorised, because
 * this will prompt for a browser login box
 *
 * Instead we respond 400 to allow the javascript to handle it.
 *
 * We only know that the authentication request came from javascript because
 * the resource is an AjaxLoginResource
 *
 * @author brad
 */
public class AjaxLoginResponseHandler extends AbstractWrappingResponseHandler {

    private final List<ResourceMatcher> resourceMatchers;

    public AjaxLoginResponseHandler( WebDavResponseHandler responseHandler, List<ResourceMatcher> resourceMatchers ) {
        super( responseHandler );
        this.resourceMatchers = resourceMatchers;
    }

    /**
     * Create with a single resource matcher, which matches on AjaxLoginResource's
     *
     * @param responseHandler
     */
    public AjaxLoginResponseHandler( WebDavResponseHandler responseHandler) {
        super( responseHandler );
        this.resourceMatchers = new ArrayList<ResourceMatcher>();
        this.resourceMatchers.add( new TypeResourceMatcher( AjaxLoginResource.class ) );
    }

    /**
     * if the resource is a AjaxLoginResource then return a 400
     *
     * otherwise just do a normal 401
     *
     * @param resource
     * @param response
     * @param request
     */
    @Override
    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        if( matches(resource) ) {
            wrapped.respondBadRequest( resource, response, request );
        } else {
            wrapped.respondUnauthorised( resource, response, request );
        }
    }

    private boolean matches(Resource r) {
        for( ResourceMatcher rm : resourceMatchers ) {
            if( rm.matches( r )) return true;
        }
        return false;
    }
}

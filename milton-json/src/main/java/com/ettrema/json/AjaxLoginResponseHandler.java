package com.ettrema.json;

import com.bradmcevoy.http.AbstractWrappingResponseHandler;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.ResponseHandler;

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
public class AjaxLoginResponseHandler extends AbstractWrappingResponseHandler{

    public AjaxLoginResponseHandler(ResponseHandler responseHandler) {
        super(responseHandler);
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
        if( resource instanceof AjaxLoginResource) {
            wrapped.respondBadRequest( resource, response, request );
        } else {
            wrapped.respondUnauthorised( resource, response, request );
        }
    }

}

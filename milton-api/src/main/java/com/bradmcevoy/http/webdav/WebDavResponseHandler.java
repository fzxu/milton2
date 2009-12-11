package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.HrefStatus;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.http11.Http11ResponseHandler;
import java.util.List;

/**
 *
 * @author brad
 */
public interface WebDavResponseHandler extends Http11ResponseHandler{
    void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii);

    /**
     * Generate the response for a PROPFIND or a PROPPATCH
     *
     * @param propFindResponses
     * @param response
     * @param request
     * @param r - the resource
     */
    void respondPropFind( List<PropFindResponse> propFindResponses, Response response, Request request, Resource r );
}

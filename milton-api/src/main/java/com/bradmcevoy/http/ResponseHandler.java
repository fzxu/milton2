package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import java.util.List;
import java.util.Map;

/**
 *  The ResponseHandler should handle all responses back to the client.
 *
 *  Methods are provided for each significant response circumstance with respect
 *  to Milton.
 *
 *  The intention is that implementations may be provided or customised to support
 *  per implementation requirements for client compatibility.
 *
 *  In other words, hacks to support particular client programs should be implemented
 *  here
 */
public interface ResponseHandler {
    void respondContent(Resource resource, Response response, Request request, Map<String,String> params);
    void respondPartialContent(GetableResource resource, Response response, Request request, Map<String,String> params, Range range);
    void respondCreated(Resource resource, Response response, Request request);
    void respondUnauthorised(Resource resource, Response response, Request request);
    void respondMethodNotImplemented(Resource resource, Response response, Request request);
    void respondMethodNotAllowed(Resource res, Response response, Request request);
    void respondConflict(Resource resource, Response response, Request request, String message);
    void respondRedirect(Response response, Request request, String redirectUrl);
    void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii);
    void respondNotModified(GetableResource resource, Response response, Request request);
    void respondNotFound(Response response, Request request);
    void respondWithOptions(Resource resource, Response response,Request request, List<Method> methodsAllowed);
}

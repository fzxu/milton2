package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostHandler extends ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(PostHandler.class);
    
    public PostHandler(HttpManager manager) {
        super(manager);
    }
    
    
    @Override
    public Request.Method method() {
        return Method.POST;
    }
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof PostableResource);
    }

    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        PostableResource r = (PostableResource) resource;
        Map<String, String> params = new HashMap<String,String>();
        Map<String, FileItem> files = new HashMap<String,FileItem>();
        try {
            request.parseRequestParameters(params,files);
        } catch (RequestParseException ex) {
            log.warn("exception parsing request. probably interrupted upload",ex);
            return ;
        }
        manager.onPost(request, response, resource, params, files);
        String url = processForm(r,params, files);
        if( url != null ) {
            respondRedirect(response,url);
        } else {
            respondWithContent(request,response,r,params);
        }
    }

    /** Return a URL to perform a redirect. Return null to render the current resource
     */
    protected String processForm(PostableResource r, Map<String,String> parameters, Map<String,FileItem> files) {
        return r.processForm(parameters, files);
    }
    
    protected void respondWithContent(Request request, Response response, PostableResource resource,Map<String,String> parameters) {
        _respondWithContent(request,response,resource,parameters);
    }    
    
    @Override
    protected boolean doCheckRedirect(Request request, Response response,Resource resource) {
        String redirectUrl = resource.checkRedirect(request);
        if( redirectUrl != null ) {
            respondRedirect( response, redirectUrl );
            return true;
        } else {
            return false;
        }
    }        
}
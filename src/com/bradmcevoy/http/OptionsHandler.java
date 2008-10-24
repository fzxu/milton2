package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import java.util.ArrayList;
import java.util.List;


public class OptionsHandler extends ExistingEntityHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OptionsHandler.class);
    
    public OptionsHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        respondWithOptions(response,resource);
    }
    
    @Override
    public Request.Method method() {
        return Method.OPTIONS;
    }   
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return true;
    }

    private List<Request.Method> determineMethodsAllowed(Resource res) {
        List<Method> list = new ArrayList<Request.Method>();
        for(Handler f : manager.allHandlers) {
            if( f.isCompatible(res) ) {
                list.add(f.method());
            }
        }
        return list;
    }

    private void respondWithOptions(Response response, Resource resource) {
        response.setStatus(Response.Status.SC_OK);
        response.setDavHeader(manager.getSupportedLevels());
        List<Method> methodsAllowed = determineMethodsAllowed(resource);
        response.setAllowHeader( methodsAllowed );
        response.setNonStandardHeader("MS-Author-Via", "DAV");            
        response.setContentLengthHeader((long)0);
    }

}
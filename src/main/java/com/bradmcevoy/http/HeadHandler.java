package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import java.util.Map;

public class HeadHandler extends GetHandler {
    public HeadHandler(HttpManager manager) {
        super(manager);
    }

    @Override
    protected Method method() {
        return Request.Method.HEAD; 
    }

    @Override
    protected void sendContent(Request request, Response response, GetableResource resource, Map<String, String> params) {
        // do nothing
    }

    
}

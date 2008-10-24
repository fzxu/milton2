package com.bradmcevoy.http;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardFilter implements Filter {
    
    private Logger log = LoggerFactory.getLogger(StandardFilter.class);
    
    public static final String INTERNAL_SERVER_ERROR_HTML = "<html><body><h1>Internal Server Error (500)</h1></body></html>";
    
    public StandardFilter() {
    }
    
    public void process(FilterChain chain, Request request, Response response) {
        try {
            Request.Method method = request.getMethod();
            
            HttpManager manager = chain.getHttpManager();
            Handler handler = manager.methodFactoryMap.get(method);
            if( handler == null ) throw new RuntimeException("No handler for method: " + method.code);        
            
            handler.process(manager,request,response);
        } catch(Throwable e) {
            log.error("process", e);
            try {
                response.setStatus(Response.Status.SC_INTERNAL_SERVER_ERROR);
                log.info("setting error content");
                response.getOutputStream().write(INTERNAL_SERVER_ERROR_HTML.getBytes());                
            } catch (IOException ex) {
                log.warn("exception writing content");
            }
        } finally {
            response.close();            
        }
    }    
}

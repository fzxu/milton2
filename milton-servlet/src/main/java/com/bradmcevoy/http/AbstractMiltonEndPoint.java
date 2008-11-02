package com.bradmcevoy.http;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class for both MiltonServlet and MiltonFilter
 */
public class AbstractMiltonEndPoint {
    
    private Logger log = LoggerFactory.getLogger(AbstractMiltonEndPoint.class);
    
    protected ServletHttpManager httpManager;
    
    protected void init(String resourceFactoryClassName) throws ServletException {
        log.debug("resourceFactoryClassName: " + resourceFactoryClassName);
        ResourceFactory rf;
        try {
            Class c = Class.forName(resourceFactoryClassName);
            rf = (ResourceFactory) c.newInstance();
        } catch (Throwable ex) {
            throw new ServletException("Failed to instantiate resource factory: " + resourceFactoryClassName, ex);
        }                
        httpManager = new ServletHttpManager(rf);
    }
    
    public void destroy() {
        log.debug("destroy");
        if( httpManager == null ) return ;
        httpManager.destroy(httpManager);
    }
}

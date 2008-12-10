package com.bradmcevoy.http;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class for both MiltonServlet and MiltonFilter
 */
public class AbstractMiltonEndPoint {
    
    private Logger log = LoggerFactory.getLogger(AbstractMiltonEndPoint.class);
    
    protected ServletHttpManager httpManager;
    
    protected void init(String resourceFactoryClassName, String notFoundPath) throws ServletException {
        log.debug("resourceFactoryClassName: " + resourceFactoryClassName);
        ResourceFactory rf = instantiate(resourceFactoryClassName);
        init(rf, notFoundPath);
    }
    
    protected void initFromFactoryFactory(String resourceFactoryFactoryClassName, String notFoundPath) throws ServletException {
        log.debug("resourceFactoryFactoryClassName: " + resourceFactoryFactoryClassName);
        ResourceFactoryFactory rff = instantiate(resourceFactoryFactoryClassName);
        rff.init();
        ResourceFactory rf = rff.createResourceFactory();
        init(rf, notFoundPath);
    }
    
    protected void init(ResourceFactory rf, String notFoundPath) {
        httpManager = new ServletHttpManager(rf, notFoundPath);
    }
    
    protected <T> T instantiate(String className) throws ServletException {
        try {
            Class c = Class.forName(className);
            T rf = (T) c.newInstance();
            return rf;
        } catch (Throwable ex) {
            throw new ServletException("Failed to instantiate: " + className, ex);
        }                
    }
    
    public void destroy() {
        log.debug("destroy");
        if( httpManager == null ) return ;
        httpManager.destroy(httpManager);
    }
}

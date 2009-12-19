package com.bradmcevoy.http;

import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * MiltonServlet is a thin wrapper around HttpManager. It takes care of initialisation
 * and delegates requests to the HttpManager
 * 
 * The servlet API is hidden by the Milton API, however you can get access to
 * the underlying request and response objects from the static request and response
 * methods which use ThreadLocal variables
 * 
 * @author brad
 */
public class MiltonServlet implements Servlet{
    
    private Logger log = LoggerFactory.getLogger(MiltonServlet.class);
        
    private static final ThreadLocal<HttpServletRequest> originalRequest = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> originalResponse = new ThreadLocal<HttpServletResponse>();
    private static final ThreadLocal<ServletConfig> tlServletConfig = new ThreadLocal<ServletConfig>();

    public static HttpServletRequest request() {
        return originalRequest.get();
    }
    
    public static HttpServletResponse response() {
        return originalResponse.get();
    }

    /**
     * Make the servlet config available to any code on this thread.
     *
     * @return
     */
    public static ServletConfig servletConfig() {
        return tlServletConfig.get();
    }
    
    public static void forward(String url) {
        try {
            request().getRequestDispatcher(url).forward(originalRequest.get(),originalResponse.get());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ServletException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ServletConfig config;

    protected ServletHttpManager httpManager;


    public void init(ServletConfig config) throws ServletException {
        try {
            this.config = config;
            String resourceFactoryFactoryClassName = config.getInitParameter("resource.factory.factory.class");
            if( resourceFactoryFactoryClassName != null && resourceFactoryFactoryClassName.length() > 0 ) {
                initFromFactoryFactory(resourceFactoryFactoryClassName);
            } else {
                String resourceFactoryClassName = config.getInitParameter("resource.factory.class");
                String responseHandlerClassName = config.getInitParameter("response.handler.class");
                init(resourceFactoryClassName, responseHandlerClassName);
            }
            httpManager.init(new ApplicationConfig(config),httpManager); 
        } catch( ServletException ex )  {
            log.error("Exception starting milton servlet",ex);
            throw ex;
        } catch (Throwable ex) {
            log.error("Exception starting milton servlet",ex);
            throw new RuntimeException(ex);
        }        
    }

    protected void init(String resourceFactoryClassName, String responseHandlerClassName) throws ServletException {
        log.debug("resourceFactoryClassName: " + resourceFactoryClassName);
        ResourceFactory rf = instantiate(resourceFactoryClassName);
        WebDavResponseHandler responseHandler;
        if( responseHandlerClassName == null ) {
            responseHandler = null; // allow default to be created
        } else {
            responseHandler = instantiate(responseHandlerClassName);
        }
        init(rf, responseHandler);
    }

    protected void initFromFactoryFactory(String resourceFactoryFactoryClassName) throws ServletException {
        log.debug("resourceFactoryFactoryClassName: " + resourceFactoryFactoryClassName);
        ResourceFactoryFactory rff = instantiate(resourceFactoryFactoryClassName);
        rff.init();
        ResourceFactory rf = rff.createResourceFactory();
        WebDavResponseHandler responseHandler = rff.createResponseHandler();
        init(rf, responseHandler);
    }

    protected void init(ResourceFactory rf, WebDavResponseHandler responseHandler) {
        if( responseHandler == null ) {
            httpManager = new ServletHttpManager(rf);
        } else {
            httpManager = new ServletHttpManager(rf, responseHandler);
        }
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


    public void service(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            originalRequest.set(req);
            originalResponse.set(resp);
            tlServletConfig.set( config );
            Request request = new ServletRequest(req);
            Response response = new ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            originalRequest.remove();
            originalResponse.remove();
            tlServletConfig.remove();
            servletResponse.getOutputStream().flush();            
            servletResponse.flushBuffer();
        }
    }

    public String getServletInfo() {
        return "MiltonServlet";
    }

    public ServletConfig getServletConfig() { 
        return config;
    }    
}

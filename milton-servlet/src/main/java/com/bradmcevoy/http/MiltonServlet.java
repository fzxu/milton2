package com.bradmcevoy.http;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MiltonServlet extends AbstractMiltonEndPoint implements Servlet{
    
    private Logger log = LoggerFactory.getLogger(MiltonServlet.class);
    
    private ServletConfig config;
    
    private static final ThreadLocal<HttpServletRequest> originalRequest = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> originalResponse = new ThreadLocal<HttpServletResponse>();
    
    public static void forward(String url) {
        try {
            originalRequest.get().getRequestDispatcher(url).forward(originalRequest.get(),originalResponse.get());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ServletException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        try {
            this.config = config;
            String resourceFactoryFactoryClassName = config.getInitParameter("resource.factory.factory.class");
            if( resourceFactoryFactoryClassName != null && resourceFactoryFactoryClassName.length() > 0 ) {
                initFromFactoryFactory(resourceFactoryFactoryClassName);
            } else {
                String resourceFactoryClassName = config.getInitParameter("resource.factory.class");
                init(resourceFactoryClassName);                
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
    
    public void service(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            originalRequest.set(req);
            originalResponse.set(resp);
            Request request = new ServletRequest(req);
            Response response = new ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            originalRequest.remove();
            originalResponse.remove();
            servletResponse.flushBuffer();
//            servletResponse.getOutputStream().flush();
//            servletResponse.getOutputStream().close();
        }
    }

    public String getServletInfo() {
        return "MiltonServlet";
    }

    public ServletConfig getServletConfig() {
        return config;
    }    
}

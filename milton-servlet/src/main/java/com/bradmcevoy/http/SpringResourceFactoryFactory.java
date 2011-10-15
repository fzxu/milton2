package com.bradmcevoy.http;

import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**  Specify this class name in your init parameter: resource.factory.factory.class
 * 
 * This will load applicationContext.xml from the classpath and use that to
 * load the ResourceFactory from bean id: milton.resource.factory
 * 
 * Example web.xml
 * <PRE>
 * {@code
 * <servlet>
 *   <servlet-name>milton</servlet-name>
 *   <servlet-class>com.bradmcevoy.http.MiltonServlet</servlet-class>
 *   <init-param>
 *         <param-name>resource.factory.factory.class</param-name>
 *         <param-value>com.bradmcevoy.http.SpringResourceFactoryFactory</param-value>
 *   </init-param>
 * </servlet>
 * }
 * </PRE>
 * 
 * Example applicationContext.xml
 * <PRE>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <beans xmlns="http://www.springframework.org/schema/beans"
 *        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *        xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">
 *
 *     <bean id="milton.resource.factory" class="com.ettrema.http.fs.FileSystemResourceFactory">
 *         <property name="securityManager" ref="milton.fs.security.manager" />
 *     </bean>
 *
 *     <bean id="milton.response.handler" class="com.ettrema.http.DefaultResponseHandler" />
 *
 *     <bean id="milton.fs.security.manager" class="com.ettrema.http.fs.NullSecurityManager" >
 *         <property name="realm" value="aRealm" />
 *     </bean>
 *
 * </beans>
  * }
 * </PRE>
 *
 */
public class SpringResourceFactoryFactory implements ResourceFactoryFactory{

    ApplicationContext context;

	@Override
    public void init() {
        context = new ClassPathXmlApplicationContext(new String[] {"applicationContext.xml"});
    }


    
	@Override
    public ResourceFactory createResourceFactory() {
        ResourceFactory rf = (ResourceFactory) context.getBean("milton.resource.factory");
        return rf;
    }

	@Override
    public WebDavResponseHandler createResponseHandler() {
        return  (WebDavResponseHandler) context.getBean("milton.response.handler");
    }



}

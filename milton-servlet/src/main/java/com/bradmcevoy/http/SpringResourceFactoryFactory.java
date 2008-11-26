package com.bradmcevoy.http;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class SpringResourceFactoryFactory implements ResourceFactoryFactory{

    ApplicationContext context;

    public void init() {
        context = new ClassPathXmlApplicationContext(new String[] {"applicationContext.xml"});
    }


    
    public ResourceFactory createResourceFactory() {
        ResourceFactory rf = (ResourceFactory) context.getBean("milton.resource.factory");
        return rf;
    }

}

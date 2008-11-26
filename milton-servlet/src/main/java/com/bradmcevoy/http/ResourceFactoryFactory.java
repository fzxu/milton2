package com.bradmcevoy.http;

/**
 *
 */
public interface ResourceFactoryFactory {
    void init();
    
    ResourceFactory createResourceFactory();
}

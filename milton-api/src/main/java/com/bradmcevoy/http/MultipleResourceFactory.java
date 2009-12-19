package com.bradmcevoy.http;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleResourceFactory implements ResourceFactory {
    
    private Logger log = LoggerFactory.getLogger(MultipleResourceFactory.class);

    
    protected final List<ResourceFactory> factories;

    public MultipleResourceFactory() {
        factories = new ArrayList<ResourceFactory>();
    }

    public MultipleResourceFactory( List<ResourceFactory> factories ) {
        this.factories = factories;
    }
        

    public Resource getResource(String host, String url) {
        log.debug( "getResource: " + url);
        for( ResourceFactory rf : factories ) {
            Resource r = rf.getResource(host,url);
            if( r != null ) {
                return r;
            }
        }
        log.debug("no resource factory supplied a resouce");
        return null;
    }  
}

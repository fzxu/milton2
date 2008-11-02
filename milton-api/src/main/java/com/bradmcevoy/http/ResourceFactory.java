package com.bradmcevoy.http;

public interface ResourceFactory {
    Resource getResource(String host, String url);
    
    /**
     * 
     * @return - a string identifying the supported levels. Should be "1" or "1,2"
     */
    String getSupportedLevels();
}

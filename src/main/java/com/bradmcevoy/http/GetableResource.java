package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface GetableResource extends Resource {
    /** If range not null is a Partial content request
     */
    public void sendContent( OutputStream out, Range range, Map<String,String> params ) throws IOException;

    /** How many seconds to allow the content to be cached for, or null if caching is not allowed
     */
    Long getMaxAgeSeconds();
    
}

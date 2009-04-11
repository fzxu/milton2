package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface GetableResource extends Resource {
    /** If range not null is a Partial content request
     */
    public void sendContent( OutputStream out, Range range, Map<String,String> params ) throws IOException, NotAuthorizedException;

    /** How many seconds to allow the content to be cached for, or null if caching is not allowed
     */
    Long getMaxAgeSeconds();

    /** 
     * Given a comma seperated listed of preferred content types acceptable for a client, return one content type which is the best.
     * 
     * Returns the most preferred  MIME type. Eg text/html, image/jpeg, etc
     *
     *  Must be IANA registered
     *
     *  accepts is the accepts header. Eg: Accept: text/*, text/html, text/html;level=1
     *
     *  See - http://www.iana.org/assignments/media-types/ for a list of content types
     *  See - http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html for details about the accept header
     * 
     *  If you can't handle accepts interpretation, just return a single content type - Eg text/html
     */
    String getContentType(String accepts);

    /** The length of the content in this resource. If unknown return nnull
     */
    Long getContentLength();
    
}

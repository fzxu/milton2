package com.bradmcevoy.http;

/**
 * Implementations of this interface are authentication methods for use
 * with HTTP.
 *
 * These include basic, digest, ntlm etc
 *
 * @author brad
 */
public interface AuthenticationHandler {
    /**
     * Returns true if this supports authenticating with the given Auth data
     * on the given resource.
     *
     * @param r
     * @param auth
     * @return
     */
    boolean supports(Resource r, Auth auth);

    /**
     * Authenticate the details in the request for access to the given
     * resource.
     *
     * @param resource
     * @param request
     * @return
     */
    Object authenticate( Resource resource, Request request);

    
    /**
     * Create a challenge for this authentication method. This should be completely
     * formatted as per http://tools.ietf.org/html/rfc2617
     * 
     * @param resource
     * @param request
     * @return
     */
    String getChallenge( Resource resource, Request request );

    /**
     * Returns true if this authentication handler is compatible with the given
     * resource
     * 
     * @param resource
     * @return
     */
    boolean isCompatible( Resource resource );
}

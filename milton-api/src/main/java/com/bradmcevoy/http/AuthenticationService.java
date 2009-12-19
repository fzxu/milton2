package com.bradmcevoy.http;

import com.bradmcevoy.http.http11.auth.BasicAuthHandler;
import com.bradmcevoy.http.http11.auth.DigestAuthenticationHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger( AuthenticationService.class );

    private final List<AuthenticationHandler> authenticationHandlers;

    public AuthenticationService( List<AuthenticationHandler> authenticationHandlers ) {
        this.authenticationHandlers = authenticationHandlers;
    }

    public AuthenticationService() {
        AuthenticationHandler digest = new DigestAuthenticationHandler();
        AuthenticationHandler basic = new BasicAuthHandler();
        this.authenticationHandlers = Arrays.asList( digest, basic);
    }

    public Object authenticate( Resource resource, Request request ) {
        for( AuthenticationHandler h : authenticationHandlers ) {
            if( h.supports( resource, request.getAuthorization() ) ) {
                return h.authenticate( resource, request );
            }            
        }
        log.debug("No AuthenticationHandler supports scheme:" + request.getAuthorization().getScheme() + " and resource type: " + resource.getClass());
        return null;
    }

    /**
     * Generates a list of http authentication challenges, one for each
     * supported authentication method, to be sent to the client.
     *
     * @param resource - the resoruce being requested
     * @param request - the current request
     * @return - a list of http challenges
     */
    public List<String> getChallenges( Resource resource, Request request ) {
        List<String> challenges = new ArrayList<String>();
        for( AuthenticationHandler h : authenticationHandlers ) {
            if( h.isCompatible(resource)) {
                String ch = h.getChallenge(resource, request);
                challenges.add( ch );
            }
        }
        return challenges;
    }
}

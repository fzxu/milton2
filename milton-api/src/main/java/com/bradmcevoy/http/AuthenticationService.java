package com.bradmcevoy.http;

import com.bradmcevoy.http.http11.auth.BasicAuthHandler;
import com.bradmcevoy.http.http11.auth.DigestAuthenticationHandler;
import com.bradmcevoy.http.http11.auth.NonceProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger( AuthenticationService.class );
    private List<AuthenticationHandler> authenticationHandlers;
    private boolean disableBasic;
    private boolean disableDigest;

    /**
     * Creates a AuthenticationService using the given handlers. Use this if
     * you don't want the default of a BasicAuthHandler and a DigestAuthenticationHandler
     *
     * @param authenticationHandlers
     */
    public AuthenticationService( List<AuthenticationHandler> authenticationHandlers ) {
        this.authenticationHandlers = authenticationHandlers;
    }

    /**
     * Creates basic and digest handlers with the given NonceProvider
     * 
     * @param nonceProvider
     */
    public AuthenticationService( NonceProvider nonceProvider ) {
        AuthenticationHandler digest = new DigestAuthenticationHandler( nonceProvider );
        AuthenticationHandler basic = new BasicAuthHandler();

        authenticationHandlers = new ArrayList<AuthenticationHandler>();
        authenticationHandlers.add( basic );
        authenticationHandlers.add( digest );
    }

    /**
     * Creates with Basic and Digest handlers
     *
     */
    public AuthenticationService() {
        AuthenticationHandler digest = new DigestAuthenticationHandler();
        AuthenticationHandler basic = new BasicAuthHandler();
        authenticationHandlers = new ArrayList<AuthenticationHandler>();
        authenticationHandlers.add( basic );
        authenticationHandlers.add( digest );
    }

    public void setDisableBasic( boolean b ) {
        if( b ) {
            Iterator<AuthenticationHandler> it = this.authenticationHandlers.iterator();
            while( it.hasNext() ) {
                AuthenticationHandler hnd = it.next();
                if( hnd instanceof BasicAuthHandler ) {
                    it.remove();
                }
            }
        }
        disableBasic = b;
    }

    public boolean isDisableBasic() {
        return disableBasic;
    }

    public void setDisableDigest( boolean b ) {
        if( b ) {
            Iterator<AuthenticationHandler> it = this.authenticationHandlers.iterator();
            while( it.hasNext() ) {
                AuthenticationHandler hnd = it.next();
                if( hnd instanceof DigestAuthenticationHandler ) {
                    it.remove();
                }
            }
        }
        disableDigest = b;
    }

    public boolean isDisableDigest() {
        return disableDigest;
    }

    /**
     * Looks for an AuthenticationHandler which supports the given resource and
     * authorization header, and then returns the result of that handler's
     * authenticate method.
     *
     * Returns null if no handlers support the request
     *
     * @param resource
     * @param request
     * @return - null if no authentication was attempted. Otherwise, an AuthStatus
     * object containing the Auth object and a boolean indicating whether the
     * login succeeded
     */
    public AuthStatus authenticate( Resource resource, Request request ) {
        log.trace( "authenticate" );
        Auth auth = request.getAuthorization();
        boolean preAuthenticated = ( auth != null && auth.getTag() != null );
        if( preAuthenticated ) {
            log.trace( "request is pre-authenticated" );
            return new AuthStatus( auth, false );
        }
        for( AuthenticationHandler h : authenticationHandlers ) {
            if( h.supports( resource, request ) ) {
                Object loginToken = h.authenticate( resource, request );
                if( loginToken == null ) {
                    log.warn( "authentication failed by AuthenticationHandler:" + h.getClass() );
                    return new AuthStatus( auth, true );
                } else {
                    if( log.isTraceEnabled() ) {
                        log.trace( "authentication passed by: " + h.getClass() );
                    }
                    if( auth == null ) { // some authentication handlers do not require an Auth object
                        auth = new Auth( Auth.Scheme.FORM, null, loginToken );
                        request.setAuthorization( auth );
                    }
                    auth.setTag( loginToken );
                }
                return new AuthStatus( auth, false );
            }
        }
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
            if( h.isCompatible( resource ) ) {
                log.debug( "challenge for auth: " + h.getClass() );
                String ch = h.getChallenge( resource, request );
                challenges.add( ch );
            } else {
                log.debug( "not challenging for auth: " + h.getClass() + " for resource type: " + resource.getClass() );
            }
        }
        return challenges;
    }

    public List<AuthenticationHandler> getAuthenticationHandlers() {
        return Collections.unmodifiableList( authenticationHandlers );
    }

    public static class AuthStatus {

        public final Auth auth;
        public final boolean loginFailed;

        public AuthStatus( Auth auth, boolean loginFailed ) {
            this.auth = auth;
            this.loginFailed = loginFailed;
        }
    }
}

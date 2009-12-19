package com.bradmcevoy.http;

import com.bradmcevoy.http.http11.Http11ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class HandlerHelper {

    private Logger log = LoggerFactory.getLogger(HandlerHelper.class);

    private AuthenticationService authenticationService;

    public HandlerHelper( AuthenticationService authenticationService ) {
        this.authenticationService = authenticationService;
    }




    /**
     * Checks the expect header, and responds if necessary
     *
     * @param resource
     * @param request
     * @param response
     * @return - true if the expect header is ok
     */
    public boolean checkExpects( Http11ResponseHandler responseHandler, Request request, Response response ) {
        String s = request.getExpectHeader();
        if( s != null && s.length() > 0 ) {
            responseHandler.respondExpectationFailed( response, request );
            return false;
        } else {
            return true;
        }
    }


    public boolean checkAuthorisation( HttpManager manager, Resource resource, Request request ) {
        Auth auth = request.getAuthorization();
        if( auth != null ) {
            Object authTag = authenticationService.authenticate(resource, request); //handler.authenticate( auth.user, auth.password );
            if( authTag == null ) {
                log.warn( "failed to authenticate" );
                return false;
            } else {
                log.debug( "got authenticated tag: " + authTag.getClass());
                auth.setTag( authTag );
            }
        } else {
            auth = manager.getSessionAuthentication( request );
        }


        boolean authorised = resource.authorise( request, request.getMethod(), auth );
        if( !authorised ) {
            log.warn( "Not authorised, requesting basic authentication" );
            return false;
        } else {
            return true;
        }
    }

    public boolean doCheckRedirect( Http11ResponseHandler responseHandler, Request request, Response response, Resource resource ) {
        String redirectUrl = resource.checkRedirect( request );
        if( redirectUrl != null ) {
            responseHandler.respondRedirect( response, request, redirectUrl );
            return true;
        } else {
            return false;
        }
    }

    /**
     * TODO: move to webdav
     * 
     * @param inRequest
     * @param inResource
     * @return
     */
    public boolean isLockedOut( Request inRequest, Resource inResource ) {
        if( inResource == null || !( inResource instanceof LockableResource ) ) {
            return false;
        }
        LockableResource lr = (LockableResource) inResource;
        LockToken token = lr.getCurrentLock();
        if( token != null ) {
            Auth auth = inRequest.getAuthorization();
            String lockedByUser = token.info.lockedByUser;
            if( lockedByUser == null ) {
                log.warn( "Resource is locked with a null user. Ignoring the lock" );
                return false;
            } else if( !lockedByUser.equals( auth.getUser() ) ) {
                log.info( "fail: lock owned by: " + lockedByUser + " not by " + auth.getUser() );
                String value = inRequest.getHeaders().get( "If" );
                if( value != null ) {
                    if( value.contains( "opaquelocktoken:" + token.tokenId + ">" ) ) {
                        log.info( "Contained valid token. so is unlocked" );
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean missingLock(Request inRequest, Resource inParentcol) {
		//make sure we are not requiring a lock
	    String value = inRequest.getHeaders().get("If");
	    if( value != null)
	    {
	    	if( value.contains("(<DAV:no-lock>)") )
	    	{
	    		log.info("Contained valid token. so is unlocked");
	    		return true;
	    	}
	    }

		return false;
	}

}

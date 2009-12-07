package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.BadRequestException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 *
 */
public class DefaultHttp11ResponseHandler implements Http11ResponseHandler {

    private static final Logger log = LoggerFactory.getLogger( DefaultHttp11ResponseHandler.class );
    public static final String METHOD_NOT_ALLOWED_HTML = "<html><body><h1>Method Not Allowed</h1></body></html>";
    public static final String NOT_FOUND_HTML = "<html><body><h1>${url} Not Found (404)</h1></body></html>";
    public static final String METHOD_NOT_IMPLEMENTED_HTML = "<html><body><h1>Method Not Implemented</h1></body></html>";
    public static final String CONFLICT_HTML = "<html><body><h1>Conflict</h1></body></html>";

    public static String generateEtag( Resource r ) {
        String s = r.getUniqueId();
        if( s == null ) return null;
        Date dt = r.getModifiedDate();
        if( dt != null ) {
            s = s + "_" + dt.hashCode();
        }
        return s;
    }


    public void respondWithOptions( Resource resource, Response response, Request request, List<String> methodsAllowed ) {
        response.setStatus( Response.Status.SC_OK );        
        response.setAllowHeader( methodsAllowed );        
        response.setContentLengthHeader( (long) 0 );
    }

    public void respondNotFound( Response response, Request request ) {
        log.debug( "responding not found" );
        response.setStatus( Response.Status.SC_NOT_FOUND );
        response.setContentTypeHeader( "text/html" );
        response.setStatus( Response.Status.SC_NOT_FOUND );
        PrintWriter pw = new PrintWriter( response.getOutputStream(), true );

        String s = NOT_FOUND_HTML.replace( "${url}", request.getAbsolutePath() );
        pw.print( s );
        pw.flush();

    }

    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        log.debug( "requesting authorisation" );
        response.setStatus( Response.Status.SC_UNAUTHORIZED );
        response.setAuthenticateHeader( resource.getRealm() );
    }

    public void respondMethodNotImplemented( Resource resource, Response response, Request request ) {
        log.debug( "method not implemented. resource: " + resource.getClass().getName() + " - method " + request.getMethod() );
        try {
            response.setStatus( Response.Status.SC_NOT_IMPLEMENTED );
            OutputStream out = response.getOutputStream();
            out.write( METHOD_NOT_IMPLEMENTED_HTML.getBytes() );
        } catch( IOException ex ) {
            log.warn( "exception writing content" );
        }
    }

    public void respondMethodNotAllowed( Resource res, Response response, Request request ) {
        log.debug( "method not allowed. handler: " + this.getClass().getName() + " resource: " + res.getClass().getName() );
        try {
            response.setStatus( Response.Status.SC_METHOD_NOT_ALLOWED );
            OutputStream out = response.getOutputStream();
            out.write( METHOD_NOT_ALLOWED_HTML.getBytes() );
        } catch( IOException ex ) {
            log.warn( "exception writing content" );
        }
    }

    /**
     *
     * @param resource
     * @param response
     * @param message - optional message to output in the body content
     */
    public void respondConflict( Resource resource, Response response, Request request, String message ) {
        try {
            response.setStatus( Response.Status.SC_CONFLICT );
            OutputStream out = response.getOutputStream();
            out.write( CONFLICT_HTML.getBytes() );
        } catch( IOException ex ) {
            log.warn( "exception writing content" );
        }
    }

    public void respondRedirect( Response response, Request request, String redirectUrl ) {
        if( redirectUrl == null ) {
            throw new NullPointerException( "redirectUrl cannot be null" );
        }
        response.setStatus( Response.Status.SC_MOVED_TEMPORARILY );
        response.setLocationHeader( redirectUrl );
    }

    public void respondExpectationFailed( Response response, Request request ) {
        response.setStatus( Response.Status.SC_EXPECTATION_FAILED );
    }

    public void respondCreated( Resource resource, Response response, Request request ) {
        log.debug( "respondCreated" );
        response.setStatus( Response.Status.SC_CREATED );
    }

    public void respondNoContent( Resource resource, Response response, Request request ) {
        log.debug( "respondNoContent" );
        response.setStatus( Response.Status.SC_OK );
    }

    public void respondPartialContent( GetableResource resource, Response response, Request request, Map<String, String> params, Range range ) throws NotAuthorizedException, BadRequestException {
        log.debug( "respondPartialContent: " + range.getStart() + " - " + range.getFinish() );
        response.setStatus( Response.Status.SC_PARTIAL_CONTENT );
        response.setContentRangeHeader( range.getStart(), range.getFinish(), resource.getContentLength() );
        response.setDateHeader( new Date() );
        String etag = generateEtag( resource );
        if( etag != null ) {
            response.setEtag( etag );
        }
        String acc = request.getAcceptHeader();
        String ct = resource.getContentType( acc );
        if( ct != null ) {
            response.setContentTypeHeader( ct );
        }
        try {
            resource.sendContent( response.getOutputStream(), range, params, ct );
        } catch( IOException ex ) {
            log.warn( "IOException writing to output, probably client terminated connection", ex );
        }
    }

    public void respondHead( Resource resource, Response response, Request request ) {
        setRespondContentCommonHeaders( response, resource, Response.Status.SC_NO_CONTENT );
    }

    public void respondContent( Resource resource, Response response, Request request, Map<String, String> params ) throws NotAuthorizedException, BadRequestException {
        log.debug( "respondContent: " + resource.getClass() );
        setRespondContentCommonHeaders( response, resource );
        if( resource instanceof GetableResource ) {
            GetableResource gr = (GetableResource) resource;
            Long contentLength = gr.getContentLength();
            if( contentLength != null ) { // often won't know until rendered
                response.setContentLengthHeader( contentLength );
            }
            String acc = request.getAcceptHeader();
            String ct = gr.getContentType( acc );
            if( ct != null ) {
                response.setContentTypeHeader( ct );
            }
            setCacheControl( gr, response, request.getAuthorization() );
            sendContent( request, response, (GetableResource) resource, params, null, ct );
        }
    }

    public void respondNotModified( GetableResource resource, Response response, Request request ) {
        log.debug( "not modified" );
        response.setStatus( Response.Status.SC_NOT_MODIFIED );
        response.setDateHeader( new Date() );
        String acc = request.getAcceptHeader();
        String etag = generateEtag( resource );
        if( etag != null ) {
            response.setEtag( etag );
        }
        response.setLastModifiedHeader( resource.getModifiedDate() );
        setCacheControl( resource, response, request.getAuthorization() );
    }

    public static void setCacheControl( final GetableResource resource, final Response response, Auth auth ) {
        Long delta = resource.getMaxAgeSeconds( auth );
        log.debug( "setCacheControl: " + delta + " - " + resource.getClass() );
        if( delta != null ) {
            if( auth != null ) {
                response.setCacheControlPrivateMaxAgeHeader( delta );
                //response.setCacheControlMaxAgeHeader(delta);
            } else {
                response.setCacheControlMaxAgeHeader( delta );
            }
            Date expiresAt = calcExpiresAt( resource.getModifiedDate(), delta.longValue() );
            response.setExpiresHeader( expiresAt );
        } else {
            response.setCacheControlNoCacheHeader();
        }
    }

    public static Date calcExpiresAt( Date modifiedDate, long deltaSeconds ) {
        long deltaMs = deltaSeconds * 1000;
        long expiresAt = System.currentTimeMillis() + deltaMs;
        return new Date( expiresAt );
    }

    protected void sendContent( Request request, Response response, GetableResource resource, Map<String, String> params, Range range, String contentType ) throws NotAuthorizedException, BadRequestException {
        OutputStream out = outputStreamForResponse( request, response, resource );
        try {
            resource.sendContent( out, null, params, contentType );
            out.flush();
        } catch( IOException ex ) {
            log.warn( "IOException sending content", ex );
        }
    }

    protected OutputStream outputStreamForResponse( Request request, Response response, GetableResource resource ) {
        OutputStream outToUse = response.getOutputStream();
        return outToUse;
    }

    protected void output( final Response response, final String s ) {
        PrintWriter pw = new PrintWriter( response.getOutputStream(), true );
        pw.print( s );
        pw.flush();
    }

    public static void setRespondContentCommonHeaders( Response response, Resource resource ) {
        setRespondContentCommonHeaders( response, resource, Response.Status.SC_OK );
    }

    public static void setRespondContentCommonHeaders( Response response, Resource resource, Response.Status status ) {
        response.setStatus( status );
        response.setDateHeader( new Date() );
        String etag = generateEtag( resource );
        if( etag != null ) {
            response.setEtag( etag );
        }
        if( resource.getModifiedDate() != null ) {
            response.setLastModifiedHeader( resource.getModifiedDate() );
        }
    }

    public void respondBadRequest( Resource resource, Response response, Request request ) {
        response.setStatus( Response.Status.SC_BAD_REQUEST );
    }

    public void respondDeleteFailed( Request request, Response response, Resource resource, Status status ) {
        response.setStatus( status );
    }
}

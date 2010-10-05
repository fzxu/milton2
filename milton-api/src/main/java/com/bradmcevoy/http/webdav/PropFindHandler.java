package com.bradmcevoy.http.webdav;

import com.bradmcevoy.property.PropertySource;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.AuthenticationService.AuthStatus;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.webdav.PropFindRequestFieldParser.ParseResult;
import com.bradmcevoy.property.DefaultPropertyAuthoriser;
import com.bradmcevoy.property.PropertyHandler;
import com.bradmcevoy.property.PropertyAuthoriser;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class PropFindHandler implements ExistingEntityHandler, PropertyHandler {

    private static final Logger log = LoggerFactory.getLogger( PropFindHandler.class );
    private final ResourceHandlerHelper resourceHandlerHelper;
    private final PropFindRequestFieldParser requestFieldParser;
    private final WebDavResponseHandler responseHandler;
    private final PropFindPropertyBuilder propertyBuilder;
    private PropertyAuthoriser permissionService = new DefaultPropertyAuthoriser();

    /**
     * 
     * @param resourceHandlerHelper
     * @param resourceTypeHelper
     * @param responseHandler
     */
    public PropFindHandler( ResourceHandlerHelper resourceHandlerHelper, ResourceTypeHelper resourceTypeHelper, WebDavResponseHandler responseHandler, List<PropertySource> propertySources ) {
        this.resourceHandlerHelper = resourceHandlerHelper;

        DefaultPropFindRequestFieldParser defaultFieldParse = new DefaultPropFindRequestFieldParser();
        this.requestFieldParser = new MsPropFindRequestFieldParser( defaultFieldParse ); // use MS decorator for windows support
        this.responseHandler = responseHandler;

        this.propertyBuilder = new PropFindPropertyBuilder( propertySources );
    }

    /**
     *
     * @param resourceHandlerHelper
     * @param requestFieldParser
     * @param responseHandler
     * @param propertyBuilder
     */
    public PropFindHandler( ResourceHandlerHelper resourceHandlerHelper, PropFindRequestFieldParser requestFieldParser, WebDavResponseHandler responseHandler, PropFindPropertyBuilder propertyBuilder ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.requestFieldParser = requestFieldParser;
        this.responseHandler = responseHandler;
        this.propertyBuilder = propertyBuilder;
    }

    public String[] getMethods() {
        return new String[]{Method.PROPFIND.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return ( handler instanceof PropFindableResource );
    }

    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, ConflictException, BadRequestException {
        long t = System.currentTimeMillis();
        try {

            manager.onProcessResourceStart( request, response, resource );

            if( resourceHandlerHelper.isNotCompatible( resource, request.getMethod() ) || !isCompatible( resource ) ) {
                if( log.isTraceEnabled() ) {
                    log.trace( "resource not compatible. Resource class: " + resource.getClass() + " handler: " + getClass() );
                }
                responseHandler.respondMethodNotImplemented( resource, response, request );
                return;
            }

            AuthStatus authStatus = resourceHandlerHelper.checkAuthentication( manager, resource, request );
            if( authStatus.loginFailed ) {
                if( log.isTraceEnabled() ) {
                    log.trace( "authentication failed. respond with: " + responseHandler.getClass().getCanonicalName() + " resource: " + resource.getClass().getCanonicalName() );
                }
                responseHandler.respondUnauthorised( resource, response, request );
                return;
            }

            if( request.getMethod().isWrite ) {
                if( resourceHandlerHelper.isLockedOut( request, resource ) ) {
                    response.setStatus( Status.SC_LOCKED ); // replace with responsehandler method
                    return;
                }
            }

            processExistingResource( manager, request, response, resource );
        } finally {
            t = System.currentTimeMillis() - t;
            manager.onProcessResourceFinish( request, response, resource, t );
        }
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        log.trace( "propfind" );
        PropFindableResource pfr = (PropFindableResource) resource;
        int depth = request.getDepthHeader();
        response.setStatus( Response.Status.SC_MULTI_STATUS );
        response.setContentTypeHeader( Response.XML );
        PropFindRequestFieldParser.ParseResult parseResult;
        try {
            parseResult = requestFieldParser.getRequestedFields( request.getInputStream() );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
        String url = request.getAbsoluteUrl();

        // Check that the current user has permission to write requested fields
        Set<QName> allFields = getAllFields( parseResult, pfr );
        Set<PropertyAuthoriser.CheckResult> errorFields = permissionService.checkPermissions( request, request.getMethod(), PropertyAuthoriser.PropertyPermission.READ, allFields, resource );
        if( errorFields != null && errorFields.size() > 0 ) {
            if( log.isTraceEnabled() ) {
                log.trace( "permissionService denied access: " + permissionService.getClass().getCanonicalName() );
            }
            responseHandler.respondUnauthorised( resource, response, request );
        } else {
            List<PropFindResponse> propFindResponses = propertyBuilder.buildProperties( pfr, depth, parseResult, url );
            if( log.isTraceEnabled() ) {
                log.trace( "responses: " + propFindResponses.size() );
            }
            responseHandler.respondPropFind( propFindResponses, response, request, pfr );
        }
    }

    private Set<QName> getAllFields( ParseResult parseResult, PropFindableResource resource ) {
        Set<QName> set = new HashSet<QName>();
        if( parseResult.isAllProp() ) {
            set.addAll( propertyBuilder.findAllProps( resource ) );
        } else {
            set.addAll( parseResult.getNames() );
        }
        return set;
    }

    public PropertyAuthoriser getPermissionService() {
        return permissionService;
    }

    public void setPermissionService( PropertyAuthoriser permissionService ) {
        this.permissionService = permissionService;
    }
}

package com.ettrema.json;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.webdav.PropFindPropertyBuilder;
import com.bradmcevoy.http.webdav.PropPatchSetter;
import com.bradmcevoy.property.PropertyAuthoriser;
import com.bradmcevoy.property.PropertySource;
import com.ettrema.event.EventManager;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class JsonResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger( JsonResourceFactory.class );
    private final ResourceFactory wrapped;
    private final JsonPropFindHandler propFindHandler;
    private final JsonPropPatchHandler propPatchHandler;
    private final EventManager eventManager;
    private Long maxAgeSecsPropFind = null;
    private static final String DAV_FOLDER = "_DAV";

    public JsonResourceFactory( ResourceFactory wrapped, EventManager eventManager, JsonPropFindHandler propFindHandler, JsonPropPatchHandler propPatchHandler ) {
        this.wrapped = wrapped;
        this.propFindHandler = propFindHandler;
        this.propPatchHandler = propPatchHandler;
        this.eventManager = eventManager;
        log.debug( "created with: " + propFindHandler.getClass().getCanonicalName() );
    }

    public JsonResourceFactory( ResourceFactory wrapped, EventManager eventManager, List<PropertySource> propertySources, PropPatchSetter patchSetter, PropertyAuthoriser permissionService ) {
        this.wrapped = wrapped;
        this.eventManager = eventManager;
        log.debug( "using property sources: " + propertySources.size() );
        this.propFindHandler = new JsonPropFindHandler( new PropFindPropertyBuilder( propertySources ) );
        this.propPatchHandler = new JsonPropPatchHandler( patchSetter, permissionService, eventManager );
    }

    public Resource getResource( String host, String sPath ) {
        if( log.isTraceEnabled() ) {
            log.trace( host + " :: " + sPath );
        }
        Path path = Path.path( sPath );
        Path parent = path.getParent();
        String encodedPath = HttpManager.request().getAbsolutePath();
        if( parent != null && parent.getName() != null && parent.getName().equals( DAV_FOLDER ) ) {
            Path resourcePath = parent.getParent();
            if( resourcePath != null ) {
                String method = path.getName();
                Resource wrappedResource = wrapped.getResource( host, resourcePath.toString() );
                if( wrappedResource != null ) {
                    return wrapResource( host, wrappedResource, method, encodedPath );
                }
            }
        } else {
            return wrapped.getResource( host, sPath );
        }
        return null;
    }

    private Resource wrapResource( String host, Resource wrappedResource, String method, String href ) {
        if( Request.Method.PROPFIND.code.equals( method ) ) {
            if( wrappedResource instanceof PropFindableResource ) {
                return new PropFindJsonResource( (PropFindableResource) wrappedResource, propFindHandler, href, maxAgeSecsPropFind );
            }
        }
        if( Request.Method.PROPPATCH.code.equals( method ) ) {
            return new PropPatchJsonResource( wrappedResource, propPatchHandler, href );
        }
        if( Request.Method.PUT.code.equals( method ) ) {
            if( wrappedResource instanceof PutableResource ) {
                return new PutJsonResource( (PutableResource) wrappedResource, href );
            }
        }
        if( Request.Method.MKCOL.code.equals( method ) ) {
            if( wrappedResource instanceof MakeCollectionableResource ) {
                return new MkcolJsonResource( (MakeCollectionableResource) wrappedResource, href, eventManager );
            }
        }
        if( Request.Method.COPY.code.equals( method ) ) {
            if( wrappedResource instanceof CopyableResource ) {
                return new CopyJsonResource( host, (CopyableResource) wrappedResource, wrapped );
            }
        }
        return null;
    }


    public JsonPropFindHandler getPropFindHandler() {
        return propFindHandler;
    }



    public JsonPropPatchHandler getPropPatchHandler() {
        return propPatchHandler;
    }

    public EventManager getEventManager() {
        return eventManager;
    }



    public Long getMaxAgeSecsPropFind() {
        return maxAgeSecsPropFind;
    }

    public void setMaxAgeSecsPropFind( Long maxAgeSecsPropFind ) {
        this.maxAgeSecsPropFind = maxAgeSecsPropFind;
    }
}

package com.ettrema.json;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.PropPatchRequestParser.ParseResult;
import com.bradmcevoy.http.webdav.PropPatchSetter;
import com.bradmcevoy.http.webdav.PropPatchableSetter;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.property.DefaultPropertyAuthoriser;
import com.bradmcevoy.property.PropertyAuthoriser;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class JsonPropPatchHandler {

    private static final Logger log = LoggerFactory.getLogger( JsonPropPatchHandler.class );
    private final PropPatchSetter patchSetter;
    private PropertyAuthoriser permissionService = new DefaultPropertyAuthoriser();

    public JsonPropPatchHandler( PropPatchSetter patchSetter ) {
        this.patchSetter = patchSetter;
    }

    /**
     * Uses a PropPatchableSetter
     */
    public JsonPropPatchHandler() {
        this.patchSetter = new PropPatchableSetter();
    }

    public PropFindResponse process( Resource wrappedResource, String encodedUrl, Map<String, String> params ) throws NotAuthorizedException {
        Map<QName, String> fields = new HashMap<QName, String>();
        for( String fieldName : params.keySet() ) {
            String sFieldValue = params.get( fieldName );
            QName qn;
            if( fieldName.contains( ":" ) ) {
                // name is of form uri:local  E.g. MyDav:authorName
                String parts[] = fieldName.split( ":" );
                String nsUri = parts[0];
                String localName = parts[1];
                qn = new QName( nsUri, localName );
            } else {
                // name is simple form E.g. displayname, default nsUri to DAV
                qn = new QName( WebDavProtocol.NS_DAV.getPrefix(), fieldName );
            }
            log.debug( "field: " + qn );
            fields.put( qn, sFieldValue );
        }

        ParseResult parseResult = new ParseResult( fields, null );

        Set<PropertyAuthoriser.CheckResult> errorFields = permissionService.checkPermissions(HttpManager.request(),Method.PROPPATCH , PropertyAuthoriser.PropertyPermission.WRITE, fields.keySet(), wrappedResource );
        if( errorFields != null && errorFields.size() > 0 ) {
            log.trace( "authorisation errors" );
            throw new NotAuthorizedException( wrappedResource );
        } else {
            return patchSetter.setProperties( encodedUrl, parseResult, wrappedResource );
        }
    }

    public PropertyAuthoriser getPermissionService() {
        return permissionService;
    }

    public void setPermissionService( PropertyAuthoriser permissionService ) {
        this.permissionService = permissionService;
    }

    
}

package com.ettrema.http.acl;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Handler;
import com.bradmcevoy.http.HttpExtension;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.webdav.PropertyMap;
import com.bradmcevoy.http.webdav.PropertyMap.StandardProperty;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.property.PropertySource;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ACLProtocol implements HttpExtension, PropertySource {

    private static final Logger log = LoggerFactory.getLogger( ACLProtocol.class );
    private final PropertyMap propertyMap;

    public ACLProtocol(WebDavProtocol webDavProtocol) {
        propertyMap = new PropertyMap( WebDavProtocol.NS_DAV );
        propertyMap.add( new PrincipalUrl() );
        log.debug( "registering the ACLProtocol as a property source");
        webDavProtocol.addPropertySource( this );
    }

    /**
     * No methods currently defined
     * 
     * @return
     */
    public Set<Handler> getHandlers() {
        return Collections.EMPTY_SET;
    }


    public Object getProperty( QName name, Resource r ) {
        log.debug( "getProperty: " + name.getLocalPart() );
        return propertyMap.getProperty( name, r );
    }

    public void setProperty( QName name, Object value, Resource r ) {
        log.debug( "setProperty: " + name.getLocalPart() );
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public PropertyMetaData getPropertyMetaData( QName name, Resource r ) {
        log.debug( "getPropertyMetaData: " + name.getLocalPart() );
        return propertyMap.getPropertyMetaData( name, r );
    }

    public void clearProperty( QName name, Resource r ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public List<QName> getAllPropertyNames( Resource r ) {
        log.debug( "getAllPropertyNames" );
        List<QName> list = new ArrayList<QName>();
        list.addAll( propertyMap.getAllPropertyNames( r ) );
        return list;
    }

    class PrincipalUrl implements StandardProperty<String> {

        public String fieldName() {
            return "principal-URL";
        }

        public String getValue( PropFindableResource res ) {
            // TODO: should be the owner of 'res'
            if( res instanceof AccessControlledResource ) {
                AccessControlledResource acr = (AccessControlledResource) res;
                return acr.getPrincipalURL();
            } else {
                log.warn( "requested property 'principal-url', but resource doesnt implement AccessControlledResource: " + res.getClass().getCanonicalName() );
                return null;
            }
        }

        public Class<String> getValueClass() {
            return String.class;
        }
    }

    class CurrentUserPrivledges implements StandardProperty<PriviledgeList> {

        public String fieldName() {
            return "current-user-privilege-set";
        }

        public PriviledgeList getValue( PropFindableResource res ) {
            if( res instanceof AccessControlledResource ) {
                AccessControlledResource acr = (AccessControlledResource) res;
                Auth auth = HttpManager.request().getAuthorization();
                List<Priviledge> list = acr.getPriviledges( auth );
                PriviledgeList privs = new PriviledgeList(list);
                return privs;
            } else {
                return null;
            }
        }

        public Class<PriviledgeList> getValueClass() {
            return PriviledgeList.class;
        }
    }
}

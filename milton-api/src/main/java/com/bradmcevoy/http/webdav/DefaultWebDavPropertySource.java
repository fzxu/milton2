package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Utils;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.http11.DefaultHttp11ResponseHandler;
import com.bradmcevoy.http.webdav.WebDavProtocol.SupportedLocks;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class DefaultWebDavPropertySource implements WebDavPropertySource{

    private final Map<String, PropertyAccessor> writersMap = new HashMap<String,PropertyAccessor>();

    private final ResourceTypeHelper resourceTypeHelper;

    public DefaultWebDavPropertySource(ResourceTypeHelper resourceTypeHelper) {
        this.resourceTypeHelper = resourceTypeHelper;
        add( new ContentLengthPropertyWriter() );
        add( new ContentTypePropertyWriter() );
        add( new CreationDatePropertyWriter() );
        add( new DisplayNamePropertyWriter() );
        add( new LastModifiedDatePropertyWriter() );
        add( new ResourceTypePropertyWriter() );
        add( new EtagPropertyWriter() );

        add( new SupportedLockPropertyWriter() );
        add( new LockDiscoveryPropertyWriter() );

        add( new MSIsCollectionPropertyWriter() );
        add( new MSIsReadOnlyPropertyWriter() );
        add( new MSNamePropertyWriter() );
    }

    

    public Object getProperty( QName name, Resource r ) {
        if(!name.getNamespaceURI().equals( WebDavProtocol.NS_DAV)) return null;
        PropertyAccessor pa = writersMap.get( name.getLocalPart());
        if( pa == null ) return null;
        if( r instanceof PropFindableResource ) {
            return pa.getValue( (PropFindableResource) r);
        } else {
            return null;
        }
    }

    public void setProperty( QName name, Object value, Resource r ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean hasProperty( QName name, Resource r ) {
        if(!name.getNamespaceURI().equals( WebDavProtocol.NS_DAV)) return false;
        PropertyAccessor pa = writersMap.get( name.getLocalPart());
        if( pa == null ) return false;
        if( r instanceof PropFindableResource ) {
            return true;
        } else {
            return false;
        }

    }

    public void clearProperty( QName name, Resource r ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public List<QName> getAllPropertyNames( Resource r ) {
        List<QName> list = new ArrayList<QName>();
        for( String nm : this.writersMap.keySet() ) {
            QName qname = new QName( WebDavProtocol.NS_DAV, nm);
            list.add( qname );
        }
        return list;
    }


    class DisplayNamePropertyWriter implements PropertyAccessor<String> {

        public void append( XmlWriter writer, PropFindableResource res, String href ) {
            String s = nameEncode( getValue( res ) );
            sendStringProp( writer, "D:" + fieldName(), s );
        }

        public String getValue( PropFindableResource res ) {
            return res.getName();
        }

        public String fieldName() {
            return "displayname";
        }
    }

    class LastModifiedDatePropertyWriter implements PropertyAccessor<Date> {

        public String fieldName() {
            return "getlastmodified";
        }

        public Date getValue( PropFindableResource res ) {
            return res.getModifiedDate();
        }
    }

    class CreationDatePropertyWriter implements PropertyAccessor<Date> {

        public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
            sendDateProp( xmlWriter, "D:" + fieldName(), getValue( res ) );
        }

        public Date getValue( PropFindableResource res ) {
            return res.getCreateDate();
        }

        public String fieldName() {
            return "creationdate";
        }
    }

    class ResourceTypePropertyWriter implements PropertyAccessor<List<QName>> {

        public List<QName> getValue( PropFindableResource res ) {
            return resourceTypeHelper.getResourceTypes( res );
        }

        public String fieldName() {
            return "resourcetype";
        }
    }

    class ContentTypePropertyWriter implements PropertyAccessor<String> {

        public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
            String ct = getValue( res );
            sendStringProp( xmlWriter, "D:" + fieldName(), ct );
        }

        public String getValue( PropFindableResource res ) {
            if( res instanceof GetableResource ) {
                GetableResource getable = (GetableResource) res;
                return getable.getContentType( null );
            } else {
                return "";
            }
        }

        public String fieldName() {
            return "getcontenttype";
        }
    }

    class ContentLengthPropertyWriter implements PropertyAccessor<Long> {

        public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
            Long ll = getValue( res );
            sendStringProp( xmlWriter, "D:" + fieldName(), ll == null ? "" : ll.toString() );
        }

        public Long getValue( PropFindableResource res ) {
            if( res instanceof GetableResource ) {
                GetableResource getable = (GetableResource) res;
                Long l = getable.getContentLength();
                return l;
            } else {
                return null;
            }
        }

        public String fieldName() {
            return "getcontentlength";
        }
    }

    class EtagPropertyWriter implements PropertyAccessor<String> {

        public void append( XmlWriter writer, PropFindableResource resource, String href ) {
            String etag = getValue( resource );
            if( etag != null ) {
                sendStringProp( writer, "D:getetag", etag );
            }
        }

        public String getValue( PropFindableResource res ) {
            String etag = DefaultHttp11ResponseHandler.generateEtag( res );
            return etag;
        }

        public String fieldName() {
            return "getetag";
        }
    }

//    <D:supportedlock/><D:lockdiscovery/>
    class LockDiscoveryPropertyWriter implements PropertyAccessor<LockToken> {
        public LockToken getValue( PropFindableResource res ) {
            if( !( res instanceof LockableResource ) ) return null;
            LockableResource lr = (LockableResource) res;
            LockToken token = lr.getCurrentLock();
            return token;
        }

        public String fieldName() {
            return "supportedlock";
        }
    }

    class SupportedLockPropertyWriter implements PropertyAccessor<Object> {

        public Object getValue( PropFindableResource res ) {
            if( res instanceof LockableResource ) {
                return new SupportedLocks();
            } else {
                return null;
            }
        }

        public String fieldName() {
            return "supportedlock";
        }
    }

    // MS specific fields
    class MSNamePropertyWriter extends DisplayNamePropertyWriter {

        @Override
        public String fieldName() {
            return "name";
        }
    }


    class MSIsCollectionPropertyWriter implements PropertyAccessor<Boolean> {

        @Override
        public String fieldName() {
            return "iscollection";
        }

        public Boolean getValue( PropFindableResource res ) {
            return (res instanceof CollectionResource);
        }
    }

    class MSIsReadOnlyPropertyWriter implements PropertyAccessor<Boolean> {

        @Override
        public String fieldName() {
            return "isreadonly";
        }

        public Boolean getValue( PropFindableResource res ) {
            return !(res instanceof PutableResource);
        }
    }


    private String nameEncode( String s ) {
        //return Utils.encode(href, false); // see MIL-31
        return Utils.escapeXml( s );
    //return href.replaceAll("&", "&amp;");  // http://www.ettrema.com:8080/browse/MIL-24
    }

    protected void sendStringProp( XmlWriter writer, String name, String value ) {
        String s = value;
        if( s == null ) {
            writer.writeProperty( null, name );
        } else {
            writer.writeProperty( null, name, s );
        }
    }

    void sendDateProp( XmlWriter writer, String name, Date date ) {
        sendStringProp( writer, name, ( date == null ? null : DateUtils.formatDate( date ) ) );
    }

    private void add( PropertyAccessor pw ) {
        writersMap.put( pw.fieldName(), pw );
    }

}

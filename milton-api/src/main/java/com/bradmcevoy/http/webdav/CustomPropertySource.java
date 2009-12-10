package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.CustomProperty;
import com.bradmcevoy.http.CustomPropertyResource;
import com.bradmcevoy.http.Resource;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class CustomPropertySource implements WebDavPropertySource {

    public Object getProperty( QName name, Resource r ) {
        CustomProperty prop = lookupProperty( name, r );
        if( prop != null ) {
            return prop.getTypedValue();
        } else {
            return null;
        }
    }

    public void setProperty( QName name, Object value, Resource r ) {
        CustomProperty prop = lookupProperty( name, r );
        if( prop != null ) {
            prop.setFormattedValue( value.toString() );
        } else {
            throw new RuntimeException( "property not found: " + name.getLocalPart() );
        }
    }

    public boolean hasProperty( QName name, Resource r ) {
        CustomProperty prop = lookupProperty( name, r );
        return prop != null;
    }

    public void clearProperty( QName name, Resource r ) {
        CustomProperty prop = lookupProperty( name, r );
        prop.setFormattedValue( null );
    }

    private CustomProperty lookupProperty( QName name, Resource r ) {
        if( r instanceof CustomPropertyResource ) {
            CustomPropertyResource cpr = (CustomPropertyResource) r;
            if( cpr.getNameSpaceURI().equals( name.getNamespaceURI() ) ) {
                return cpr.getProperty( name.getLocalPart() );
            } else {
                return null;
            }
        } else {
            return null;
        }

    }
}

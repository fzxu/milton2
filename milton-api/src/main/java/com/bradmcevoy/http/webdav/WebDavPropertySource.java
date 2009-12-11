package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.Resource;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Defines a source of properties. This is used by both propfind and proppatch
 * 
 *
 * @author brad
 */
public interface WebDavPropertySource {

    Object getProperty( QName name, Resource r );

    void setProperty( QName name, Object value, Resource r );

    boolean hasProperty( QName name, Resource r );

    /**
     * Remove the given property. There may be a semantic difference in some
     * cases between setting a property to a null value vs removing the property.
     * Generally this should completely the remove the property if possible.
     *
     * @param name
     * @param r
     */
    void clearProperty( QName name, Resource r );

    /**
     *
     * @param r - the resource which may contain properties
     * 
     * @return - all properties known by this source on the given resource.
     * This list should be exclusive. Ie only return properties not returned
     * by any other source
     */
    List<QName> getAllPropertyNames(Resource r);
}

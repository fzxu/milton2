package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.Resource;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public interface WebDavPropertySource {

    Object getProperty( QName name, Resource r );

    void setProperty( QName name, Object value, Resource r );

    boolean hasProperty( QName name, Resource r );

    void clearProperty( QName name, Resource r );
}

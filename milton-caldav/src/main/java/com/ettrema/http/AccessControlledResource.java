package com.ettrema.http;

import com.bradmcevoy.http.Resource;

/**
 *
 * @author alex
 */
public interface AccessControlledResource extends Resource{
    /**
     * A URL which identifies the principal owner of this resource
     *
     * See http://greenbytes.de/tech/webdav/rfc3744.html#PROPERTY_principal-URL
     *
     * @return
     */
    String getPrincipalURL();
}

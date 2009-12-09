package com.bradmcevoy.http.webdav;

import java.io.InputStream;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Parses the body of a PROPFIND request and returns the requested fields
 *
 * @author brad
 */
public interface PropFindRequestFieldParser {

    Set<QName> getRequestedFields( InputStream in );
}

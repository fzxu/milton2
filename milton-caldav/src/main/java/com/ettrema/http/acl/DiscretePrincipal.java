package com.ettrema.http.acl;

import com.bradmcevoy.http.webdav.WebDavProtocol;
import javax.xml.namespace.QName;

/**
 * Indicates a principle which is identifiable by a URL, like a user or
 * an application defined group
 *
 * @author brad
 */
public class DiscretePrincipal implements Principal{

    private static final QName ID_TYPE = new QName( WebDavProtocol.NS_DAV.getName(), "href");

    private final String url;

    public DiscretePrincipal( String url ) {
        this.url = url;
    }
        
    /**
     * A URL to identify this principle
     *
     * @return
     */
    public String getPrincipalURL() {
        return url;
    }

	@Override
    public PrincipleId getIdenitifer() {
        return new PrincipleId() {

			@Override
            public QName getIdType() {
                return ID_TYPE;
            }

			@Override
            public String getValue() {
                return url;
            }
        };
    }

}

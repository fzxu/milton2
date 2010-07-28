package com.ettrema.http.acl;

import javax.xml.namespace.QName;

/**
 * Marker interface to identify those classes which can act as Principals
 *
 * There are 2 types
 *  - Discrete: identifies a particular resource, which might be a user or group
 *  - DAV: a dynamically evaluated as defined by the ACL spec
 *
 * @author brad
 */
public interface Principal {

    /**
     * A means to identify the principle to webdav clients
     *
     * @return
     */
    PrincipleId getIdenitifer();

    /**
     * Eg
     * <D:href>http://www.example.com/acl/groups/maintainers</D:href>
     *
     * or
     *
     * <D:all/> 
     *
     */
    public static interface PrincipleId {
        /**
         * Eg D:href or D:all
         *
         * @return
         */
        QName getIdType();

        /**
         * Eg null for an idType of "D:all", or http://blah.com/users/sam for "D:href"
         *
         * @return
         */
        String getValue();
    }
}

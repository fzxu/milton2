package com.ettrema.json;

import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class TypeResourceMatcher implements ResourceMatcher{

    private final Class matchClass;

    public TypeResourceMatcher( Class matchClass ) {
        this.matchClass = matchClass;
    }
    

    public boolean matches( Resource r ) {
        return r.getClass().isAssignableFrom( matchClass );
    }

}

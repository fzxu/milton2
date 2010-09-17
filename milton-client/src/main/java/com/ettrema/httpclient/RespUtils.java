package com.ettrema.httpclient;

import org.dom4j.Element;

/**
 *
 * @author mcevoyb
 */
public class RespUtils {

    public static String asString( Element el, String name ) {
        Element elChild = el.element( name );
        if( elChild == null ) return null;
        return elChild.getText();
    }

    public static Long asLong( Element el, String name ) {
        String s = asString( el, name );
        if( s == null || s.length()==0 ) return null;
        long l = Long.parseLong( s );
        return l;
    }

    public static boolean hasChild( Element el, String name ) {
        if( el == null ) return false;
        Element elChild = el.element( name );
        return !( elChild == null );
    }
}

package com.ettrema.httpclient;

import org.dom4j.QName;
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

    public static String asString( Element el, QName qname ) {
//        System.out.println("asString: " + qname + " in: " + el.getName());
//        for( Object o : el.elements() ) {
//            Element e = (Element) o;
//            System.out.println(" - " + e.getQualifiedName());
//        }
        Element elChild = el.element( qname );
        if( elChild == null ) return null;
        return elChild.getText();
    }    
    
    public static Long asLong( Element el, String name ) {
        String s = asString( el, name );
        if( s == null || s.length()==0 ) return null;
        long l = Long.parseLong( s );
        return l;
    }
    
    public static Long asLong( Element el, QName name ) {
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
    
    public static boolean hasChild( Element el, QName name ) {
        if( el == null ) return false;
        Element elChild = el.element( name );
        return !( elChild == null );
    }    
}

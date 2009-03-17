/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bradmcevoy.http;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class UtilsTest extends TestCase {
    
    public UtilsTest(String testName) {
        super(testName);
    }

    public void testFindChild() {
    }

    public void testNow() {
    }

    public void testAddSeconds_Date_long() {
    }

    public void testAddSeconds_Date_int() {
    }

    public void testGetProtocol() {
    }

    public void testEncodeHref() {
        String s =  Utils.encodeHref("http://localhost:8085/webdav/");
        System.out.println(s);
        assertEquals("http://localhost:8085/webdav/",s);
        assertEquals("http://localhost:8085/webdav/special%20chars",Utils.encodeHref("http://localhost:8085/webdav/special chars"));
        s = Utils.encodeHref("http://localhost:8085/webdav/ampersand&");
        System.out.println(s);
        assertEquals("http://localhost:8085/webdav/ampersand%26",s);
        assertEquals("http://www.example.com/you%20I%2010%25?%20weird%20weirder%20ne%C3%A9",Utils.encodeHref("http://www.example.com/you I 10%? weird weirder ne�"));

        assertEquals("https://localhost:8085/webdav/",Utils.encodeHref("https://localhost:8085/webdav/"));
    }

    public void testPercentEncode() {
        for( int i=0; i<70; i++ ) {
            System.out.println(i + " = " + (char)i);
        }
        assertEquals("", Utils.percentEncode(""));
        assertEquals("abc", Utils.percentEncode("abc"));
        assertEquals("%20", Utils.percentEncode(" "));
        assertEquals("ampersand%26", Utils.percentEncode("ampersand&"));
    }

//    public void testEncode() {
//        assertEquals("abc", Utils.encodeHref("abc", false));
//        assertEquals("a c", Utils.encodeHref("a c", false));
//        assertEquals("a%C3%A6c", Utils.encodeHref("a�c", false));
//        assertEquals("ac%3F", Utils.encodeHref("ac?", false));
//        assertEquals("a%26c", Utils.encodeHref("a&c", false));
//
//        assertEquals("a%20c", Utils.encodeHref("a c", true));
//    }
//

}

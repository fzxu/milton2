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
        assertEquals("http://www.example.com/you%20I%2010%25?%20weird%20weirder%20ne%C3%A9",Utils.encodeHref("http://www.example.com/you I 10%? weird weirder neé"));

        assertEquals("https://localhost:8085/webdav/",Utils.encodeHref("https://localhost:8085/webdav/"));
    }

    public void testPercentEncode() {
//        for( int i=0; i<70; i++ ) {
//            System.out.println(i + " = " + (char)i);
//        }
        assertEquals("", Utils.percentEncode(""));
        assertEquals("abc", Utils.percentEncode("abc"));
        assertEquals("%20", Utils.percentEncode(" "));
        assertEquals("ampersand%26", Utils.percentEncode("ampersand&"));
    }


    public void testDecodeHref() {
        String href = "/";
        String result = Utils.decodePath(href);
        assertEquals(result, href);

        href = "/with%20space";
        result = Utils.decodePath(href);
        assertEquals("/with space", result);

    }
}

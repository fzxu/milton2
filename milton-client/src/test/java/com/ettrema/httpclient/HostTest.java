package com.ettrema.httpclient;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class HostTest extends TestCase {
    
    public HostTest(String testName) {
        super(testName);
    }

    public void testFind() throws Exception {
    }

    public void test_find() throws Exception {
    }

    public void testGetFolder() throws Exception {
    }

    public void testOptions() throws Exception {
    }

    public void testGet() {
    }

    public void testHost() {
    }

    public void testHref() {
    }

    public void testUrlEncode() {
        String s = Host.urlEncode( "http://bb.shmego.com/files/Documents/r%20j/RA’S%20WEB%20TEXT.doc");
        System.out.println( "s: " + s );
        assertEquals( "http://bb.shmego.com/files/Documents/r%2520j/RA%E2%80%99S%2520WEB%2520TEXT.doc", s);
    }

    public void testGetPropFindXml() {
    }

    public void testSetPropFindXml() {
    }

    public void testScratch() throws URISyntaxException {
        //java.net.URI uri = new java.net.URI("http://abc.com/a b");
//        java.net.URI uri = java.net.URI.create("http://abc.com/a b");
    }

}

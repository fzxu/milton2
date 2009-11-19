package com.bradmcevoy.http;
import org.apache.commons.codec.binary.Base64;


/**
 * Holds authentication information for a request
 *
 * There are two sets of information:
 *   - that which is present in the request
 *   - that which is determined as part of performing authentication
 *
 * Note that even if authentication fails, this object will still be available
 * in the request - DO NOT USE THE PRESENCE OF THIS OBJECT TO CHECK FOR A VALID LOGIN!!!
 *
 * Instead use the tag property. This will ONLY be not null after a successful
 * authentication
 *
 * @author brad
 */
public class Auth {

    /**
     * Holds application specific user data, as returned from the authenticate
     * method on Resource
     *
     * This should be used to test for a valid login.
     */
    private Object tag;
    
    public enum Scheme {
        BASIC
    };
    
    public final Scheme scheme;
    public final String user;
    public final String password;
    
    public Auth(String sAuth) {
        int pos = sAuth.indexOf(" ");
        String schemeCode;
        String enc;
        if( pos >= 0 ) {
            schemeCode = sAuth.substring(0,pos);
            scheme = Scheme.valueOf(schemeCode.toUpperCase());
            enc = sAuth.substring(pos+1);
        } else {
            // assume basic
            scheme = Scheme.BASIC;
            enc = sAuth;
        }
        byte[] bytes = Base64.decodeBase64( enc.getBytes() );
        String s = new String(bytes);
        pos = s.indexOf(":");
        if( pos >= 0 ) {
            user = s.substring(0,pos);
            password = s.substring(pos+1);
        } else {
            user = s;
            password = null;
        }
    }
    
    public Auth(String user, Object userTag) {
        this.scheme = Scheme.BASIC;
        this.user = user;
        this.password = null;
        this.tag = userTag;
    }

    /**
     *
     * @return - the user property in the request. This MIGHT NOT be an
     * actual user
     */
    public String getUser() {
        return user;
    }

    /**
     * Set after a successful authenticate method with a not-null value
     *
     * The actual value will be application dependent
     */
    void setTag(Object authTag) {
        tag = authTag;
    }

    /**
     * Holds application specific user data, as returned from the authenticate
     * method on Resource
     *
     * This should be used to test for a valid login.
     */
    public Object getTag() {
        return tag;
    }           
}

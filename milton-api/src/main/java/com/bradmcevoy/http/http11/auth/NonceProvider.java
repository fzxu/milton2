package com.bradmcevoy.http.http11.auth;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public interface NonceProvider {

    public String createNonce( Resource resource, Request request );

    public enum NonceValidity {
        OK,
        EXPIRED,
        INVALID
    }
    

    /**
     * Check to see if the given nonce is known. If known, is it still valid
     * or has it expired
     *
     * @param nonce
     * @return
     */
    NonceValidity getNonceValidity(String nonce);
}

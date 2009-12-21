package com.bradmcevoy.http.http11.auth;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class SimpleMemoryNonceProvider implements NonceProvider {
    private static final Logger log = LoggerFactory.getLogger( SimpleMemoryNonceProvider.class );
    private final int nonceValiditySeconds;
    private Map<UUID, Nonce> nonces = new ConcurrentHashMap<UUID, Nonce>();
    private final ExpiredNonceRemover remover;

    public SimpleMemoryNonceProvider( int nonceValiditySeconds ) {
        this.nonceValiditySeconds = nonceValiditySeconds;
        this.remover = new ExpiredNonceRemover( nonces, nonceValiditySeconds );
        log.debug( "created");
    }

    public SimpleMemoryNonceProvider( int nonceValiditySeconds, ExpiredNonceRemover remover ) {
        this.nonceValiditySeconds = nonceValiditySeconds;
        this.remover = remover;
    }



    public String createNonce( Resource resource, Request request ) {
        UUID id = UUID.randomUUID();
        Date now = new Date();
        Nonce n = new Nonce( id, now );
        nonces.put( n.getValue(), n );
        log.debug( "created nonce: " + n.getValue());
        log.debug( "map size: " + nonces.size());
        return n.getValue().toString();
    }

    public NonceValidity getNonceValidity( String nonce ) {
        log.debug( "getNonceValidity: " + nonce);
        UUID value = null;
        try {
            value = UUID.fromString( nonce );
        } catch( Exception e ) {
            log.debug( "couldnt parse nonce");
            return NonceValidity.INVALID;
        }
        Nonce n = nonces.get( value );
        if( n == null ) {
            log.debug( "not found in map of size: " + nonces.size());
            return NonceValidity.INVALID;
        } else {
            if( isExpired(n.getIssued())) {
                log.debug( "nonce has expired");
                return NonceValidity.EXPIRED;
            } else {
                log.debug( "nonce ok");
                return NonceValidity.OK;
            }
        }
    }

    private boolean isExpired( Date issued ) {
        long dif = (System.currentTimeMillis() - issued.getTime()) / 1000;
        return dif > nonceValiditySeconds;
    }
}

package com.ettrema.http.carddav;

import com.bradmcevoy.http.values.HrefList;
import com.ettrema.http.acl.DiscretePrincipal;

/**
 *
 * @author brad
 */
public interface CardDavPrincipal extends DiscretePrincipal {
    /**
     * This is usually a single href which identifies the collection which
     * contains the users addressbooks. This might be the user's own href.
     *
     */
    HrefList getAddressBookHomeSet();

}

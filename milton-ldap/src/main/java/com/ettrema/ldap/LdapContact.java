package com.ettrema.ldap;

import com.bradmcevoy.http.PropFindableResource;

/**
 * Represents an entry in an address book. It is a map of attributes
 *
 * @author brad
 */
public interface LdapContact extends PropFindableResource {
	
	String getImapUid();
	
}

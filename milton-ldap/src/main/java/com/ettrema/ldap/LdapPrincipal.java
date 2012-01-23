package com.ettrema.ldap;

import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public interface LdapPrincipal {


	String getAlias();
	

	/**
	 * Search for contacts in this user's private contact list. Generally these contacts
	 * will not be User accounts
	 * 
	 * @param contactReturningAttributes
	 * @param condition
	 * @param maxCount
	 * @return 
	 */
	List<LdapContact> searchContacts(Condition condition, int maxCount);
}

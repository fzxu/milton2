package com.ettrema.ldap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brad
 */
public interface User {

	/**
	 * Not really sure what this is for, but i think you can just return the username
	 * 
	 * @return 
	 */
	String getAlias();
	
	Map<String, Contact> galFind(Condition equalTo, Set<String> convertLdapToContactReturningAttributes, int sizeLimit);

	List<Contact> searchContacts(Set<String> contactReturningAttributes, Condition condition, int maxCount);
}

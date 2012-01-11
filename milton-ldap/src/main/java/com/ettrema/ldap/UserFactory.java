package com.ettrema.ldap;

import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public interface UserFactory {
	

	/**
	 * Used for SASL authentication
	 * 
	 * @param userName
	 * @return 
	 */
	String getUserPassword(String userName);

	User getUser(String userName, String password);
	
	/**
	 * Search for contacts in the Global Address List
	 * 
	 * @param equalTo
	 * @param convertLdapToContactReturningAttributes
	 * @param sizeLimit
	 * @return 
	 */
	List<Contact> galFind(Condition equalTo, Set<String> convertLdapToContactReturningAttributes, int sizeLimit);	
}

package com.ettrema.ldap;

import com.bradmcevoy.http.Resource;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public interface User {


	String getAlias();
	

	/**
	 * I don't know what the difference is between this and galFind
	 * 
	 * @param contactReturningAttributes
	 * @param condition
	 * @param maxCount
	 * @return 
	 */
	List<Contact> searchContacts(Set<String> contactReturningAttributes, Condition condition, int maxCount);
}

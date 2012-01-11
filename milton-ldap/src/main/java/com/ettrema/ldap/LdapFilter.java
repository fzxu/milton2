package com.ettrema.ldap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brad
 */
public interface LdapFilter {

	Condition getContactSearchFilter();

	List<Contact> findInGAL(User user, Set<String> returningAttributes, int sizeLimit) throws IOException;

	void add(LdapFilter filter);

	boolean isFullSearch();

	boolean isMatch(Map<String, String> person);

}

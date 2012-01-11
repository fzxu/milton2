package com.ettrema.ldap;

import java.util.Map;

/**
 * Represents an entry in an address book. It is a map of attributes
 *
 * @author brad
 */
public interface Contact extends  Map<String, String>{

	/**
	 * Get the unique id of this contact
	 * 
	 * @return 
	 */
	String getUniqueId();
	
}

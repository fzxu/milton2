package com.ettrema.ldap;

import java.util.HashMap;

/**
 *
 * @author brad
 */
public class MapContact extends HashMap<String, String> implements Contact {

	private final String id;

	public MapContact(String id) {
		this.id = id;
	}
	
		
	@Override
	public String getUniqueId() {
		return id;
	}

	
}

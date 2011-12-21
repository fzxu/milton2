package com.ettrema.ldap;

/**
 *
 * @author brad
 */
public interface UserSessionFactory {
	
	User getUser(String userName);

	/**
	 * Used for SASL authentication
	 * 
	 * @param userName
	 * @return 
	 */
	String getUserPassword(String userName);

	User getUser(String userName, String password);
}

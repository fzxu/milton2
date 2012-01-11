package com.ettrema.ldap;

/**
 *
 * @author brad
 */
public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting milton ldap...");
		MemoryUserSessionFactory factory = new MemoryUserSessionFactory();
		factory.addUser("userA", "password", "joe", "bloggs", "joeblogss@blogs.com");
		factory.addUser("userB", "password", "joe2", "bloggs2", "joeblogss2@blogs.com");
		factory.addUser("userC", "password", "joe3", "bloggs3", "joeblogss3@blogs.com");
		LdapServer ldapServer = new LdapServer(factory, 8389, true, "localhost");
		System.out.println("Created server, binding to address...");
		ldapServer.bind();
		System.out.println("Starting server...");
		ldapServer.start();
		System.out.println("Started");
		while(true) {
			Thread.sleep(5000);
			System.out.println("still running...");
		}
	}
			
}

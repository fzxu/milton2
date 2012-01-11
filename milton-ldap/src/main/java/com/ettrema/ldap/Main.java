package com.ettrema.ldap;

import com.bradmcevoy.property.PropertySource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting milton ldap...");
		MemoryUserSessionFactory factory = new MemoryUserSessionFactory();
		List<PropertySource> propertySources = new ArrayList<PropertySource>();
		
		// TODO: add property sources
		
		factory.addUser("userA", "password", "joe", "bloggs", "joeblogss@blogs.com");
		factory.addUser("userB", "password", "joe2", "bloggs2", "joeblogss2@blogs.com");
		factory.addUser("userC", "password", "joe3", "bloggs3", "joeblogss3@blogs.com");
		LdapServer ldapServer = new LdapServer(factory, propertySources, 8389, true, "localhost");
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

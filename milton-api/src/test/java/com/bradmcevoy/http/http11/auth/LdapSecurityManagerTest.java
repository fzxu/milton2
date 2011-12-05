package com.bradmcevoy.http.http11.auth;

import junit.framework.TestCase;

/**
 *
 * @author bradm
 */
public class LdapSecurityManagerTest extends TestCase {
	
	LdapSecurityManager securityManager;
	
	public LdapSecurityManagerTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		securityManager = new LdapSecurityManager();
	}



	public void testAuthoriseBasic() {
		securityManager.authenticate("brad", "Password1");
	}

	public void testAuthoriseDigest() {
	}
	
}

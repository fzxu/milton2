package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.Resource;
import com.ettrema.common.LogUtils;
import com.ettrema.ldap.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class TUserFactory implements UserFactory {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TUserFactory.class);
	private final TResourceFactory resourceFactory;

	public TUserFactory(TResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

	@Override
	public String getUserPassword(String userName) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public LdapPrincipal getUser(String userName, String password) {
		TCalDavPrincipal user = TResourceFactory.findUser(userName);
		if (user.authenticate(password)) {
			return user;
		} else {
			return null;
		}
	}

	@Override
	public List<LdapContact> galFind(Condition condition, Set<String> attributes, int sizeLimit) {
		log.trace("galFind");
		List<LdapContact> results = new ArrayList<LdapContact>();

		for (Resource r : TResourceFactory.users.children) {
			if (r instanceof TCalDavPrincipal) {
				TCalDavPrincipal user = (TCalDavPrincipal) r;
				if (condition == null || condition.isMatch(user)) {
					log.debug("searchContacts: add to results:" + user.getAlias());
					LdapContact c = toContact(user, attributes);
					results.add(c);
					if (results.size() >= sizeLimit) {
						break;
					}
				}
			}
		}
		log.debug("galFind: results: " + results.size());
		return results;
	}
	
	private LdapContact toContact(TCalDavPrincipal user, Set<String> attributes) {
		MapContact contact = new MapContact(user.getUniqueId());
		for (String a : attributes) {
			String value = user.get(a);
			if (value != null) {
				contact.put(a, value);
			} else {
				log.trace("toContact: property not found: " + a);
				//contact.put(a, "Unknown property: " + a);
			}
		}
		return contact;
	}	
}

package com.ettrema.ldap;

import com.ettrema.common.LogUtils;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MemoryUserSessionFactory implements UserFactory {

	private static final Logger log = LoggerFactory.getLogger(MemoryUserSessionFactory.class);
	private final Map<String, MemoryUser> users;

	public MemoryUserSessionFactory(Map<String, MemoryUser> users) {
		this.users = users;
	}

	public MemoryUserSessionFactory() {
		this.users = new HashMap<String, MemoryUser>();
	}

	public void addUser(String name, String password, String givenName, String surname, String email) {
		MemoryUser u = new MemoryUser(name, password, givenName, surname);
		u.setEmail(email);
		users.put(name, u);
	}

	public MemoryUser getUser(String userName) {
		MemoryUser u = users.get(userName);
		LogUtils.debug(log, "getUser", userName, "result=", u);
		return u;
	}

	@Override
	public String getUserPassword(String userName) {
		MemoryUser user = getUser(userName);
		if (user == null) {
			LogUtils.warn(log, "getUserPassword: user not found", userName);
			return null;
		} else {
			return user.getPassword();
		}
	}

	@Override
	public User getUser(String userName, String password) {
		MemoryUser user = getUser(userName);
		if (user == null) {
			LogUtils.warn(log, "getUser: user not found", userName);
			return null;
		} else {
			if (password.equals(user.getPassword())) {
				LogUtils.debug(log, "getUser: user authentuicated ok", userName);
				return user;
			} else {
				LogUtils.warn(log, "getUser: incorrect password", userName);
				return null;
			}
		}
	}

	@Override
	public Map<String, Contact> galFind(Condition condition, Set<String> attributes, int sizeLimit) {
		log.trace("galFind");
		Map<String, Contact> results = new HashMap<String, Contact>();
		for (MemoryUser user : users.values()) {
			if (condition == null || condition.isMatch(user)) {
				LogUtils.debug(log, "searchContacts: add to results", user.alias);
				Contact c = toContact(user, attributes);
				results.put(user.getAlias(), c);
				if (results.size() >= sizeLimit) {
					break;
				}
			}
		}
		LogUtils.debug(log, "galFind: results: ", results.size());
		return results;
	}

	private Contact toContact(MemoryUser user, Set<String> attributes) {
		MapContact contact = new MapContact(user.getUniqueId());
		for (String a : attributes) {
			String value = user.get(a);
			if (value != null) {
				contact.put(a, value);
			} else {
				LogUtils.trace(log, "toContact: property not found: ", a);
				//contact.put(a, "Unknown property: " + a);
			}
		}
		return contact;
	}

	public class MemoryUser extends MapContact implements User, Contact {

		private final String alias;
		private String password;

		public MemoryUser(String alias, String password, String givenName,String surname) {
			super(alias);
			this.alias = alias;
			this.password = password;
			put("imapUid", alias);
			put("uid", alias);
			put("etag", alias + this.hashCode());
			Date dtBirth = new Date();
			String sBirth = LdapUtils.getZuluDateFormat().format(dtBirth);
			put("birth", sBirth);
			put("bday", sBirth);
			put("im", alias);
			setGivenName(givenName);
			setSurname(surname);
			put("cn", givenName + " " + surname);
		}

		@Override
		public String getAlias() {
			return alias;
		}

		@Override
		public List<Contact> searchContacts(Set<String> attributes, Condition condition, int maxCount) {
			return Collections.EMPTY_LIST;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getEtag() {
			return this.hashCode() + "";
		}

		@Override
		public String get(Object key) {
			return super.get(key);
		}

		public String getGivenName() {
			return get("givenName");
		}

		public final void setGivenName(String givenName) {
			put("givenName", givenName);
		}

		public String getSurname() {
			return get("sn");
		}

		public final void setSurname(String surname) {
			put("sn", surname);
		}

		public String getEmail() {
			return get("mail");
		}

		public void setEmail(String s) {
			put("mail", s);
		}
	}
}

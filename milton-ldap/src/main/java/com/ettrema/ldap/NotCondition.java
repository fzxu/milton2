package com.ettrema.ldap;

/**
 *
 * @author brad
 */
public abstract class NotCondition implements Condition {

	protected final Condition condition;

	protected NotCondition(Condition condition) {
		this.condition = condition;
	}

	public boolean isEmpty() {
		return condition.isEmpty();
	}

	public boolean isMatch(Contact contact) {
		return !condition.isMatch(contact);
	}
}
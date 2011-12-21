package com.ettrema.ldap;

/**
 *
 * @author brad
 */
public class MonoCondition implements Condition {

	protected final String attributeName;
	protected final Operator operator;

	protected MonoCondition(String attributeName, Operator operator) {
		this.attributeName = attributeName;
		this.operator = operator;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isMatch(Contact contact) {
		String actualValue = contact.get(attributeName);
		return (operator == Operator.IsNull && actualValue == null)
				|| (operator == Operator.IsFalse && "false".equals(actualValue))
				|| (operator == Operator.IsTrue && "true".equals(actualValue));
	}
}
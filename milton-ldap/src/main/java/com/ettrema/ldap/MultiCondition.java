package com.ettrema.ldap;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public abstract class MultiCondition implements Condition {

	protected final Operator operator;
	protected final List<Condition> conditions;

	protected MultiCondition(Operator operator, Condition... conditions) {
		this.operator = operator;
		this.conditions = new ArrayList<Condition>();
		for (Condition condition : conditions) {
			if (condition != null) {
				this.conditions.add(condition);
			}
		}
	}

	/**
	 * Conditions list.
	 *
	 * @return conditions
	 */
	public List<Condition> getConditions() {
		return conditions;
	}

	/**
	 * Condition operator.
	 *
	 * @return operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Add a new condition.
	 *
	 * @param condition single condition
	 */
	public void add(Condition condition) {
		if (condition != null) {
			conditions.add(condition);
		}
	}

	public boolean isEmpty() {
		boolean isEmpty = true;
		for (Condition condition : conditions) {
			if (!condition.isEmpty()) {
				isEmpty = false;
				break;
			}
		}
		return isEmpty;
	}

	public boolean isMatch(Contact contact) {
		if (operator == Operator.And) {
			for (Condition condition : conditions) {
				if (!condition.isMatch(contact)) {
					return false;
				}
			}
			return true;
		} else if (operator == Operator.Or) {
			for (Condition condition : conditions) {
				if (condition.isMatch(contact)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
}

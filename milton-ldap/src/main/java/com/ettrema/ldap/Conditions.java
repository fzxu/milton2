package com.ettrema.ldap;

import com.ettrema.ldap.Condition.Operator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class Conditions {

	protected static enum FolderQueryTraversal {

		Shallow, Deep
	}
	public static final Map<Operator, String> OPERATOR_MAP = new HashMap<Operator, String>();

	static {
		OPERATOR_MAP.put(Operator.IsEqualTo, " = ");
		OPERATOR_MAP.put(Operator.IsGreaterThanOrEqualTo, " >= ");
		OPERATOR_MAP.put(Operator.IsGreaterThan, " > ");
		OPERATOR_MAP.put(Operator.IsLessThanOrEqualTo, " <= ");
		OPERATOR_MAP.put(Operator.IsLessThan, " < ");
		OPERATOR_MAP.put(Operator.Like, " like ");
		OPERATOR_MAP.put(Operator.IsNull, " is null");
		OPERATOR_MAP.put(Operator.IsFalse, " = false");
		OPERATOR_MAP.put(Operator.IsTrue, " = true");
		OPERATOR_MAP.put(Operator.StartsWith, " = ");
		OPERATOR_MAP.put(Operator.Contains, " = ");
	}

	public static MultiCondition and(Condition... condition) {
		return new MultiCondition(Operator.And, condition);
	}

	public static MultiCondition or(Condition... condition) {
		return new MultiCondition(Operator.Or, condition);
	}

	public static Condition not(Condition condition) {
		if (condition == null) {
			return null;
		} else {
			return new NotCondition(condition);
		}
	}

	public static Condition isEqualTo(String attributeName, String value) {
		return new AttributeCondition(attributeName, Operator.IsEqualTo, value);
	}

	public static Condition isEqualTo(String attributeName, int value) {
		return new AttributeCondition(attributeName, Operator.IsEqualTo, value);
	}

//	public static Condition headerIsEqualTo(String headerName, String value) {
//		return new HeaderCondition(headerName, Operator.IsEqualTo, value);
//	}

	public static Condition gte(String attributeName, String value) {
		return new AttributeCondition(attributeName, Operator.IsGreaterThanOrEqualTo, value);
	}

	public static Condition lte(String attributeName, String value) {
		return new AttributeCondition(attributeName, Operator.IsLessThanOrEqualTo, value);
	}

	public static Condition lt(String attributeName, String value) {
		return new AttributeCondition(attributeName, Operator.IsLessThan, value);
	}

	public static Condition gt(String attributeName, String value) {
		return new AttributeCondition(attributeName, Operator.IsGreaterThan, value);
	}

	public static Condition contains(String attributeName, String value) {
		return new AttributeCondition(attributeName, Operator.Like, value);
	}

	public static Condition startsWith(String attributeName, String value) {
		return new AttributeCondition(attributeName, Operator.StartsWith, value);
	}

	public static Condition isNull(String attributeName) {
		return new MonoCondition(attributeName, Operator.IsNull);
	}

	public static Condition isTrue(String attributeName) {
		return new MonoCondition(attributeName, Operator.IsTrue);
	}

	public static Condition isFalse(String attributeName) {
		return new MonoCondition(attributeName, Operator.IsFalse);
	}
}

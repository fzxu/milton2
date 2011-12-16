package com.ettrema.ldap;

import com.ettrema.ldap.Condition.Operator;

/**
 *
 * @author brad
 */
public abstract class AttributeCondition {
	protected final String attributeName;
        protected final Operator operator;
        protected final String value;

        protected AttributeCondition(String attributeName, Operator operator, String value) {
            this.attributeName = attributeName;
            this.operator = operator;
            this.value = value;
        }

        public boolean isEmpty() {
            return false;
        }

        /**
         * Get attribute name.
         *
         * @return attribute name
         */
        public String getAttributeName() {
            return attributeName;
        }

        /**
         * Condition value.
         *
         * @return value
         */
        public String getValue() {
            return value;
        }
}

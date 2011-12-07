package com.bradmcevoy.http.webdav;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author bradm
 */
public class PropertiesRequest {
	private final boolean allProp;
	private final Map<QName,Property> properties;

	public static PropertiesRequest toProperties(Set<QName> set) {
		Set<Property> props = new HashSet<Property>();
		for(QName n : set ) {
			props.add(new Property(n, null));
		}
		return new PropertiesRequest(props);
	}	
	
	public PropertiesRequest() {
		this.allProp = true;
		this.properties = new HashMap<QName,Property>();
	}
	
	public PropertiesRequest(Set<Property> set) {
		this.allProp = false;
		this.properties = new HashMap<QName,Property>();
		for(Property p : set ) {
			properties.put(p.getName(), p);
		}
	}	
	
	public Property get(QName name) {
		return properties.get(name);
	}
	
	public void add(QName name ) {
		properties.put(name, new Property(name, null));
	}

	public boolean isAllProp() {
		return allProp;
	}

	public Set<QName> getNames() {
		return properties.keySet();
	}

	public Collection<Property> getProperties() {
		return properties.values();
	}
	
	
	
	public static class Property {
		private final QName name;
		private final Set<Property> nested;

		public Property(QName name, Set<Property> nested) {
			this.name = name;
			this.nested = nested;
		}

		public QName getName() {
			return name;
		}

		public Set<Property> getNested() {
			return nested;
		}				
	}
}

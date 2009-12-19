package com.bradmcevoy.http.webdav;

import com.bradmcevoy.property.BeanPropertySource;
import com.bradmcevoy.property.CustomPropertySource;
import com.bradmcevoy.property.MultiNamespaceCustomPropertySource;
import com.bradmcevoy.property.PropertySource;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author brad
 */
public class PropertySourceUtil {
    public static List<PropertySource> createDefaultSources(ResourceTypeHelper resourceTypeHelper) {
        PropertySource propertySource = new DefaultWebDavPropertySource(resourceTypeHelper);
        CustomPropertySource customPropertySource = new CustomPropertySource();
        MultiNamespaceCustomPropertySource mncps = new MultiNamespaceCustomPropertySource();
        BeanPropertySource beanPropertySource = new BeanPropertySource();
        List<PropertySource> propertySources = Arrays.asList( customPropertySource, propertySource, mncps, beanPropertySource );
        return propertySources;
    }
}

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
    /**
     * Create default extension property sources. These are those additional
     * to the webdav default properties defined on the protocol itself
     *
     * @param resourceTypeHelper
     * @return
     */
    public static List<PropertySource> createDefaultSources(ResourceTypeHelper resourceTypeHelper) {
        CustomPropertySource customPropertySource = new CustomPropertySource();
        MultiNamespaceCustomPropertySource mncps = new MultiNamespaceCustomPropertySource();
        BeanPropertySource beanPropertySource = new BeanPropertySource();
        List<PropertySource> propertySources = Arrays.asList( customPropertySource, mncps, beanPropertySource );
        return propertySources;
    }
}

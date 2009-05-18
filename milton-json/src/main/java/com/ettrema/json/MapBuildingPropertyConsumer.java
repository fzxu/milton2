package com.ettrema.json;

import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropertyConsumer;
import com.bradmcevoy.http.PropertyWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brad
 */
public class MapBuildingPropertyConsumer implements PropertyConsumer{

    Map<String,Object> properties = new HashMap<String, Object>();
    boolean first = true;

    public void consumeProperties( Set<PropertyWriter> knownProperties, Set<PropertyWriter> unknownProperties, String href, PropFindableResource resource ) {
        if( first ) {
            first = false;
            addProps(knownProperties, resource, href, properties);
        } else {
            List<Map<String,Object>> list = (List<Map<String, Object>>) properties.get( "children");
            if( list == null ) {
                list = new ArrayList<Map<String, Object>>();
                properties.put( "children", list);
            }
            Map<String,Object> childProps = new HashMap<String, Object>();
            list.add( childProps);
            addProps( knownProperties, resource, href, childProps);
        }
    }

    private void addProps( Set<PropertyWriter> knownProperties, PropFindableResource resource, String href, Map<String, Object> properties ) {
        for( PropertyWriter pw : knownProperties) {
            String key = pw.fieldName();
            Object value = pw.getValue( resource );
            properties.put( key, value );
        }
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    
}

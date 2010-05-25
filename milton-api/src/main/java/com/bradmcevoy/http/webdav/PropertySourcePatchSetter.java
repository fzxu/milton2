package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.values.ValueAndType;
import com.bradmcevoy.http.values.ValueWriters;
import com.bradmcevoy.http.webdav.PropPatchRequestParser.ParseResult;
import com.bradmcevoy.property.PropertySource;
import com.bradmcevoy.property.PropertySource.PropertyMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PropertySourcePatchSetter implements PropPatchSetter{

    private static final Logger log = LoggerFactory.getLogger( PropertySourcePatchSetter.class );

    private final List<PropertySource> propertySources;

    private final ValueWriters valueWriters;

    public PropertySourcePatchSetter( List<PropertySource> propertySources, ValueWriters valueWriters ) {
        this.propertySources = propertySources;
        this.valueWriters = valueWriters;
    }

    public PropertySourcePatchSetter( List<PropertySource> propertySources) {
        this.propertySources = propertySources;
        this.valueWriters = new ValueWriters();
    }

    /**
     * This returns true for all resources, but it actually depends on the
     * configured property sources.
     *
     * If no property sources support a given resource, a proppatch attempt
     * will return 404's for all properties
     *
     * @param r
     * @return
     */
    public boolean supports( Resource r ) {
        return true;
    }



    public List<PropFindResponse> setProperties( String href, ParseResult parseResult, Resource r ) {
        log.debug( "setProperties: toset: " + parseResult.getFieldsToSet().size());
        Map<QName, ValueAndType> knownProps = new HashMap<QName, ValueAndType>();
        List<QName> unknownProps = new ArrayList<QName>();
        PropertyMetaData meta;

        for( Entry<QName, String> entry : parseResult.getFieldsToSet().entrySet()) {
            log.debug( "setting: " + entry.getKey().getLocalPart());
            boolean found = false;
            for(PropertySource source : propertySources ) {
                meta = source.getPropertyMetaData( entry.getKey(), r );
                if( meta != null && meta.isWritable() ) {
                    log.debug( "setting: " + entry.getKey().getLocalPart() + " to: " + entry.getValue());
                    found = true;
                    Object val = parse(entry.getKey(), entry.getValue(), meta.getValueType());
                    source.setProperty( entry.getKey(), val, r);
                    knownProps.put( entry.getKey(), new ValueAndType( null, meta.getValueType()));
                }
            }
            if( !found ) {
                unknownProps.add( entry.getKey());
            }
        }
        List<PropFindResponse> list = new ArrayList<PropFindResponse>();
        PropFindResponse resp = new PropFindResponse( href, knownProps, unknownProps);
        list.add( resp );
        return list;
    }

    private Object parse( QName key, String value, Class valueType ) {
        return valueWriters.parse(key, valueType, value);
    }

}

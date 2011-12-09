package com.ettrema.http.carddav;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.property.PropertySource;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alex
 */
public class CardDavPropertySource implements PropertySource {

    private static final Logger log = LoggerFactory.getLogger(CardDavPropertySource.class);

    @Override
    public Object getProperty(QName name, Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProperty(QName name, Object value, Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearProperty(QName name, Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<QName> getAllPropertyNames(Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // TODO: implement properties
}
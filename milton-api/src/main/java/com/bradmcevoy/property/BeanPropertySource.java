package com.bradmcevoy.property;

import com.bradmcevoy.http.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class BeanPropertySource implements PropertySource {

    private static final Logger log = LoggerFactory.getLogger( BeanPropertySource.class );
    private static final Object[] NOARGS = new Object[0];

    public Object getProperty( QName name, Resource r ) {
        PropertyDescriptor pd = getPropertyDescriptor( r, name.getLocalPart() );
        if( pd == null )
            throw new IllegalArgumentException( "no prop: " + name.getLocalPart() + " on " + r.getClass() );
        try {
            return pd.getReadMethod().invoke( r, NOARGS );
        } catch( Exception ex ) {
            throw new RuntimeException( name.toString(), ex );
        }
    }

    public void setProperty( QName name, Object value, Resource r ) {
        log.debug( "setProperty: " + name + " = " + value );
        PropertyDescriptor pd = getPropertyDescriptor( r, name.getLocalPart() );
        try {
            pd.getWriteMethod().invoke( r, value );
        } catch( Exception ex ) {
            if( value == null ) {
                log.error( "Exception setting property: " + name.toString() + " to null" );
            } else {
                log.error( "Exception setting property: " + name.toString() + " to value: " + value + " class:" + value.getClass() );
            }
            throw new RuntimeException( name.toString(), ex );
        }
    }

    public PropertyMetaData getPropertyMetaData( QName name, Resource r ) {
        log.debug( "getPropertyMetaData" );
        BeanPropertyResource anno = getAnnotation( r );
        if( anno == null ) {
            log.debug( " no annotation: " + r.getClass());
            return PropertyMetaData.UNKNOWN;
        }
        if( !name.getNamespaceURI().equals( anno.value() ) ) {
            log.debug( "different namespace",anno.value(),name.getNamespaceURI());
            return PropertyMetaData.UNKNOWN;
        }

        PropertyDescriptor pd = getPropertyDescriptor( r, name.getLocalPart() );
        if( pd == null || pd.getReadMethod() == null ) {
            log.debug( "no read method");
            return PropertyMetaData.UNKNOWN;
        } else {
            if( log.isDebugEnabled() ) {
                log.debug( "writable: " + anno.writable() + " - " + ( pd.getWriteMethod() != null ) );
            }
            boolean writable = anno.writable() && ( pd.getWriteMethod() != null );
            if( writable ) {
                return new PropertyMetaData( PropertyAccessibility.WRITABLE, pd.getPropertyType() );
            } else {
                return new PropertyMetaData( PropertyAccessibility.READ_ONLY, pd.getPropertyType() );
            }
        }
    }

    public void clearProperty( QName name, Resource r ) {
        setProperty( name, null, r );
    }

    public List<QName> getAllPropertyNames( Resource r ) {
        BeanPropertyResource anno = getAnnotation( r );
        if( anno == null ) return null;
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors( r );
        List<QName> list = new ArrayList<QName>();
        for( PropertyDescriptor pd : pds ) {
            if( pd.getReadMethod() != null ) {
                list.add( new QName( anno.value(), pd.getName() ) );
            }
        }
        return list;
    }

    private BeanPropertyResource getAnnotation( Resource r ) {
        return r.getClass().getAnnotation( BeanPropertyResource.class );
    }

    private PropertyDescriptor getPropertyDescriptor( Resource r, String name ) {
        try {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( r, name );
            return pd;
        } catch( IllegalAccessException ex ) {
            throw new RuntimeException( ex );
        } catch( InvocationTargetException ex ) {
            throw new RuntimeException( ex );
        } catch( NoSuchMethodException ex ) {
            return null;
        }

    }
}

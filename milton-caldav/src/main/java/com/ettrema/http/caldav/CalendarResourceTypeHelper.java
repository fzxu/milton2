package com.ettrema.http.caldav;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.webdav.ResourceTypeHelper;
import com.ettrema.http.CalendarResource;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class CalendarResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger( CalendarResourceTypeHelper.class );
    private final ResourceTypeHelper wrapped;

    public CalendarResourceTypeHelper( ResourceTypeHelper wrapped ) {
        log.debug( "CalendarResourceTypeHelper constructed :"+wrapped.getClass().getSimpleName() );
        this.wrapped = wrapped;
    }

    public List<QName> getResourceTypes( Resource r ) {
        log.debug( "getResourceTypes" );
        List<QName> list = wrapped.getResourceTypes( r );
        if( r instanceof CalendarResource ) {            
            // http://greenbytes.de/tech/webdav/draft-dusseault-caldav-04.html#new-resources
            QName qn = new QName( CalDavProtocol.CALDAV_NS, "calendar");
            log.debug( "is a calendar, added: " + qn);
            if( list == null ) {
                list = new ArrayList<QName>();
            }
            list.add(qn);
        }
        return list;
    }

    public List<String> getSupportedLevels( Resource r ) {
        log.debug( "getSupportedLevels" );
        List<String> list = wrapped.getSupportedLevels( r );
        if( r instanceof CalendarResource ) {
            list.add( "calendar-access" );
        }
        return list;
    }
}

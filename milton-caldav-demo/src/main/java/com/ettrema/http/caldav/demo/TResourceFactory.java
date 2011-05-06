package com.ettrema.http.caldav.demo;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * For iCal, start off by opening a calendar at
 *
 * http://localhost:8080/users/userA/  - iCal will discover the calendar inside
 * that user.
 *
 * For Mozilla clients (eg thunderbird) connect directory to the calendar url, eg
 *
 * http://localhost:8080/users/userA/calendars/cal1/
 *
 * @author brad
 */
public class TResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResourceFactory.class );
    public static final TFolderResource ROOT = new TFolderResource( (TFolderResource) null, "http://localhost:8080" );

    static TFolderResource users;

    static {
        users = new TFolderResource( ROOT, "users" );        
        addUser(users, "userA");
        addUser(users, "userB");
        addUser(users, "userC");
    }

    private static void addUser(TFolderResource users, String name) {
        TCalDavPrincipal userA = new TCalDavPrincipal(users, name, null, null, null, null);
        TFolderResource calendars = new TFolderResource(userA, "calendars");
        TCalendarResource calendar = new TCalendarResource(calendars, "cal1");
        TScheduleInboxResource scheduleInbox = new TScheduleInboxResource(calendars, "inbox");
        TScheduleOutboxResource scheduleOutbox = new TScheduleOutboxResource(calendars, "outbox");
        userA.setCalendarHome(calendars);
        userA.setScheduleInboxResource(scheduleInbox);
        userA.setScheduleOutboxResource(scheduleOutbox);
    }

    public static TCalDavPrincipal findUser(String name) {
        if( name.contains("@")) {
            name = name.substring(0, name.indexOf("@"));
        }
        System.out.println("find user:" + name);
        for(Resource r : users.children) {
            if( r.getName().equals(name)) {
                return (TCalDavPrincipal) r;
            }
        }
        return null;
    }

    public Resource getResource( String host, String url ) {
        log.debug( "getResource: url: " + url );
        Path path = Path.path( url );
        Resource r = find( path );
        log.debug( "_found: " + r + " for url: " + url + " and path: " + path );
        return r;
    }

    private Resource find( Path path ) {
        if( path.isRoot() ) {
            return ROOT;
        }
        Resource r = find( path.getParent() );
        if( r == null ) return null;
        if( r instanceof TFolderResource ) {
            TFolderResource folder = (TFolderResource) r;
            for( Resource rChild : folder.getChildren() ) {
                Resource r2 = rChild;
                if( r2.getName().equals( path.getName() ) ) {
                    return r2;
                }
            }
        }
        log.debug( "not found: " + path );
        return null;
    }


}

package com.bradmcevoy.http;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockTimeout {

    private static Logger log = LoggerFactory.getLogger(LockTimeout.class);
    private static final String INFINITE = "Infinite";
    
            
    public static LockTimeout parseTimeout(Request request) {
        String sTimeout = request.getTimeoutHeader();
        log.debug("..requested timeout: " + sTimeout);
        return parseTimeout(sTimeout);
    }
    
    public static LockTimeout parseTimeout(String s) {      
        if ( s==null ) return new LockTimeout(null);
        s = s.trim();
        if( s.length() == 0 ) return new LockTimeout(null);

        List<Long> list = new ArrayList<Long>();
        for( String part : s.split(",")) {
            part = part.trim();
            if( part.equalsIgnoreCase(INFINITE)) {
                list.add(Long.MAX_VALUE);
            } else {
                Long seconds = parseTimeoutPart(part);
                if(seconds != null ) {
                    list.add(seconds);
                }
            }
        }
        
        LockTimeout timeout = new LockTimeout(list);
        return timeout;
    }

    static String trim(String s) {
        if( s == null ) return "";
        return s.trim();
    }
    
    static boolean isPresent(String s) {
        return s != null && s.length()>0;
    }

    private static Long parseTimeoutPart(String part) {
        if( part == null || part.length() == 0 ) return null;
        int pos = part.indexOf("-");
        if( pos <= 0 ) {
            return null;
        }
        String s = part.substring(pos+1, part.length());
        long l = 0;
        try {
            l = Long.parseLong(s);
            return l;
        } catch (NumberFormatException numberFormatException) {
            log.error("Number format exception parsing timeout: " + s);
            return null;
        }        
    }
    
    final Long seconds;
    final Long[] otherSeconds;

    private LockTimeout(List<Long> timeouts) {
        if( timeouts == null || timeouts.size()==0 ) {
            this.seconds = null;
            this.otherSeconds = null;
        } else {
            this.seconds = timeouts.get(0);
            timeouts.remove(0);
            otherSeconds = new Long[timeouts.size()];
            timeouts.toArray(otherSeconds);
        }
    }
    
    /**
     * 
     * @return - the preferred timeout. Infinite is represents as Long.MAX_VALUE. Maybe null if no timeout provided
     */
    public Long getSeconds() {
        return seconds;
    }

    /**
     * 
     * @return - an array of less preferred timeouts
     */
    public Long[] getOtherSeconds() {
        return otherSeconds;
    }
    
    
}

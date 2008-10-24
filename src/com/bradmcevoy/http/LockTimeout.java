package com.bradmcevoy.http;

public class LockTimeout {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LockTimeout.class);
    
            
    public static LockTimeout parseTimeout(Request request) {
        String sTimeout = request.getTimeoutHeader();
        log.debug("..requested timeout: " + sTimeout);
        return parseTimeout(sTimeout);
    }
    
    public static LockTimeout parseTimeout(String s) {
        LockTimeout timeout = new LockTimeout(Long.MAX_VALUE);
        return timeout;
    }

            
    
    static String trim(String s) {
        if( s == null ) return "";
        return s.trim();
    }
    
    static boolean isPresent(String s) {
        return s != null && s.length()>0;
    }
    
    
    
    
    
    final Long seconds;

    public LockTimeout(Long seconds) {
        this.seconds = seconds;
    }
}


package com.bradmcevoy.http;

import java.util.Calendar;
import java.util.Date;

class Utils {
    static Date now() {
        return new Date();
    }
    
    static Date addSeconds(Date dt, long seconds) {
        return addSeconds(dt, (int)seconds);
    }
    
    static Date addSeconds(Date dt, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();        
    }
}

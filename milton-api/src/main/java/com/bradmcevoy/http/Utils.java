
package com.bradmcevoy.http;

import com.bradmcevoy.common.Path;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    
    public static Resource findChild(Resource parent, Path path) {
        return _findChild(parent, path.getParts(), 0);
    }
    
    private static Resource _findChild(Resource parent, String[] arr, int i) {        
        if( parent instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource) parent;
            String childName = arr[i];
            
            Resource child = col.child(childName);
            if( child == null ) {
                return null;
            } else {
                if( i < arr.length-1) {
                    return _findChild(child, arr, i+1);
                } else {
                    return child;
                }
            }
        } else {
            return null;
        }
    }

    public static Date now() {
        return new Date();
    }

    public static Date addSeconds(Date dt, long seconds) {
        return addSeconds(dt, (int)seconds);
    }

    public static Date addSeconds(Date dt, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

}


package com.bradmcevoy.http;

import com.bradmcevoy.common.Path;

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
}

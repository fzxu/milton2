package com.bradmcevoy.http;

public interface MoveableResource  extends Resource {
    /** rDest is the destination folder to move to.
     *
     *  name is the new name of the moved resource
     */
    void moveTo(CollectionResource rDest, String name);
    
}

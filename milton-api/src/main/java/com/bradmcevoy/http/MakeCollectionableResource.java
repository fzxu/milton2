package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.ConflictException;

public interface MakeCollectionableResource extends CollectionResource {
    CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException;
    
}

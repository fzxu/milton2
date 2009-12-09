package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.*;

public interface PropertyAccessor<T> {

    String fieldName();

    T getValue( PropFindableResource res );
}

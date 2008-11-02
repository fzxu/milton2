package com.bradmcevoy.http;

import java.util.List;

public interface CollectionResource extends Resource {

    public Resource child(String childName);
    List<? extends Resource> getChildren();
}

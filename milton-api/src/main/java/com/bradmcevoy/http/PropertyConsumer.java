package com.bradmcevoy.http;

import java.util.Set;

public interface PropertyConsumer {

    public void consumeProperties( Set<PropertyWriter> knownProperties, Set<PropertyWriter> unknownProperties, String href, PropFindableResource resource );
}

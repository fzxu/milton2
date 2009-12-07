package com.bradmcevoy.http;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;

/**
 *
 */
public interface PropPatchableResource extends Resource {
    public void setProperties(Fields fields);
}

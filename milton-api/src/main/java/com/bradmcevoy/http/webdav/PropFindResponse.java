package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.values.ValueAndType;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

public class PropFindResponse {

    private final String href;
    private Map<QName, ValueAndType> knownProperties;
    private final List<QName> unknownProperties;

    public PropFindResponse( String href, Map<QName, ValueAndType> knownProperties, List<QName> unknownProperties ) {
        super();
        this.href = href;
        this.knownProperties = knownProperties;
        this.unknownProperties = unknownProperties;
    }

    public String getHref() {
        return href;
    }

    public Map<QName, ValueAndType> getKnownProperties() {
        return knownProperties;
    }

    public List<QName> getUnknownProperties() {
        return unknownProperties;
    }
}

package com.bradmcevoy.http.webdav;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.xml.namespace.QName;

public class PropFindResponse {

    private final String href;
    private LinkedHashMap<QName, Object> knownProperties;
    private final ArrayList<QName> unknownProperties;

    public PropFindResponse( String href, LinkedHashMap<QName, Object> knownProperties, ArrayList<QName> unknownProperties ) {
        super();
        this.href = href;
        this.knownProperties = knownProperties;
        this.unknownProperties = unknownProperties;
    }

    public String getHref() {
        return href;
    }

    public LinkedHashMap<QName, Object> getKnownProperties() {
        return knownProperties;
    }

    public ArrayList<QName> getUnknownProperties() {
        return unknownProperties;
    }
}

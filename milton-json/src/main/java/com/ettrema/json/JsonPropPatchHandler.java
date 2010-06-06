package com.ettrema.json;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.webdav.PropPatchRequestParser.ParseResult;
import com.bradmcevoy.http.webdav.PropPatchSetter;
import com.bradmcevoy.http.webdav.PropPatchableSetter;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class JsonPropPatchHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonPropPatchHandler.class);
    private final PropPatchSetter patchSetter;

    public JsonPropPatchHandler(PropPatchSetter patchSetter) {
        this.patchSetter = patchSetter;
    }

    /**
     * Uses a PropPatchableSetter
     */
    public JsonPropPatchHandler() {
        this.patchSetter = new PropPatchableSetter();
    }

    public void process(Resource wrappedResource, String encodedUrl, Map<String, String> params) {
        Map<QName,String> fields = new HashMap<QName, String>();
        for (String fieldName : params.keySet()) {
            String sFieldValue = params.get(fieldName);
            QName qn;
            if (fieldName.contains(":")) {
                // name is of form uri:local  E.g. MyDav:authorName
                String parts[] = fieldName.split(":");
                String nsUri = parts[0];
                String localName = parts[1];
                qn = new QName(nsUri, localName);
            } else {
                // name is simple form E.g. displayname, default nsUri to DAV
                qn = new QName(WebDavProtocol.NS_DAV, fieldName);
            }
            log.debug("field: " + qn);
            fields.put(qn, sFieldValue);
        }

        ParseResult parseResult = new ParseResult(fields, null);
        patchSetter.setProperties(encodedUrl, parseResult, wrappedResource);
    }

}

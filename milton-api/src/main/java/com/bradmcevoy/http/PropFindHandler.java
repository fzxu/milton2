package com.bradmcevoy.http;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.io.StreamToStream;
import java.io.ByteArrayInputStream;
import java.net.URI;

public class PropFindHandler extends ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger(PropFindHandler.class);
//    private Namespace nsWebDav = new Namespace();
    final Map<String, PropertyWriter> writersMap = new HashMap<String, PropFindHandler.PropertyWriter>();


    {
        add(new ContentLengthPropertyWriter());
        add(new ContentTypePropertyWriter());
        add(new CreationDatePropertyWriter());
        add(new DisplayNamePropertyWriter());
        add(new LastModifiedDatePropertyWriter());
        add(new ResourceTypePropertyWriter());
        add(new EtagPropertyWriter());

        add(new SupportedLockPropertyWriter());
        add(new LockDiscoveryPropertyWriter());

        add(new MSHrefPropertyWriter());
        add(new MSIsCollectionPropertyWriter());
        add(new MSNamePropertyWriter());
    }

    PropFindHandler(HttpManager manager) {
        super(manager);
    }

    private void add(PropertyWriter pw) {
        writersMap.put(pw.fieldName(), pw);
    }

    @Override
    public Request.Method method() {
        return Method.PROPFIND;
    }

    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof PropFindableResource);
    }

    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        PropFindableResource pfr = (PropFindableResource) resource;
        int depth = request.getDepthHeader();
        response.setStatus(Response.Status.SC_MULTI_STATUS);
        response.setContentTypeHeader(Response.XML);
        Set<String> requestedFields;
        try {
            requestedFields = getRequestedFields(request);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
        try {
            String url = request.getAbsoluteUrl();
            String protocol = Utils.getProtocol(url);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XmlWriter writer = new XmlWriter(out);
            writer.writeXMLHeader();
            writer.open("D:multistatus" + generateNamespaceDeclarations());
            writer.newLine();
            appendResponses(writer, pfr, depth, requestedFields, url);
            writer.close("D:multistatus");
            writer.flush();
            log.debug(out.toString());
            response.getOutputStream().write(out.toByteArray());   // note: this can and should write to the outputstream directory. but if it aint broke, dont fix it...
        } catch (IOException ex) {
            log.warn("ioexception sending output", ex);
        }
    }

    protected String generateNamespaceDeclarations() {
//            return " xmlns:" + nsWebDav.abbrev + "=\"" + nsWebDav.url + "\"";
        return " xmlns:D" + "=\"DAV:\"";
    }

    void appendResponses(XmlWriter writer, PropFindableResource resource, int depth, Set<String> requestedFields, String encodedCollectionUrl) {
        try {
            String collectionHref = suffixSlash(encodedCollectionUrl);
            URI parentUri = new URI(collectionHref);
            
            collectionHref = parentUri.toASCIIString();
            log.debug("new collectio href: " + collectionHref);
            sendResponse(writer, resource, requestedFields, collectionHref);

            if (depth > 0 && resource instanceof CollectionResource) {
                CollectionResource col = (CollectionResource) resource;
                List<Resource> list = new ArrayList<Resource>(col.getChildren());
                for (Resource child : list) {
                    if (child instanceof PropFindableResource) {
                        String childHref = collectionHref + Utils.percentEncode(child.getName());
                        sendResponse(writer, (PropFindableResource) child, requestedFields, childHref);
                    }
                }
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    void sendResponse(XmlWriter writer, PropFindableResource resource, Set<String> requestedFields, String href) {
        XmlWriter.Element el = writer.begin("D:response").open();
        final Set<PropertyWriter> unknownProperties = new HashSet<PropertyWriter>();
        final Set<PropertyWriter> knownProperties = new HashSet<PropertyWriter>();
        if (resource instanceof CollectionResource) {
            if (!href.endsWith("/")) {
                href = href + "/";
            }
        }
        writer.writeProperty(null, "D:href", href);

        for (String field : requestedFields) {
            final PropertyWriter pw = writersMap.get(field);
            if (pw != null) {
                knownProperties.add(pw);
            } else {
                unknownProperties.add(new UnknownPropertyWriter(field));
            }
        }

        sendResponseProperties(writer, resource, knownProperties, href, "HTTP/1.1 200 Ok");
        sendResponseProperties(writer, resource, unknownProperties, href, "HTTP/1.1 404 Not Found");

        el.close();
    }

    void sendResponseProperties(XmlWriter writer, PropFindableResource resource, Set<PropertyWriter> properties, String href, String status) {
        if (!properties.isEmpty()) {
            XmlWriter.Element elUnknownPropStat = writer.begin("D:propstat").open();
            XmlWriter.Element elUnknownProp = writer.begin("D:prop").open();
            for (final PropertyWriter pw : properties) {
                appendField(pw, writer, resource, href);
            }
            elUnknownProp.close();
            writer.writeProperty(null, "D:status", status);
            elUnknownPropStat.close();
        }
    }

    private String suffixSlash(String s) {
        if (!s.endsWith("/")) {
            s = s + "/";
        }
        return s;
    }

    private String nameEncode(String s) {
        //return Utils.encode(href, false); // see MIL-31
        return Utils.escapeXml(s);
    //return href.replaceAll("&", "&amp;");  // http://www.ettrema.com:8080/browse/MIL-24
    }

    protected void sendStringProp(XmlWriter writer, String name, String value) {
        String s = value;
        if (s == null) {
            writer.writeProperty(null, name);
        } else {
            writer.writeProperty(null, name, s);
        }
    }

    void sendDateProp(XmlWriter writer, String name, Date date) {
        sendStringProp(writer, name, (date == null ? null : DateUtils.formatDate(date)));
    }

    protected boolean isFolder(PropFindableResource resource) {
        return (resource instanceof CollectionResource);
    }

    private void appendField(PropertyWriter propertyWriter, XmlWriter writer, PropFindableResource resource, String collectionHref) {
        propertyWriter.append(writer, resource, collectionHref);
    }

    private Set<String> getRequestedFields(Request request) throws IOException, SAXException, FileNotFoundException {
        final Set<String> set = new LinkedHashSet<String>();
        InputStream in = request.getInputStream();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamToStream.readTo(in, bout, false, true);
        byte[] arr = bout.toByteArray();
        if (arr.length > 1) {
            ByteArrayInputStream bin = new ByteArrayInputStream(arr);
            XMLReader reader = XMLReaderFactory.createXMLReader();
            PropFindSaxHandler handler = new PropFindSaxHandler();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(bin));
            set.addAll(handler.getAttributes().keySet());
        }

        if (set.size() == 0) {
            set.add("creationdate");
            set.add("getlastmodified");
            set.add("displayname");
            set.add("resourcetype");
            set.add("getcontenttype");
            set.add("getcontentlength");
            set.add("getetag");
        }
        return set;
    }

    interface PropertyWriter {

        String fieldName();

        void append(XmlWriter xmlWriter, PropFindableResource res, String href);
    }

    class DisplayNamePropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), nameEncode(res.getName()));
        }

        public String fieldName() {
            return "displayname";
        }
    }

    class LastModifiedDatePropertyWriter implements PropertyWriter {

        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            //sendDateProp(xmlWriter, "D:" + fieldName(), res.getModifiedDate());
            Date dt = res.getModifiedDate();
            String f;
            if (dt == null) {
                f = "";
            } else {
                f = DateUtils.formatForWebDavModifiedDate(res.getModifiedDate());
            }
            sendStringProp(xmlWriter, "D:" + fieldName(), f);
        //sendDateProp(xmlWriter, "D:" + fieldName(), res.getModifiedDate());
        //sendStringProp(xmlWriter, "D:" + fieldName(), "Thu, 01 Jan 1970 00:00:00 GMT");
        }

        public String fieldName() {
            return "getlastmodified";
        }
    }

    class CreationDatePropertyWriter implements PropertyWriter {

        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            sendDateProp(xmlWriter, "D:" + fieldName(), res.getCreateDate());
        }

        public String fieldName() {
            return "creationdate";
        }
    }

    class ResourceTypePropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource resource, String href) {
            String rt = isFolder(resource) ? "<D:collection/>" : "";
            sendStringProp(writer, "D:resourcetype", rt);
        }

        public String fieldName() {
            return "resourcetype";
        }
    }

    class ContentTypePropertyWriter implements PropertyWriter {

        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            if (res instanceof GetableResource) {
                GetableResource getable = (GetableResource) res;
                sendStringProp(xmlWriter, "D:" + fieldName(), getable.getContentType(null));
            } else {
                sendStringProp(xmlWriter, "D:" + fieldName(), "");
            }
        }

        public String fieldName() {
            return "getcontenttype";
        }
    }

    class ContentLengthPropertyWriter implements PropertyWriter {

        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            if (res instanceof GetableResource) {
                GetableResource getable = (GetableResource) res;
                Long l = getable.getContentLength();
                String s = l == null ? "0" : l.toString();
                sendStringProp(xmlWriter, "D:" + fieldName(), s);
            } else {
                sendStringProp(xmlWriter, "D:" + fieldName(), "");
            }
        }

        public String fieldName() {
            return "getcontentlength";
        }
    }

    class EtagPropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource resource, String href) {
            String etag = resource.getUniqueId();
            if (etag != null) {
                sendStringProp(writer, "D:getetag", etag);
            }
        }

        public String fieldName() {
            return "getetag";
        }
    }

//    <D:supportedlock/><D:lockdiscovery/>

    class LockDiscoveryPropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource resource, String href) {
            if( !(resource instanceof LockableResource) )return ;
            LockableResource lr = (LockableResource) resource;
            LockToken token = lr.getCurrentLock();
            Element lockentry = writer.begin("D:lockdiscovery").open();
            if( token != null ) {
                LockInfo info = token.info;
                writer.begin( "D:lockscope").open().writeText( "<D:" + info.scope.name().toLowerCase() + "/>").close();
                writer.begin( "D:locktype").open().writeText( "<D:" + info.type.name().toLowerCase() + "/>").close();
                writer.begin( "D:depth").open().writeText( "0").close();
                writer.begin( "D:owner").open().writeText( info.owner).close();
                writer.begin( "D:timeout").open().writeText( token.timeout.toString()).close();

                Element elToken = writer.begin( "D:locktoken").open();
                writer.begin( "D:href").open().writeText( "urn:uuid:" + token.tokenId).close();
                writer.begin( "D:lockroot").open().writeText( href).close();
                elToken.close();
            }
            lockentry.close();
        }

        public String fieldName() {
            return "supportedlock";
        }
    }


    class SupportedLockPropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource resource, String href) {
            if( resource instanceof LockableResource ){
                Element lockentry = writer.begin("lockentry").open();
                writer.begin( "lockscope").open().writeText( "<D:exclusive/>").close();
                writer.begin( "locktype").open().writeText( "<D:write/>").close();
                lockentry.close();
            }
        }

        public String fieldName() {
            return "supportedlock";
        }
    }



    // MS specific fields
    class MSNamePropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), nameEncode(res.getName()));
        }

        public String fieldName() {
            return "name";
        }
    }

    class MSHrefPropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), href);
            // sendStringProp(writer, "D:" + fieldName(), href + Utils.percentEncode(res.getName()));
        }

        public String fieldName() {
            return "href";
        }
    }

    class MSIsCollectionPropertyWriter implements PropertyWriter {

        public void append(XmlWriter writer, PropFindableResource res, String href) {
            String s = isFolder(res) ? "true" : "false";
            sendStringProp(writer, "D:" + fieldName(), s);
        }

        public String fieldName() {
            return "iscollection";
        }
    }

    class UnknownPropertyWriter implements PropertyWriter {

        final String name;

        public UnknownPropertyWriter(String name) {
            this.name = name;
        }

        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), null);
        }

        public String fieldName() {
            return name;
        }
    }


}

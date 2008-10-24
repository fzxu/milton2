package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.io.StreamToStream;
import com.bradmcevoy.io.XmlUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXParseException;

/** Copyright 2008 Brad Mcevoy Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable 
 * law or agreed to in writing, software distributed under the License is 
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 * 
 * @author brad
 */
public class PropFindHandler extends ExistingEntityHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PropFindHandler.class);
//    private Namespace nsWebDav = new Namespace();

    final Map<String,PropertyWriter> writersMap = new HashMap<String, PropFindHandler.PropertyWriter>();
    {
        add(new ContentLengthPropertyWriter());
        add(new ContentTypePropertyWriter());
        add(new CreationDatePropertyWriter());
        add(new DisplayNamePropertyWriter());
        add(new LastModifiedDatePropertyWriter());
        add(new ResourceTypePropertyWriter());
        
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
        } catch (SAXParseException ex) {
            throw new RuntimeException(ex);
        } catch (JDOMException ex) {
            throw new RuntimeException(ex);
        }
        try {            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XmlWriter writer = new XmlWriter(out);
            writer.writeXMLHeader();
            writer.open("D:multistatus" + generateNamespaceDeclarations());
            writer.newLine();            
            appendResponses(writer, pfr, depth,requestedFields, request.getAbsolutePath(), request.getHostHeader());
            writer.close("D:multistatus");
            writer.flush();

//            log.info("out: " + out.toString());
            response.getOutputStream().write(out.toByteArray());
            response.close();
        } catch (IOException ex) {
            log.warn("ioexception sending output",ex);
        }
    }

    void appendResponses(XmlWriter writer, PropFindableResource resource, int depth,Set<String> requestedFields, String requestUrl, String host) {
        String collectionHref = suffixSlash("http://" + host + requestUrl);
        log.debug("collectionHref: " + collectionHref);
        sendResponse(writer, resource,requestedFields, collectionHref);
        
        if( resource instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource) resource;
            List<Resource> list = new ArrayList<Resource>(col.getChildren());
            log.debug("appendResponses: " + list.size());
            for (Resource child : list) {
                if (child instanceof PropFindableResource) {
                    sendResponse(writer, (PropFindableResource) child, requestedFields, collectionHref + child.getName());
                } else {
                    log.debug("not adding child: " + child);
                }
            }
        }
    }

    void sendResponse(XmlWriter writer, PropFindableResource resource,Set<String> requestedFields, String href) {
        XmlWriter.Element el = writer.begin("D:response").open();
        
        String href2 = urlEncode(href);
        writer.writeProperty(null, "D:href", href2);
        XmlWriter.Element elPropStat = writer.begin("D:propstat").open();
        XmlWriter.Element elProp = writer.begin("D:prop").open();        
        for( String field : requestedFields ) {
            appendField(field,writer,resource, href);
        }
        elProp.close();
        writer.writeProperty(null, "D:status", "HTTP/1.1 200 OK");
        elPropStat.close();
        el.close();
    }

    private String childHref(PropFindableResource child, String collectionHref) {
        String s = collectionHref + child.getName();
        if (child instanceof CollectionResource) {
            s = suffixSlash(s);
        }
        return s;
    }
    
    private String suffixSlash(String s) {
            if (!s.endsWith("/")) {
                s = s + "/";
            }
            return s;
    }
    
    private String urlEncode(String href) {
        String s = href.replaceAll(" ", "%20");
        return s;
    }

    protected void sendStringProp(XmlWriter writer, String name, String value) {
        String s = value;
        if( s == null ) s = "";
        writer.writeProperty(null, name, s);
    }

    void sendDateProp(XmlWriter writer, String name, Date date) {
        String s;
        if (date == null) {
            s = "";
        } else {
            s = DateUtils.formatDate(date);
        }
        sendStringProp(writer, name, s);
    }

    protected boolean isFolder(PropFindableResource resource) {
        return (resource instanceof CollectionResource);
    }

    private void appendField(String field, XmlWriter writer, PropFindableResource resource, String collectionHref) {
        PropertyWriter pw = writersMap.get(field);
        if( pw == null ) {
//            log.warn("Couldnt find writer for field:" + field);
            pw = new UnknownPropertyWriter(field);
        }
        pw.append(writer, resource, collectionHref);
    }

    private Set<String> getRequestedFields(Request request) throws IOException, SAXParseException, FileNotFoundException, JDOMException {
        final Set<String> set = new LinkedHashSet<String>();
        InputStream in = request.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamToStream.readTo(in, out);
        String xml = new String(out.toByteArray());        
        xml = xml.trim();
        if( xml.length() > 0 ) {
            Document doc = XmlUtils.getDomDocument(xml);

            XmlUtils.process(doc.getRootElement(), new XmlUtils.XmlNodeOperator() {
                @Override
                public void process(Element n) {
                    if( n instanceof Element ) {
                        Element el = (Element) n;
                        Element parent = el.getParentElement();
                        if( parent != null && parent.getName().endsWith("prop")) {
                            String name = el.getName();
                            set.add(name);
                        }
                    }
                }
            });
            
        }
        if( set.size() == 0 ) {
            log.debug("adding default fields");
            set.add("creationdate");
            set.add("getlastmodified");
            set.add("displayname");
            set.add("resourcetype");
            set.add("getcontenttype");
            set.add("getcontentlength");
        }
        return set;
    }
    
    
    
    interface PropertyWriter {
        String fieldName();
        void append(XmlWriter xmlWriter, PropFindableResource res, String href);
    }
    
    class DisplayNamePropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), res.getName());
        }

        @Override
        public String fieldName() {
            return "displayname";
        }        
    }
    
    class LastModifiedDatePropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            //sendDateProp(xmlWriter, "D:" + fieldName(), res.getModifiedDate());
            String f = DateUtils.formatForWebDavModifiedDate(res.getModifiedDate());
            sendStringProp(xmlWriter, "D:" + fieldName(), f);
            //sendDateProp(xmlWriter, "D:" + fieldName(), res.getModifiedDate());
            //sendStringProp(xmlWriter, "D:" + fieldName(), "Thu, 01 Jan 1970 00:00:00 GMT");
        }

        @Override
        public String fieldName() {
            return "getlastmodified";
        }
        
    }

    class CreationDatePropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            sendDateProp(xmlWriter, "D:" + fieldName(), res.getCreateDate());
        }

        @Override
        public String fieldName() {
            return "creationdate";
        }
        
    }

    class ResourceTypePropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter writer, PropFindableResource resource, String href) {
            String rt = isFolder(resource) ? "<D:collection/>" : "";
            sendStringProp(writer, "D:resourcetype", rt);
        }

        @Override
        public String fieldName() {
            return "resourcetype";
        }
        
    }

    class ContentTypePropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            sendStringProp(xmlWriter, "D:"+fieldName(), res.getContentType(null));
        }

        @Override
        public String fieldName() {
            return "getcontenttype";
        }
        
    }

    class ContentLengthPropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter xmlWriter, PropFindableResource res, String href) {
            Long l = res.getContentLength();
            String s = l == null ? "0" : l.toString();
            sendStringProp(xmlWriter, "D:"+fieldName(), s);
        }

        @Override
        public String fieldName() {
            return "getcontentlength";
        }
        
    }

    // MS specific fields
    class MSNamePropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), res.getName());
        }

        @Override
        public String fieldName() {
            return "name";
        }        
    }

    class MSHrefPropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), href);
        }

        @Override
        public String fieldName() {
            return "href";
        }        
    }


    class MSIsCollectionPropertyWriter implements PropertyWriter{
        @Override
        public void append(XmlWriter writer, PropFindableResource res, String href) {
            String s = isFolder(res) ? "true" : "false";
            sendStringProp(writer, "D:" + fieldName(), s);
        }

        @Override
        public String fieldName() {
            return "iscollection";
        }        
    }

    class UnknownPropertyWriter implements PropertyWriter{
        final String name;

        public UnknownPropertyWriter(String name) {
            this.name = name;
        }
        
        
        @Override
        public void append(XmlWriter writer, PropFindableResource res, String href) {
            sendStringProp(writer, "D:" + fieldName(), "");
        }

        @Override
        public String fieldName() {
            return name;
        }        
    }
    
}

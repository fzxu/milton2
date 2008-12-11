package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamToStream;
import com.bradmcevoy.io.WritingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Example request (from ms office)
 *
 * PROPPATCH /Documents/test.docx HTTP/1.1
content-length: 371
cache-control: no-cache
connection: Keep-Alive
host: milton:8080
user-agent: Microsoft-WebDAV-MiniRedir/6.0.6001
pragma: no-cache
translate: f
if: (<opaquelocktoken:900f718e-801c-4152-ae8e-f9395fe45d71>)
content-type: text/xml; charset="utf-8"
<?xml version="1.0" encoding="utf-8" ?>
 * <D:propertyupdate xmlns:D="DAV:" xmlns:Z="urn:schemas-microsoft-com:">
 *  <D:set>
 *  <D:prop>
 *  <Z:Win32LastAccessTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastAccessTime>
 *  <Z:Win32LastModifiedTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastModifiedTime>
 *  <Z:Win32FileAttributes>00000020</Z:Win32FileAttributes>
 * </D:prop>
 * </D:set>
 * </D:propertyupdate>
 *
 *
 * And another example request (from spec)
 *
 *    <?xml version="1.0" encoding="utf-8" ?>
   <D:propertyupdate xmlns:D="DAV:"
   xmlns:Z="http://www.w3.com/standards/z39.50/">
     <D:set>
          <D:prop>
               <Z:authors>
                    <Z:Author>Jim Whitehead</Z:Author>
                    <Z:Author>Roy Fielding</Z:Author>
               </Z:authors>
          </D:prop>
     </D:set>
     <D:remove>
          <D:prop><Z:Copyright-Owner/></D:prop>
     </D:remove>
   </D:propertyupdate>

 *
 *
 * Here is an example response (from the spec)
 *
 *    HTTP/1.1 207 Multi-Status
   Content-Type: text/xml; charset="utf-8"
   Content-Length: xxxx

   <?xml version="1.0" encoding="utf-8" ?>
   <D:multistatus xmlns:D="DAV:"
   xmlns:Z="http://www.w3.com/standards/z39.50">
     <D:response>
          <D:href>http://www.foo.com/bar.html</D:href>
          <D:propstat>
               <D:prop><Z:Authors/></D:prop>
               <D:status>HTTP/1.1 424 Failed Dependency</D:status>
          </D:propstat>
          <D:propstat>
               <D:prop><Z:Copyright-Owner/></D:prop>
               <D:status>HTTP/1.1 409 Conflict</D:status>
          </D:propstat>
          <D:responsedescription> Copyright Owner can not be deleted or
   altered.</D:responsedescription>
     </D:response>
   </D:multistatus>

 *
 *
 * @author brad
 */
public class PropPatchHandler  extends ExistingEntityHandler {
    
    private final static Logger log = LoggerFactory.getLogger(PropPatchHandler.class);
    
    PropPatchHandler(HttpManager manager) {
        super(manager);
    }
    
    public Request.Method method() {
        return Method.PROPPATCH;
    }
    
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof PropPatchableResource);
    }

    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        PropPatchableResource patchable = (PropPatchableResource) resource;
        // todo: check if token header
        try {
            InputStream in = request.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamToStream.readTo(in,out);
            log.debug("PropPatch: " + out.toString());
            Fields fields = parseContent(request);
            if( fields != null ) {
                patchable.setProperties(fields);
            }
            respondOk(request, response, fields);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static Fields parseContent(Request request) throws IOException, SAXException {
        InputStream in = request.getInputStream();
        return parseContent(in);
    }
    static Fields parseContent(InputStream in) throws IOException, SAXException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamToStream.readTo(in, bout,false,true);
        byte[] arr = bout.toByteArray();
        if( arr.length == 0 ) return null;
        return parseContent(arr);
    }

    static Fields parseContent(byte[] arr) throws IOException, SAXException {
        ByteArrayInputStream bin = new ByteArrayInputStream(arr);
        XMLReader reader = XMLReaderFactory.createXMLReader();
        PropPatchSaxHandler handler = new PropPatchSaxHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(bin));
        return handler.getFields();
    }

    public static class Field {
        final String name;

        public Field(String name) {
            this.name = name;
        }

    }

    public static class SetField extends Field {
        final String value;

        public SetField(String name, String value) {
            super(name);
            this.value = value;
        }
    }

    public static class Fields {
        final List<Field> removeFields = new ArrayList<Field>();
        List<SetField> setFields = new ArrayList<PropPatchHandler.SetField>();
    }

    private void respondOk(Request request, Response response, Fields fields) {
        response.setStatus(Response.Status.SC_OK);
        // todo: set multistatus body content
    }
}

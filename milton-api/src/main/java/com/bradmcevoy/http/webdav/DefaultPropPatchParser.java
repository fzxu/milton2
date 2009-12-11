package com.bradmcevoy.http.webdav;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.namespace.QName;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author brad
 */
public class DefaultPropPatchParser implements PropPatchRequestParser {

    public ParseResult getRequestedFields( InputStream in ) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo( in, bout, false, true );
            byte[] arr = bout.toByteArray();
            return parseContent( arr );
        } catch( SAXException ex ) {
            throw new RuntimeException( ex );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        } catch(IOException ex) {
            throw new RuntimeException( ex );
        }
    }

    private ParseResult parseContent( byte[] arr ) throws IOException, SAXException {
        if( arr.length> 0 ) {
            ByteArrayInputStream bin = new ByteArrayInputStream( arr );
            XMLReader reader = XMLReaderFactory.createXMLReader();
            PropPatchSaxHandler handler = new PropPatchSaxHandler();
            reader.setContentHandler( handler );
            reader.parse( new InputSource( bin ) );
            return new ParseResult( handler.getAttributesToSet(), handler.getAttributesToRemove().keySet());
        } else {
            return new ParseResult( new HashMap<QName, String>(), new HashSet<QName>());
        }

    }
}

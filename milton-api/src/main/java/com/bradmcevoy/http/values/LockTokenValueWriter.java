package com.bradmcevoy.http.values;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import java.util.Map;

public class LockTokenValueWriter implements ValueWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return LockToken.class.isAssignableFrom( c );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        LockToken token = (LockToken) val;
        Element lockentry = writer.begin( "D:lockdiscovery" ).open();
        if( token != null ) {
            LockInfo info = token.info;
            writer.begin( "D:lockscope" ).open().writeText( "<D:" + info.scope.name().toLowerCase() + "/>" ).close();
            writer.begin( "D:locktype" ).open().writeText( "<D:" + info.type.name().toLowerCase() + "/>" ).close();
            writer.begin( "D:depth" ).open().writeText( "0" ).close();
            writer.begin( "D:owner" ).open().writeText( info.owner ).close();
            writer.begin( "D:timeout" ).open().writeText( token.timeout.toString() ).close();
            Element elToken = writer.begin( "D:locktoken" ).open();
            writer.begin( "D:href" ).open().writeText( "urn:uuid:" + token.tokenId ).close();
            writer.begin( "D:lockroot" ).open().writeText( href ).close();
            elToken.close();
        }
        lockentry.close();
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}

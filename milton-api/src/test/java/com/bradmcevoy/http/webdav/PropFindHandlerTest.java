package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import static org.easymock.classextension.EasyMock.*; 

/**
 *
 * @author brad
 */
public class PropFindHandlerTest extends TestCase {

    PropFindHandler handler;
    ResourceHandlerHelper resourceHandlerHelper;
    PropFindRequestFieldParser requestFieldParser;
    PropFindPropertyBuilder propertyBuilder;
    WebDavResponseHandler responseHandler;
    Request request;
    Response response;
    PropFindableResource pfr;
    CustomPropertyResource cpr;
    CustomProperty prop;
    String namespace = "http://ns.example.com/boxschema/";
    Date aDate;

    public PropFindHandlerTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        resourceHandlerHelper = createMock( ResourceHandlerHelper.class );
        requestFieldParser = createMock( PropFindRequestFieldParser.class );
        propertyBuilder = createMock( PropFindPropertyBuilder.class );
        responseHandler = createMock( WebDavResponseHandler.class );

        handler = new PropFindHandler(resourceHandlerHelper, requestFieldParser, responseHandler, propertyBuilder );
        request = createMock( Request.class );
        response = createMock( Response.class );
        pfr = createMock( PropFindableResource.class );
        cpr = createMock( CustomPropertyResource.class );
        prop = createMock( CustomProperty.class );
        aDate = SimpleDateFormat.getInstance().parse( "11/4/03 8:14 PM" );
    }

    public void test() {
        // dummy until we add tests
    }

    private void prepareRequest( String xml ) throws IOException {
        expect( request.getDepthHeader() ).andReturn( 1 ).atLeastOnce();
        expect( request.getInputStream() ).andReturn( new ByteArrayInputStream( xml.getBytes() ) );
        expect( request.getAbsoluteUrl() ).andReturn( "http://www.blah.com/test" ).atLeastOnce();
        replay( request );
    }

    private ByteArrayOutputStream prepareResponse() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        expect( response.getOutputStream() ).andReturn( out );
        response.setContentTypeHeader( "text/xml; charset=UTF-8" );
        expectLastCall();
        response.setStatus( Status.SC_MULTI_STATUS );
        expectLastCall();
        replay( response );
        return out;
    }

    void checkValidXml( String xml ) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream( xml.getBytes() );
        org.jdom.input.SAXBuilder b = new SAXBuilder();
        Document doc = b.build( in );
    }
}

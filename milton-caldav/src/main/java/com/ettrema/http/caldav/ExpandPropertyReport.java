package com.ettrema.http.caldav;


import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.webdav.PropFindPropertyBuilder;
import com.bradmcevoy.http.webdav.PropFindRequestFieldParser;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.PropFindXmlGenerator;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.ettrema.http.report.Report;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http://tools.ietf.org/html/rfc3253#section-3.8
 * 
 * Many property values are defined as a DAV:href, or a set of DAV:href
   elements.  The DAV:expand-property report provides a mechanism for
   retrieving in one request the properties from the resources
   identified by those DAV:href elements.  This report not only
   decreases the number of requests required, but also allows the server
   to minimize the number of separate read transactions required on the
   underlying versioning store.

   The DAV:expand-property report SHOULD be supported by all resources
   that support the REPORT method.

   Marshalling:

      The request body MUST be a DAV:expand-property XML element.

      <!ELEMENT expand-property (property*)>
      <!ELEMENT property (property*)>
      <!ATTLIST property name NMTOKEN #REQUIRED>
      name value: a property element type
      <!ATTLIST property namespace NMTOKEN "DAV:">
      namespace value: an XML namespace

      The response body for a successful request MUST be a
      DAV:multistatus XML element.

      multistatus: see RFC 2518, Section 12.9

      The properties reported in the DAV:prop elements of the
      DAV:multistatus element MUST be those identified by the
      DAV:property elements in the DAV:expand-property element.  If
      there are DAV:property elements nested within a DAV:property
      element, then every DAV:href in the value of the corresponding
      property is replaced by a DAV:response element whose DAV:prop
      elements report the values of the properties identified by the
      nested DAV:property elements.  The nested DAV:property elements
      can in turn contain DAV:property elements, so that multiple levels
      of DAV:href expansion can be requested.

      Note that a validating parser MUST be aware that the DAV:expand-
      property report effectively modifies the DTD of every property by
      replacing every occurrence of "href" in the DTD with "href |
      response".
 * 
 *  REPORT /foo.html HTTP/1.1
     Host: www.webdav.org
     Content-Type: text/xml; charset="utf-8"
     Content-Length: xxxx

     <?xml version="1.0" encoding="utf-8" ?>
     <D:expand-property xmlns:D="DAV:">
       <D:property name="version-history">
         <D:property name="version-set">
           <D:property name="creator-displayname"/>
           <D:property name="activity-set"/>
         </D:property>
       </D:property>
     </D:expand-property>

   >>RESPONSE

     HTTP/1.1 207 Multi-Status
     Content-Type: text/xml; charset="utf-8"
     Content-Length: xxxx

     <?xml version="1.0" encoding="utf-8" ?>
     <D:multistatus xmlns:D="DAV:">
       <D:response>
         <D:href>http://www.webdav.org/foo.html</D:href>
         <D:propstat>
           <D:prop>
             <D:version-history>
               <D:response>
                 <D:href>http://repo.webdav.org/his/23</D:href>
                 <D:propstat>
                   <D:prop>
                     <D:version-set>
                       <D:response>
   <D:href>http://repo.webdav.org/his/23/ver/1</D:href>
                         <D:propstat>
                           <D:prop>
   <D:creator-displayname>Fred</D:creator-displayname>



Clemm, et al.               Standards Track                    [Page 30]

 
RFC 3253            Versioning Extensions to WebDAV           March 2002


                             <D:activity-set> <D:href>
                               http://www.webdav.org/ws/dev/sally
                             </D:href> </D:activity-set> </D:prop>
                           <D:status>HTTP/1.1 200 OK</D:status>
                         </D:propstat> </D:response>
                       <D:response>
   <D:href>http://repo.webdav.org/his/23/ver/2</D:href>
                         <D:propstat>
                           <D:prop>
   <D:creator-displayname>Sally</D:creator-displayname>
                             <D:activity-set>
   <D:href>http://repo.webdav.org/act/add-refresh-cmd</D:href>
                             </D:activity-set> </D:prop>
                           <D:status>HTTP/1.1 200 OK</D:status>
                         </D:propstat> </D:response>
                     </D:version-set> </D:prop>
                   <D:status>HTTP/1.1 200 OK</D:status>
                 </D:propstat> </D:response>
             </D:version-history> </D:prop>
           <D:status>HTTP/1.1 200 OK</D:status>
         </D:propstat> </D:response>
     </D:multistatus>

   In this example, the DAV:creator-displayname and DAV:activity-set
   properties of the versions in the DAV:version-set of the
   DAV:version-history of http://www.webdav.org/foo.html are reported.
 *
 * @author bradm
 */
public class ExpandPropertyReport implements Report {


    private static final Logger log = LoggerFactory.getLogger( MultiGetReport.class );
    private final ResourceFactory resourceFactory;
    private final PropFindPropertyBuilder propertyBuilder;
    private final PropFindXmlGenerator xmlGenerator;

    private final Namespace NS_DAV = Namespace.getNamespace( WebDavProtocol.NS_DAV.getPrefix(), WebDavProtocol.NS_DAV.getName() );

    public ExpandPropertyReport( ResourceFactory resourceFactory, PropFindPropertyBuilder propertyBuilder, PropFindXmlGenerator xmlGenerator ) {
        this.resourceFactory = resourceFactory;
        this.propertyBuilder = propertyBuilder;
        this.xmlGenerator = xmlGenerator;
    }


	@Override
    public String process( String host, Resource calendar, Document doc ) {
        log.debug( "process" );
        // The requested properties
        Set<ExpandProperty> props = getProps( doc );
        // The requested resources
        List<String> hrefs = getHrefs( doc );

        PropFindRequestFieldParser.ParseResult parseResult = null; //new PropFindRequestFieldParser.ParseResult( false, props );

        // Generate the response
        Element elMulti = new Element( "multistatus", NS_DAV );
        Document resp = new Document( elMulti );
        List<PropFindResponse> respProps = new ArrayList<PropFindResponse>();

        for( String href : hrefs ) {
            Resource r = resourceFactory.getResource( host, href );
            if( r != null ) {
                if( r instanceof PropFindableResource ) {
                    PropFindableResource pfr = (PropFindableResource) r;
					try {
						respProps.addAll( propertyBuilder.buildProperties( pfr, 0, parseResult, href ) );
					} catch (URISyntaxException ex) {
						throw new RuntimeException("There was an unencoded url requested: " + href, ex);
					}
                } else {
                    // todo
                }
            } else {
                // todo
            }
        }

        String xml = xmlGenerator.generate( respProps );
        return xml;
    }

    private List<String> getHrefs( Document doc ) {
        List<String> list = new ArrayList<String>();
        for( Object o : doc.getRootElement().getChildren() ) {
            if( o instanceof Element ) {
                Element el = (Element) o;
                if( el.getName().equals( "href")) {
                    list.add( el.getText());
                }
            }
        }
        return list;
    }

    private Set<ExpandProperty> getProps( Document doc ) {
        Element elProp = doc.getRootElement().getChild( "prop", NS_DAV );
        if( elProp == null ) {
            throw new RuntimeException( "No prop element" );
        }

        Set<QName> set = new HashSet<QName>();
        for( Object o : elProp.getChildren() ) {
            if( o instanceof Element ) {
                Element el = (Element) o;
                String local = el.getName();
                String ns = el.getNamespaceURI();
                set.add( new QName( ns, local, el.getNamespacePrefix() ) );
            }
        }
		return null;
        //return set;
    }	
	
	@Override
	public String getName() {
		return "expand-property";
	}
	
	/**
	 * An ExpandProperty is a property name with a (possibly empty) list of child
	 * properties
	 * 
	 * To be valid, the name must refer to a property which returns a href
	 */
	class ExpandProperty {
		final QName name;
		final List<ExpandProperty> nestedProps = new ArrayList<ExpandPropertyReport.ExpandProperty>();

		public ExpandProperty(QName name) {
			this.name = name;
		}			
	}
}

package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Utils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PropFindPropertyBuilder {

    private static final Logger log = LoggerFactory.getLogger( PropFindPropertyBuilder.class );

    private final List<WebDavPropertySource> propertySources;

    public PropFindPropertyBuilder( List<WebDavPropertySource> propertySources ) {
        this.propertySources = propertySources;
    }

    public PropFindPropertyBuilder(ResourceTypeHelper resourceTypeHelper) {
        WebDavPropertySource propertySource = new DefaultWebDavPropertySource(resourceTypeHelper);
        this.propertySources = Arrays.asList( propertySource );
    }


    

    public List<PropFindResponse> buildProperties( PropFindableResource pfr, int depth, Set<QName> requestedFields, String url ) {
        List<PropFindResponse> propFindResponses = new ArrayList<PropFindResponse>();
        appendResponses( propFindResponses, pfr, depth, requestedFields, url );
        return propFindResponses;
    }

    private void appendResponses( List<PropFindResponse> responses, PropFindableResource resource, int requestedDepth, Set<QName> requestedFields, String encodedCollectionUrl ) {
        log.debug( "appendresponses: fields size: " + requestedFields.size() );
        try {
            String collectionHref = suffixSlash( encodedCollectionUrl );
            URI parentUri = new URI( collectionHref );

            collectionHref = parentUri.toASCIIString();
            processResource( responses, resource, requestedFields, collectionHref, requestedDepth, 0, collectionHref );

        } catch( URISyntaxException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private void processResource( List<PropFindResponse> responses, PropFindableResource resource, Set<QName> requestedFields, String href, int requestedDepth, int currentDepth, String collectionHref ) {
        collectionHref = suffixSlash( collectionHref );
        final LinkedHashMap<QName,Object> knownProperties = new LinkedHashMap<QName, Object>();
        final ArrayList<QName> unknownProperties = new ArrayList<QName>();

        if( resource instanceof CollectionResource ) {
            if( !href.endsWith( "/" ) ) {
                href = href + "/";
            }
        }
        for( QName field : requestedFields ) {
            if(field.getLocalPart().equals( "href")) {
                knownProperties.put(field, href);
            } else {
                for( WebDavPropertySource source : propertySources ) {
                    boolean found = false;
                    if( source.hasProperty( field, resource ) ) {
                        Object val = source.getProperty( field, resource );
                        knownProperties.put(field, val);
                        found = true;
                        break;
                    }
                    if( !found ) {
                        unknownProperties.add( field );
                    }
                }
            }
        }

        PropFindResponse r = new PropFindResponse( href, knownProperties, unknownProperties );
        responses.add( r );

        if( requestedDepth > currentDepth && resource instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource) resource;
            List<? extends Resource> list = col.getChildren();
            list = new ArrayList<Resource>( list );
            for( Resource child : list ) {
                if( child instanceof PropFindableResource ) {
                    String childHref = collectionHref + Utils.percentEncode( child.getName() );
                    processResource( responses, (PropFindableResource) child, requestedFields, childHref, requestedDepth, currentDepth + 1, href + col.getName() );
                }
            }
        }

    }

    private String suffixSlash( String s ) {
        if( !s.endsWith( "/" ) ) {
            s = s + "/";
        }
        return s;
    }

}

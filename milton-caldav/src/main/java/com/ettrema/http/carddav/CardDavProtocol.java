package com.ettrema.http.carddav;


import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Handler;
import com.bradmcevoy.http.HandlerHelper;
import com.bradmcevoy.http.HttpExtension;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.WellKnownResourceFactory.WellKnownHandler;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.http11.CustomPostHandler;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.values.HrefList;
import com.bradmcevoy.http.values.ValueWriters;
import com.bradmcevoy.http.webdav.PropFindXmlGenerator;
import com.bradmcevoy.http.webdav.PropertyMap;
import com.bradmcevoy.http.webdav.PropertyMap.StandardProperty;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import com.bradmcevoy.property.PropertySource;
import com.ettrema.common.LogUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm
 */
public class CardDavProtocol implements HttpExtension, PropertySource, WellKnownHandler {

    private static final Logger log = LoggerFactory.getLogger(CardDavProtocol.class);
    // Standard caldav properties
    public static final String CARDDAV_NS = "urn:ietf:params:xml:ns:carddav";
    private final Set<Handler> handlers;
    private final PropertyMap propertyMapCalDav;

    public CardDavProtocol(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, WebDavProtocol webDavProtocol) {
        propertyMapCalDav = new PropertyMap(CARDDAV_NS);
		propertyMapCalDav.add(new AddressBookHomeSetProperty());

        handlers = new HashSet<Handler>();

        ValueWriters valueWriters = new ValueWriters();
        PropFindXmlGenerator gen = new PropFindXmlGenerator(valueWriters);
        webDavProtocol.addPropertySource(this);
    }

    @Override
    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet(handlers);
    }

    //TODO: remove debug logging once it's working
    @Override
    public Object getProperty(QName name, Resource r) {
        log.trace("getProperty: {}", name.getLocalPart());
        Object o;
        if (propertyMapCalDav.hasProperty(name)) {
            o = propertyMapCalDav.getProperty(name, r);
        } else {
			o = null;
		}
        log.debug("result : " + o);
        return o;
    }

    @Override
    public void setProperty(QName name, Object value, Resource r) {
        log.trace("setProperty: {}", name.getLocalPart());
        if (propertyMapCalDav.hasProperty(name)) {
            propertyMapCalDav.setProperty(name, r, value);
        }
    }

    @Override
    public PropertyMetaData getPropertyMetaData(QName name, Resource r) {        
		PropertyMetaData md;
        if (propertyMapCalDav.hasProperty(name)) {
            md = propertyMapCalDav.getPropertyMetaData(name, r);
        } else {
			md = null;
		}
		log.trace("getPropertyMetaData: {} - returned: {}", name.getLocalPart(), md);
		return md;
    }

    @Override
    public void clearProperty(QName name, Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<QName> getAllPropertyNames(Resource r) {
        log.trace("getAllPropertyNames");
        List<QName> list = new ArrayList<QName>();
        list.addAll(propertyMapCalDav.getAllPropertyNames(r));
        return list;
    }

    @Override
    public List<CustomPostHandler> getCustomPostHandlers() {
        return null;
    }


    class AddressBookHomeSetProperty implements StandardProperty<HrefList> {

		@Override
        public String fieldName() {
            return "addressbook-home-set";
        }

		@Override
        public HrefList getValue(PropFindableResource res) {
            if (res instanceof CardDavPrincipal) {
                return ((CardDavPrincipal) res).getAddressBookHomeSet();
            } else {
                return null;
            }
        }

		@Override
        public Class<HrefList> getValueClass() {
            return HrefList.class;
        }
    }

    
    @Override
    public String getWellKnownName() {
        return "carddav";
    }

    @Override
    public Resource locateWellKnownResource(Resource host) {
        log.trace("found a carddav well-known resource");
        return new CardDavWellKnownResource(host);
    }

    public class CardDavWellKnownResource implements DigestResource, GetableResource, PropFindableResource {

        private final Resource host;

        public CardDavWellKnownResource(Resource host) {
            this.host = host;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return getWellKnownName();
        }

        @Override
        public Object authenticate(String user, String password) {
            return host.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            // we require a user, so we know where to redirect to
            return (auth != null);
        }

        @Override
        public String getRealm() {
            return host.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return null; // no caching
        }

        @Override
        public String checkRedirect(Request request) {
            log.trace("well-known: checkRedirect");
            Auth auth = request.getAuthorization();
            HrefList addressBookHomes;
            String first;
            if (auth != null && auth.getTag() != null) {
                if (auth.getTag() instanceof CardDavPrincipal) {
                    CardDavPrincipal p = (CardDavPrincipal) auth.getTag();
                    addressBookHomes = p.getAddressBookHomeSet();
                    if (addressBookHomes == null || addressBookHomes.isEmpty()) {
                        log.warn("can't redirect, CalDavPrincipal.getCalendatHomeSet did not return an address. Check implementation class: " + p.getClass());
                        return null;
                    } else {
                        first = addressBookHomes.get(0); // just use first
                        LogUtils.trace(log, "well-known: checkRedirect. redirecting to:", first);
                        return first;
                    }
                } else {
                    log.warn("can't redirect, auth.getTag is not a CardDavPrincipal, is a: " + auth.getTag().getClass() + " To use CARDDAV, the user object returned from authenticate must be a " + CardDavPrincipal.class);
                    return null;
                }
            } else {
                log.trace("can't redirect, no authorisation");
                return null;
            }
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            if (host instanceof DigestResource) {
                DigestResource dr = (DigestResource) host;
                return dr.authenticate(digestRequest);
            } else {
                return null;
            }
        }

        @Override
        public boolean isDigestAllowed() {
            if (host instanceof DigestResource) {
                DigestResource dr = (DigestResource) host;
                return dr.isDigestAllowed();
            } else {
                return false;
            }
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null; // no caching
        }

        @Override
        public String getContentType(String accepts) {
            return null;
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public Date getCreateDate() {
            return null;
        }
    }
}
package com.ettrema.json;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.webdav.PropFindPropertyBuilder;
import com.bradmcevoy.http.webdav.PropPatchSetter;
import com.bradmcevoy.property.PropertySource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class JsonResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(JsonResourceFactory.class);
    private final ResourceFactory wrapped;
    private JsonPropFindHandler propFindHandler;
    private JsonPropPatchHandler propPatchHandler;
    private static final String DAV_FOLDER = "_DAV";

    public JsonResourceFactory(ResourceFactory wrapped, JsonPropFindHandler propFindHandler, JsonPropPatchHandler propPatchHandler) {
        this.wrapped = wrapped;
        this.propFindHandler = propFindHandler;
        this.propPatchHandler = propPatchHandler;
        log.debug("created with: " + propFindHandler.getClass().getCanonicalName());
    }

    public JsonResourceFactory(ResourceFactory wrapped,List<PropertySource> propertySources, PropPatchSetter patchSetter) {
        this.wrapped = wrapped;
        log.debug("using property sources: " + propertySources.size());
        this.propFindHandler = new JsonPropFindHandler(new PropFindPropertyBuilder(propertySources));
        this.propPatchHandler = new JsonPropPatchHandler(patchSetter);
    }



    public Resource getResource(String host, String sPath) {
        log.debug(host + " :: " + sPath);
        Path path = Path.path(sPath);
        Path parent = path.getParent();
        String encodedPath = HttpManager.request().getAbsolutePath();
        if (parent != null && parent.getName() != null && parent.getName().equals(DAV_FOLDER)) {
            Path resourcePath = parent.getParent();
            if (resourcePath != null) {
                String method = path.getName();
                Resource wrappedResource = wrapped.getResource(host, resourcePath.toString());
                if (wrappedResource != null) {
                    return wrapResource(host, wrappedResource, method, encodedPath);
                }
            }
        } else {
            return wrapped.getResource(host, sPath);
        }
        return null;
    }

    private Resource wrapResource(String host, Resource wrappedResource, String method, String href) {
        if (Request.Method.PROPFIND.code.equals(method)) {
            if (wrappedResource instanceof PropFindableResource) {
                if (wrappedResource instanceof DigestResource) {
                    return new DigestPropFindJsonResource((PropFindableResource) wrappedResource, propFindHandler, href);
                } else {
                    return new PropFindJsonResource((PropFindableResource) wrappedResource, propFindHandler, href);
                }
            }
        }
        if (Request.Method.PROPPATCH.code.equals(method)) {
            if (wrappedResource instanceof DigestResource) {
                return new DigestPropPatchJsonResource(wrappedResource, propPatchHandler, href);
            } else {
                return new PropPatchJsonResource(wrappedResource, propPatchHandler, href);
            }
        }
        if (Request.Method.PUT.code.equals(method)) {
            if (wrappedResource instanceof PutableResource) {
                if (wrappedResource instanceof DigestResource) {
                    return new DigestPutJsonResource((PutableResource) wrappedResource, href);
                } else {
                    return new PutJsonResource((PutableResource) wrappedResource, href);
                }
            }
        }
        if (Request.Method.MKCOL.code.equals(method)) {
            if (wrappedResource instanceof MakeCollectionableResource) {
                if (wrappedResource instanceof DigestResource) {
                    return new DigestMkcolJsonResource((MakeCollectionableResource) wrappedResource, href);
                } else {
                    return new MkcolJsonResource((MakeCollectionableResource) wrappedResource, href);
                }
            }
        }
        if (Request.Method.COPY.code.equals(method)) {
            if (wrappedResource instanceof CopyableResource) {
                if (wrappedResource instanceof DigestResource) {
                    return new DigestCopyJsonResource(host, (CopyableResource) wrappedResource, wrapped);
                } else {
                    return new CopyJsonResource(host, (CopyableResource) wrappedResource, wrapped);
                }
            }
        }
        return null;
    }

    public void setPropFindHandler(JsonPropFindHandler propFindHandler) {
        this.propFindHandler = propFindHandler;
    }

    public JsonPropFindHandler getPropFindHandler() {
        return propFindHandler;
    }


    public void setPropPatchHandler(JsonPropPatchHandler propPatchHandler) {
        this.propPatchHandler = propPatchHandler;
    }

    public JsonPropPatchHandler getPropPatchHandler() {
        return propPatchHandler;
    }
}

package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.values.HrefList;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.http.caldav.CalDavPrincipal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author brad
 */
public class TCalDavPrincipal extends TResource implements CalDavPrincipal {

    private HrefPrincipleId principleId;
    private TFolderResource calendarHome;
    private TScheduleInboxResource scheduleInboxResource;
    private String scheduleOutboxUrl;
    private TFolderResource dropBox;

    public TCalDavPrincipal(TFolderResource parent, String name, TFolderResource calendarHome, TScheduleInboxResource scheduleInboxResource, String scheduleOutboxUrl, TFolderResource dropBox) {
        super(parent, name);
        this.principleId = new HrefPrincipleId(getHref());
        this.calendarHome = calendarHome;
        this.scheduleInboxResource = scheduleInboxResource;
        this.scheduleOutboxUrl = scheduleOutboxUrl;
        this.dropBox = dropBox;
    }

    public HrefList getCalendatHomeSet() {
        return HrefList.asList(calendarHome.getHref());
    }

    public HrefList getCalendarUserAddressSet() {
        return HrefList.asList("mailto:" + name + "@localhost");
    }

    public String getScheduleInboxUrl() {
        if (scheduleInboxResource != null) {
            return scheduleInboxResource.getHref();
        } else {
            return null;
        }
    }

    public String getScheduleOutboxUrl() {
        return scheduleOutboxUrl;
    }

    public String getDropBoxUrl() {
        if (dropBox != null) {
            return dropBox.getHref();
        } else {
            return null;
        }
    }

    public PrincipleId getIdenitifer() {
        return principleId;
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        return new TCalDavPrincipal(newParent, name, calendarHome, scheduleInboxResource, scheduleOutboxUrl, dropBox);
    }

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getContentType(String accepts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

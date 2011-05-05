package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.values.HrefList;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.http.caldav.CalDavPrincipal;

/**
 *
 * @author brad
 */
public class TCalDavPrincipal extends TFolderResource implements CalDavPrincipal {

    private HrefPrincipleId principleId;
    private TFolderResource calendarHome;
    private TScheduleInboxResource scheduleInboxResource;
    private TScheduleOutboxResource scheduleOutboxResource;
    private TFolderResource dropBox;

    public TCalDavPrincipal(TFolderResource parent, String name, TFolderResource calendarHome, TScheduleInboxResource scheduleInboxResource, TScheduleOutboxResource scheduleOutboxResource, TFolderResource dropBox) {
        super(parent, name);
        this.principleId = new HrefPrincipleId(getHref());
        this.calendarHome = calendarHome;
        this.scheduleInboxResource = scheduleInboxResource;
        this.scheduleOutboxResource = scheduleOutboxResource;
        this.dropBox = dropBox;
    }

    public TFolderResource getCalendarHome() {
        return calendarHome;
    }

    public void setCalendarHome(TFolderResource calendarHome) {
        this.calendarHome = calendarHome;
    }

    public TScheduleInboxResource getScheduleInboxResource() {
        return scheduleInboxResource;
    }

    public void setScheduleInboxResource(TScheduleInboxResource scheduleInboxResource) {
        this.scheduleInboxResource = scheduleInboxResource;
    }

    public TScheduleOutboxResource getScheduleOutboxResource() {
        return scheduleOutboxResource;
    }

    public void setScheduleOutboxResource(TScheduleOutboxResource scheduleOutboxResource) {
        this.scheduleOutboxResource = scheduleOutboxResource;
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
        if (scheduleOutboxResource != null) {
            return scheduleOutboxResource.getHref();
        } else {
            return null;
        }

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
        return new TCalDavPrincipal(newParent, name, calendarHome, scheduleInboxResource, scheduleOutboxResource, dropBox);
    }


}

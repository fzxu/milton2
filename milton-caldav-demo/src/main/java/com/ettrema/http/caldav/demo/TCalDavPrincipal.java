package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.values.HrefList;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.http.caldav.CalDavPrincipal;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MessageFolder;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author brad
 */
public class TCalDavPrincipal extends TFolderResource implements CalDavPrincipal, Mailbox {

    private HrefPrincipleId principleId;
    private TFolderResource calendarHome;
    private TScheduleInboxResource scheduleInboxResource;
    private TScheduleOutboxResource scheduleOutboxResource;
    private TFolderResource dropBox;

    private final TMailFolder mailInbox;

    public TCalDavPrincipal(TFolderResource parent, String name, TFolderResource calendarHome, TScheduleInboxResource scheduleInboxResource, TScheduleOutboxResource scheduleOutboxResource, TFolderResource dropBox) {
        super(parent, name);
        this.principleId = new HrefPrincipleId(getHref());
        this.calendarHome = calendarHome;
        this.scheduleInboxResource = scheduleInboxResource;
        this.scheduleOutboxResource = scheduleOutboxResource;
        this.dropBox = dropBox;
        mailInbox = new TMailFolder(this, "Inbox");

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

    /**
     * Valiate the password for this user, required for mail support
     * @param password
     * @return
     */
    public boolean authenticate(String password) {
        Object o = authenticate(this.name, password);
        return o != null;
    }

    /**
     * Validate the password hash for this user, required for mail support
     *
     * @param passwordHash
     * @return
     */
    public boolean authenticateMD5(byte[] passwordHash) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageFolder getInbox() {
        return mailInbox;
    }

    public MessageFolder getMailFolder(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmailDisabled() {
        return false;
    }

    public void storeMail(MimeMessage mm) {
        mailInbox.storeMail(mm);
    }


}

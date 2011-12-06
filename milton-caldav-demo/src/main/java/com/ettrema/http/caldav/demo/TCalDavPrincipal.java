package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.values.HrefList;
import com.bradmcevoy.io.StreamUtils;
import com.ettrema.http.CalendarResource;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.http.caldav.CalDavPrincipal;
import com.ettrema.http.caldav.ICalFormatter;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MessageFolder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author brad
 */
public class TCalDavPrincipal extends TFolderResource implements CalDavPrincipal, Mailbox, CalendarResource {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResource.class);
    private HrefPrincipleId principleId;
    private TFolderResource calendarHome;
    private TScheduleInboxResource scheduleInboxResource;
    private TScheduleOutboxResource scheduleOutboxResource;
    private TFolderResource dropBox;
	private String password;

    private final TMailFolder mailInbox;

    public TCalDavPrincipal(TFolderResource parent, String name, String password, TFolderResource calendarHome, TScheduleInboxResource scheduleInboxResource, TScheduleOutboxResource scheduleOutboxResource, TFolderResource dropBox) {
        super(parent, name);
        this.principleId = new HrefPrincipleId(getHref());
        this.calendarHome = calendarHome;
        this.scheduleInboxResource = scheduleInboxResource;
        this.scheduleOutboxResource = scheduleOutboxResource;
        this.dropBox = dropBox;
        mailInbox = new TMailFolder(this, "Inbox");
		this.password = password;
    }

	@Override
	public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
        log.debug("createNew");
        if (contentType.startsWith("text/calendar")) {
            TEvent e = new TEvent(this, newName);
            log.debug("created tevent: " + e.name);
            ICalFormatter formatter = new ICalFormatter();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(inputStream, bout);
            bout.close();
            String data = bout.toString();
            e.setiCalData(data);
            return e;
        } else {
            throw new RuntimeException("eek");
            //log.debug( "creating a normal resource");
            //return super.createNew( newName, inputStream, length, contentType );
        }
	}
	
	
	
	@Override
    public Object authenticate(String requestedUserName, String requestedPassword) {
        log.debug("authentication: " + requestedUserName + " - " + requestedPassword + " = " + password);

        if (!this.getName().equals(requestedUserName)) {
            return null;
        }
        if (password == null) {
            if (requestedPassword == null || requestedPassword.length() == 0) {
                return this;
            } else {
                return null;
            }
        } else {
            if (password.equals(requestedPassword)) {
                return "ok";
            } else {
                return null;
            }
        }
    }
	
	@Override
    public String getPrincipalURL() {
		return getHref();
    }	

	public String getPassword() {
		return password;
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
    

	@Override
    public HrefList getCalendatHomeSet() {
        return HrefList.asList(calendarHome.getHref());
    }

	@Override
    public HrefList getCalendarUserAddressSet() {
		
        return HrefList.asList("mailto:" + name + "@localhost", getHref());
    }

	@Override
    public String getScheduleInboxUrl() {
        if (scheduleInboxResource != null) {
            return scheduleInboxResource.getHref();
        } else {
            return null;
        }
    }

	@Override
    public String getScheduleOutboxUrl() {
        if (scheduleOutboxResource != null) {
            return scheduleOutboxResource.getHref();
        } else {
            return null;
        }

    }

	@Override
    public String getDropBoxUrl() {
        if (dropBox != null) {
            return dropBox.getHref();
        } else {
            return null;
        }
    }

	@Override
    public PrincipleId getIdenitifer() {
        return principleId;
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        return new TCalDavPrincipal(newParent, name, password, calendarHome, scheduleInboxResource, scheduleOutboxResource, dropBox);
    }

    /**
     * Valiate the password for this user, required for mail support
     * @param password
     * @return
     */
	@Override
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
	@Override
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

	@Override
	public String getCalendarDescription() {
		throw new UnsupportedOperationException("Not supported yet.");
	}


}

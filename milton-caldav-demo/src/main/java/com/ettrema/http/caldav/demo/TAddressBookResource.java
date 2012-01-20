package com.ettrema.http.caldav.demo;

import com.bradmcevoy.common.InternationalizedString;
import com.bradmcevoy.http.ReportableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.values.AddressDataTypeList;
import com.ettrema.http.AddressBookResource;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bradmcevoy.http.values.Pair;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author brad
 */
public class TAddressBookResource extends TFolderResource implements AddressBookResource, ReportableResource {

    private static final Logger log = LoggerFactory.getLogger(TCalendarResource.class);

    
    public TAddressBookResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        return new TCalendarResource(newParent, name);
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
		try {
			log.debug("createNew: " + contentType);
	//        if (contentType.startsWith("text/calendar")) {
				TContact e = new TContact(this, newName);
				e.replaceContent(inputStream, length);
				log.debug("created contact: " + e.name);
				return e;
	//        } else {
	//            throw new RuntimeException("eek");
	//            //log.debug( "creating a normal resource");
	//        }
	//        }
		} catch (BadRequestException ex) {
			throw new RuntimeException(ex);
		} catch (ConflictException ex) {
			throw new RuntimeException(ex);
		} catch (NotAuthorizedException ex) {
			throw new RuntimeException(ex);
		}
    }

    @Override
    public InternationalizedString getDescription(){
        return new InternationalizedString("fr-CA", "Adresses de Oliver Daboo");
    }
    
    @Override 
    public void setDescription(InternationalizedString description){
        
    }
    
    @Override
    public Long getMaxResourceSize(){
        return 102400L; 
    }
    
    @Override
    public AddressDataTypeList getSupportedAddressData(){
        AddressDataTypeList supportedAddresses = new AddressDataTypeList();
        supportedAddresses.add(new Pair<String, String>("text/vcard", "3.0"));
        return supportedAddresses;
    }

}

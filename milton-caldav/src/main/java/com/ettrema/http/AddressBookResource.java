package com.ettrema.http;

import com.bradmcevoy.http.PropFindableResource;

/**
 * Interface for collections which can be used as address books for CARDDAV
 *
 * Must implement CalendarCollection as there is a cross-over of property support
 * 
 * @author bradm
 */
public interface AddressBookResource  extends CalendarCollection, PropFindableResource {
	
}

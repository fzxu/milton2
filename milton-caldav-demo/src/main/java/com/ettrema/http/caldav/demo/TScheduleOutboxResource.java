package com.ettrema.http.caldav.demo;

import com.ettrema.http.SchedulingInboxResource;

/**
 *
 * @author brad
 */
public class TScheduleOutboxResource extends TFolderResource implements SchedulingInboxResource {

    public TScheduleOutboxResource(TFolderResource parent, String name) {
        super(parent, name);
    }


}

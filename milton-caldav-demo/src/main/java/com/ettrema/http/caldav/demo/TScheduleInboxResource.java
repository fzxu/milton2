package com.ettrema.http.caldav.demo;

import com.ettrema.http.SchedulingInboxResource;

/**
 *
 * @author brad
 */
public class TScheduleInboxResource extends TFolderResource implements SchedulingInboxResource {

    private String color = "#2952A3";
    
    public TScheduleInboxResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public void setColor(String s) {
        this.color = s;
    }
}

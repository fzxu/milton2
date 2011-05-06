package com.ettrema.http;

import com.bradmcevoy.http.Resource;

/**
 * Represents a resource which can return an ical textual representation
 *
 * @author brad
 */
public interface ICalResource extends Resource {
    /**
     * Generate an iCalendar representation of this resource
     *
     * @return
     */
    String getICalData();


}

package com.ettrema.http;

import com.bradmcevoy.http.Resource;

/**
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

//    /**
//     *
//     *
//     * @param s
//     */
//    void setICalData(String s);
}

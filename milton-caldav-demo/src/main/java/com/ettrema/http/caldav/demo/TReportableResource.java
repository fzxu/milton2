package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.ReportableResource;

/**
 *
 * @author alex
 */
public class TReportableResource extends AbstractResource implements ReportableResource
{
      public TReportableResource( TFolderResource parent, String name ) {
        super(parent, name);
    }
}

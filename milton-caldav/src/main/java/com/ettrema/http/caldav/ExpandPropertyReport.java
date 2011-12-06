package com.ettrema.http.caldav;

import com.bradmcevoy.http.Resource;
import com.ettrema.http.report.Report;
import org.jdom.Document;

/**
 *
 * @author alex
 */
public class ExpandPropertyReport implements Report {

  public String getName()
  {
    return "expand-property";
  }

  public String process(String host, Resource r, Document doc)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}

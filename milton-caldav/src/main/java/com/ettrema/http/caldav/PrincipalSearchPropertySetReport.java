package com.ettrema.http.caldav;

import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.ettrema.http.report.Report;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alex
 */
public class PrincipalSearchPropertySetReport implements Report
{

  private static final Logger log = LoggerFactory.getLogger(PrincipalSearchPropertySetReport.class);

  public String getName()
  {
    return "principal-search-property-set";
  }

  public String process(String host, Resource r, Document doc)
  {
    log.debug("process");
    return "\n<?xml version='1.0' encoding='UTF-8'?>\n"+
    "<principal-search-property-set xmlns='DAV:'>\n"+
      "<principal-search-property>\n"+
        "<prop>\n"+
          "<displayname/>\n"+
        "</prop>\n"+
        "<description xml:lang='en'>Display Name</description>\n"+
      "</principal-search-property>\n"+
      "<principal-search-property>\n"+
        "<prop>\n"+
          "<email-address-set xmlns='http://calendarserver.org/ns/'/>\n"+
        "</prop>\n"+
        "<description xml:lang='en'>Email Addresses</description>\n"+
      "</principal-search-property>\n"+
      "<principal-search-property>\n"+
        "<prop>\n"+
          "<last-name xmlns='http://calendarserver.org/ns/'/>\n"+
        "</prop>\n"+
        "<description xml:lang='en'>Last Name</description>\n"+
      "</principal-search-property>\n"+
      "<principal-search-property>\n"+
        "<prop>\n"+
          "<calendar-user-type xmlns='urn:ietf:params:xml:ns:caldav'/>\n"+
        "</prop>\n"+
        "<description xml:lang='en'>Calendar User Type</description>\n"+
      "</principal-search-property>\n"+
      "<principal-search-property>\n"+
        "<prop>\n"+
          "<first-name xmlns='http://calendarserver.org/ns/'/>\n"+
        "</prop>\n"+
        "<description xml:lang='en'>First Name</description>\n"+
      "</principal-search-property>\n"+
      "<principal-search-property>\n"+
        "<prop>\n"+
          "<calendar-user-address-set xmlns='urn:ietf:params:xml:ns:caldav'/>\n"+
        "</prop>\n"+
        "<description xml:lang='en'>Calendar User Address Set</description>\n"+
      "</principal-search-property>\n"+
    "</principal-search-property-set>";
  }
}

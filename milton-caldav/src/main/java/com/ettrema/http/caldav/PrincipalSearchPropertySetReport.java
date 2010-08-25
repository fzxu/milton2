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
    return "<?xml version='1.0' encoding='UTF-8'?>"+
    "<principal-search-property-set xmlns='DAV:'>"+
      "<principal-search-property>"+
        "<prop>"+
          "<displayname/>"+
        "</prop>"+
        "<description xml:lang='en'>Display Name</description>"+
      "</principal-search-property>"+
      "<principal-search-property>"+
        "<prop>"+
          "<email-address-set xmlns='http://calendarserver.org/ns/'/>"+
        "</prop>"+
        "<description xml:lang='en'>Email Addresses</description>"+
      "</principal-search-property>"+
      "<principal-search-property>"+
        "<prop>"+
          "<last-name xmlns='http://calendarserver.org/ns/'/>"+
        "</prop>"+
        "<description xml:lang='en'>Last Name</description>"+
      "</principal-search-property>"+
      "<principal-search-property>"+
        "<prop>"+
          "<calendar-user-type xmlns='urn:ietf:params:xml:ns:caldav'/>"+
        "</prop>"+
        "<description xml:lang='en'>Calendar User Type</description>"+
      "</principal-search-property>"+
      "<principal-search-property>"+
        "<prop>"+
          "<first-name xmlns='http://calendarserver.org/ns/'/>"+
        "</prop>"+
        "<description xml:lang='en'>First Name</description>"+
      "</principal-search-property>"+
      "<principal-search-property>"+
        "<prop>"+
          "<calendar-user-address-set xmlns='urn:ietf:params:xml:ns:caldav'/>"+
        "</prop>"+
        "<description xml:lang='en'>Calendar User Address Set</description>"+
      "</principal-search-property>"+
    "</principal-search-property-set>";
  }
}

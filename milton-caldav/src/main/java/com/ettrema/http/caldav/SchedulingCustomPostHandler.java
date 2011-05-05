package com.ettrema.http.caldav;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.http11.CustomPostHandler;
import com.ettrema.http.SchedulingOutboxResource;
import com.ettrema.http.SchedulingResponseItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulingCustomPostHandler implements CustomPostHandler {

    private static final Logger log = LoggerFactory.getLogger(SchedulingCustomPostHandler.class);
    private final SchedulingXmlHelper schedulingHelper = new SchedulingXmlHelper();

    public boolean supports(Resource resource, Request request) {
        boolean b = resource instanceof SchedulingOutboxResource && contentTypeIsCalendar(request);
        log.trace("supports: " + b);
        return b;
    }

    public void process(Resource resource, Request request, Response response) {
        log.trace("process");
        try {
            SchedulingOutboxResource outbox = (SchedulingOutboxResource) resource;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(request.getInputStream(), bout);
            String iCalText = bout.toString("UTF-8");
            log.trace(iCalText);
            List<SchedulingResponseItem> respItems = outbox.queryFreeBusy(iCalText);

            String xml = schedulingHelper.generateXml(respItems);

            response.setStatus(Response.Status.SC_OK);
            response.setDateHeader(new Date());
            response.setContentTypeHeader("application/xml; charset=\"utf-8\"");
            response.setContentLengthHeader((long)xml.length());

            PrintWriter pw = new PrintWriter(response.getOutputStream(), true);

            pw.print(xml);
            pw.flush();
            response.close();


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean contentTypeIsCalendar(Request r) {
        String s = r.getContentTypeHeader();
        return "text/calendar".equals(s);
    }
}

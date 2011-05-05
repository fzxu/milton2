package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.http.SchedulingOutboxResource;
import com.ettrema.http.SchedulingResponseItem;
import com.ettrema.http.caldav.ITip;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class TScheduleOutboxResource extends TFolderResource implements SchedulingOutboxResource {

    public TScheduleOutboxResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    public List<SchedulingResponseItem> queryFreeBusy(String iCalText) {
        List<SchedulingResponseItem> respItems = new ArrayList<SchedulingResponseItem>();
        try {
            Reader sr = new StringReader(iCalText);
            LineNumberReader r = new LineNumberReader(sr);
            String organiser = "";
            while (r.ready()) {
                String line = nextLine(r);
                if (line.startsWith("ORGANIXER")) {
                    organiser = line.substring(line.lastIndexOf(":"));
                    System.out.println("got organiser: " + organiser);
                }
                if (line.startsWith("ATTENDEE;")) {
                    SchedulingResponseItem item = processAttendeeLine(line, organiser);
                    respItems.add(item);
                }

            }
            System.out.println("finished query");
            System.out.println("-------------------------------------------");
            return respItems;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private SchedulingResponseItem processAttendeeLine(String line, String organiser) {
        System.out.println("processAttendeeLine: " + line);
        String attendee = line.substring(line.lastIndexOf(":"));
        System.out.println("process user: " + attendee);

        String ical = "";
        ical += "BEGIN:VCALENDAR\n";
        ical += "VERSION:2.0\n";
        ical += "PRODID:-//Example Corp.//CalDAV Server//EN\n";
        ical += "METHOD:REPLY\n";
        ical += "BEGIN:VFREEBUSY\n";
        ical += "UID:4FD3AD926350\n";
        ical += "DTSTAMP:20090602T200733Z\n";
        ical += "DTSTART:20090602T000000Z\n";
        ical += "DTEND:20090604T000000Z\n";
        ical += "ORGANIZER;CN=\"" + organiser + "\":mailto:" + organiser + "\n";  // TODO: should be organiser user
        ical += "ATTENDEE;CN=\"" + attendee + "\":mailto:" + attendee + "\n";
        ical += "FREEBUSY;FBTYPE=BUSY:20090602T110000Z/20090602T120000Z\n";
        ical += "FREEBUSY;FBTYPE=BUSY:20090603T170000Z/20090603T180000Z\n";
        ical += "END:VFREEBUSY\n";
        ical += "END:VCALENDAR\n";

        return new SchedulingResponseItem(attendee, ITip.StatusResponse.RS_SUCCESS_20, ical);
    }

    private String nextLine(LineNumberReader r) throws IOException {
        String s = r.readLine();
        if (s == null) {
            return "";
        }
        System.out.println("line: " + s.length() + " -> " + s);

        int lineNo = r.getLineNumber();
        String nextLine = r.readLine();
        if (nextLine.length() != nextLine.trim().length()) {
            System.out.println("found white space, so concat with preceding line");
            s += nextLine.trim();
            System.out.println("now have: " + s);
        } else {
            System.out.println("go back to line: " + lineNo);
            r.setLineNumber(lineNo); // go back
        }

        return s;
    }
}

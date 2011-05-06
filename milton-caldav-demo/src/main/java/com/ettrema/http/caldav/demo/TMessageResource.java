package com.ettrema.http.caldav.demo;

import com.ettrema.mail.Attachment;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class TMessageResource extends TFolderResource{

    private StandardMessage msg;

    public TMessageResource(TFolderResource parent, String name, StandardMessage sm) {
        super(parent, name);
        this.msg = sm;
        for( Attachment attachment : sm.getAttachments() ) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                IOUtils.copy(attachment.getInputStream(), bout);
                TBinaryResource bRes = new TBinaryResource(this, attachment.getName(), bout.toByteArray(), attachment.getContentType());
                System.out.println("created attachment file: " + bRes.getName());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    @Override
    protected Object clone(TFolderResource newParent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void doBody(PrintWriter pw) {
        pw.println("<table>");
        writeField(pw, "Subject", msg.getSubject());
        writeField(pw, "From", msg.getFrom() );
        writeField(pw, "To", msg.getTo() );        
        pw.println("</table>");
        writeBody(pw, msg.getHtml(), msg.getText() );

        super.doBody(pw);
    }

    private void writeField(PrintWriter pw, String label, String value) {
        pw.println("<tr>");
        pw.println("<td>" + label + "</tr>");
        pw.println("<td>" + value + "</tr>");
        pw.println("</tr>");
    }

    private void writeField(PrintWriter pw, String string, MailboxAddress from) {
        writeField(pw, string, from.toString());
    }

    private void writeField(PrintWriter pw, String string, List<MailboxAddress> to) {
        String s = "";
        for(MailboxAddress mb : to) {
            s += mb.toString() + ", ";
        }
        writeField(pw, name, s);
    }

    private void writeBody(PrintWriter pw, String html, String text) {
        if( html != null ) {
            pw.print(html);
        } else {
            pw.print("<pre>");
            pw.print(text);
            pw.print("</pre>");
        }
    }




}

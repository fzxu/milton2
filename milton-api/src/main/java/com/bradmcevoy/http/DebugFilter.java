package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Header;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamToStream;
import com.bradmcevoy.io.WritingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DebugFilter implements Filter{

    private static final Logger log = LoggerFactory.getLogger(DebugFilter.class);

    private static int counter = 0;

    public void process(FilterChain chain, Request request, Response response) {
        DebugRequest req2 = new DebugRequest(request);
        record(req2);
        chain.process(req2, response);
    }

    private static synchronized void record(DebugRequest req2) {
        counter++;
        FileOutputStream fout = null;
        try {
            File f = new File(System.getProperty("user.home"));
            f = new File(f, counter + ".req");
            fout = new FileOutputStream(f);
            req2.record(fout);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                fout.close();
            } catch (IOException ex) {
            }
        }
    }

    public class DebugRequest extends AbstractRequest {
        final Request r;
        final ByteArrayInputStream content;

        public DebugRequest(Request r) {
            this.r = r;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                StreamToStream.readTo(r.getInputStream(), out);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            this.content = new ByteArrayInputStream(out.toByteArray());
            log.debug(out.toString());
        }

        public Map<String, String> getHeaders() {
            return r.getHeaders();
        }

        @Override
        public String getRequestHeader(Header header) {
            return r.getRequestHeader(header);
        }

        public String getFromAddress() {
            return r.getFromAddress();
        }

        public Method getMethod() {
            return r.getMethod();
        }

        public Auth getAuthorization() {
            return r.getAuthorization();
        }

        public String getAbsoluteUrl() {
            return r.getAbsoluteUrl();
        }

        public InputStream getInputStream() throws IOException {
            return content;
        }

        public void parseRequestParameters(Map<String, String> params, Map<String, FileItem> files) throws RequestParseException {
            r.parseRequestParameters(params, files);
        }

        public void record(OutputStream out) {
            PrintWriter writer = new PrintWriter(out);
            writer.println(getMethod() + " " + getAbsolutePath() + " HTTP/1.1");
            for(Map.Entry<String,String> header : this.getHeaders().entrySet()) {
                writer.println(header.getKey() + ": " + header.getValue());
            }
            writer.flush();
            try {
                StreamToStream.readTo(content, out);
            } catch (ReadingException ex) {
                log.error("",ex);
            } catch (WritingException ex) {
                log.error("",ex);
            }
        }

    }

    public class DebugResponse { //todo
        final Response r;

        public DebugResponse(Response r) {
            this.r = r;
        }


    }
}

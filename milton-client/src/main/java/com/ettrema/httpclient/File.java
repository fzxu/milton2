package com.ettrema.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * @author mcevoyb
 */
public class File extends Resource {

    public final String contentType;
    public final Long contentLength;

    public File(Folder parent, PropFindMethod.Response resp) {
        super(parent, resp);
        this.contentType = resp.contentType;
        this.contentLength = resp.contentLength;
    }

    public File(Folder parent, String name, String contentType, Long contentLength) {
        super(parent, name);
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public void setContent(ByteArrayInputStream in, Long contentLength) throws IOException {
        this.parent.upload(this.name,in, contentLength);
    }

    @Override
    public String toString() {
        return super.toString() + " (content type=" + this.contentType + ")";
    }
}

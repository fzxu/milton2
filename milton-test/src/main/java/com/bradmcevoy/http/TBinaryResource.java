package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class TBinaryResource extends TResource {
    
    byte[] bytes;
    
    public TBinaryResource(TFolderResource parent, String name, byte[] bytes) {
        super(parent,name);
        this.bytes = bytes;
        System.out.println("created resource of size: " + bytes.length);
    }
 
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params) throws IOException {
        System.out.println("writing binary resource:");
        out.write( bytes );
        System.out.println("wrote bytes: " + bytes.length);
    }

    @Override
    public Long getContentLength() {
        return (long)bytes.length;
    }

    
}

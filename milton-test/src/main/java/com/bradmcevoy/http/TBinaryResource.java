package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;

public class TBinaryResource extends TResource {
    
    byte[] bytes;
    
    public TBinaryResource(TFolderResource parent, String name, byte[] bytes) {
        super(parent,name);
        this.bytes = bytes;
    }
 
    public void sendContent(OutputStream out) throws IOException {
        out.write( bytes );
    }    
    
}

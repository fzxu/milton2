package com.bradmcevoy.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class FileDeletingInputStream extends InputStream{

    private static Logger log = LoggerFactory.getLogger(FileDeletingInputStream.class);

    private final File tempFile;
    private InputStream wrapped;

    public FileDeletingInputStream( File tempFile ) throws FileNotFoundException {
        this.tempFile = tempFile;
        wrapped = new FileInputStream( tempFile );
    }

    @Override
    public int read() throws IOException {
        return wrapped.read();
    }

    @Override
    public int read( byte[] b ) throws IOException {
        return wrapped.read( b );
    }

    @Override
    public int read( byte[] b, int off, int len ) throws IOException {
        return wrapped.read( b, off, len );
    }

    @Override
    public synchronized void reset() throws IOException {
        wrapped.reset();
    }

    @Override
    public void close() throws IOException {
        try{
            wrapped.close();
        } finally {
            if(!tempFile.delete()) {
                log.warn("Failde to delete: " + tempFile.getAbsolutePath());
            }
        }
    }
}

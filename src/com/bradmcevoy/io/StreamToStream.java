package com.bradmcevoy.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamToStream {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StreamToStream.class);
    
    private StreamToStream() {
    }

    public static long readTo(File inFile, OutputStream out, boolean closeOut) throws ReadingException, WritingException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(inFile);
            return readTo(in,out);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                log.error("exception closing output stream",ex);
            }
            if( closeOut ) {
                try {
                    out.close();
                } catch (IOException ex) {
                    log.error("exception closing outputstream",ex);
                }
            }
        }        
    }
    
    public static long readTo(InputStream in, File outFile, boolean closeIn) throws ReadingException, WritingException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            return readTo(in,out);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                log.error("exception closing output stream",ex);
            }
            if( closeIn ) {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.error("exception closing inputstream",ex);
                }
            }
        }        
    }
    
    /**
     * Copies data from in to out and DOES NOT close streams
     * 
     * @param in
     * @param out
     * @return
     * @throws com.bradmcevoy.io.ReadingException
     * @throws com.bradmcevoy.io.WritingException
     */
    public static long readTo(InputStream in, OutputStream out) throws ReadingException, WritingException {
        return readTo(in, out, false, false);
    }
    
    /**
     * Reads bytes from the input and writes them, completely, to the output. Closes both streams when
     * finished depending on closeIn and closeOyt
     * 
     * @param in
     * @param out
     * @param closeIn
     * @param closeOut
     * @return
     * @throws com.bradmcevoy.io.ReadingException
     * @throws com.bradmcevoy.io.WritingException
     */
    public static long readTo(InputStream inRaw, OutputStream out, boolean closeIn, boolean closeOut) throws ReadingException, WritingException {
        BufferedInputStream in = new BufferedInputStream(inRaw,1024);
        byte[] buf = new byte[1024];
        int s;
        try{
            try {
                s = in.read(buf);
            } catch (IOException ex) {
                throw new ReadingException(ex);
            }
            long numBytes = 0;
            int cnt=0;
            while( s > 0 ) {
                try {
                    numBytes+=s;
                    cnt+=s;
                    out.write(buf,0,s);                    
//                    if( cnt > 10000 ) {
//                        out.flush();
//                        cnt = 0;
//                    }
                } catch (IOException ex) {
                    throw new WritingException(ex);
                }
                try {
                    s = in.read(buf);
                } catch (IOException ex) {
                    throw new ReadingException(ex);
                }
            }
            try {
                out.flush();
            } catch (IOException ex) {
                throw new WritingException(ex);
            }
            return numBytes;
        } finally {
            if( closeIn ) {
                close(in);
            }
            if( closeOut) {
                close(out);
            }
        }        
    }

    public static void close(OutputStream out) {
        if( out == null ) return ;
        try {
            out.close();
        } catch (IOException ex) {
            log.warn("exception closing output stream",ex);
        }
    }
    
    public static void close(InputStream in) {
        try {
            in.close();
        } catch (IOException ex) {
            log.warn("exception closing inputstream",ex);
        }
    }
}

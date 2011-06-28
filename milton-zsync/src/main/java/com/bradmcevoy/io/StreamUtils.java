package com.bradmcevoy.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamUtils {
    

    
    private StreamUtils() {
    }

    private static void skip(InputStream in, Long start) {
        try {
            in.skip(start);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static long readTo(File inFile, OutputStream out, boolean closeOut)  {
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

            }
            if( closeOut ) {
                try {
                    out.close();
                } catch (IOException ex) {

                }
            }
        }        
    }
    
    public static long readTo(InputStream in, File outFile, boolean closeIn) {
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

            }
            if( closeIn ) {
                try {
                    in.close();
                } catch (IOException ex) {

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
    public static long readTo(InputStream in, OutputStream out)  {
        return readTo(in, out, false, false, null, null);
    }
    
    /**
     * Reads bytes from the input and writes them, completely, to the output. Closes both streams when
     * finished depending on closeIn and closeOyt
     * 
     * @param in
     * @param out
     * @param closeIn
     * @param closeOut
     * @return - number of bytes written
     * @throws com.bradmcevoy.io.ReadingException
     * @throws com.bradmcevoy.io.WritingException
     */
    public static long readTo(InputStream in, OutputStream out, boolean closeIn, boolean closeOut)  {
        return readTo(in, out,closeIn, closeOut, null, null);
            }
    
    private static long readTo(InputStream in, OutputStream out, boolean closeIn, boolean closeOut, Long start, Long finish)  {
        long cnt=0;        
        if( start != null ) {
            skip(in, start);
            cnt = start;
        }
            
        byte[] buf = new byte[1024];
        int s;
        try{
            try {
                s = in.read(buf);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (NullPointerException e) {
                return cnt;
            }
            long numBytes = 0;
            while( s > 0 ) {
                try {
                    numBytes+=s;
                    cnt+=s;
                    out.write(buf,0,s);                    
                    if( cnt > 10000 ) {
                        out.flush();
                        cnt = 0;
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    s = in.read(buf);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                out.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
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
            
        }
    }
    
    public static void close(InputStream in) {
        if( in == null ) return ;
        try {
            in.close();
        } catch (IOException ex) {
            
        }
    }
}

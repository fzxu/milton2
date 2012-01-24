/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gnostech.webdav.logging;

import java.util.logging.*;
import java.io.*;
/**
 *
 * @author ogutierrez
 */
public class DavLogger {
    
public static Logger logger;

static {
    try {
      File directory = new File("c:\\policy\\");
      boolean exists = directory.exists();
      if(!exists){
          directory.mkdir();
      }
      
    
      File subdirectory = new File("c:\\policy\\logs");
      boolean sexists = subdirectory.exists();
      if(!sexists){
          subdirectory.mkdir();
      }
      
      
      
      boolean append = true;
      int limit = 10000000; // 10 Mb
      int numLogFiles = 5;
      FileHandler fh = new FileHandler("c:\\policy\\logs\\AccessCC.log", append);
      fh.setFormatter(new Formatter() {
         public String format(LogRecord rec) {
            StringBuffer buf = new StringBuffer(1000);
            buf.append(new java.util.Date());
            buf.append(' ');
            buf.append(rec.getLevel());
            buf.append(' ');
            buf.append(formatMessage(rec));
            buf.append('\n');
            return buf.toString();
            }
          });
      logger = Logger.getLogger("DavLogger");
      logger.addHandler(fh);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
}
    
}

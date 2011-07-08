/* HttpConnection.java

   HttpConnection: HTTP connection and parsing methods for Range requests
   Copyright (C) 2011 Tomáš Hlavnička <hlavntom@fel.cvut.cz>

   This file is a part of Jazsync.

   Jazsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at
   your option) any later version.

   Jazsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Jazsync; if not, write to the

      Free Software Foundation, Inc.,
      59 Temple Place, Suite 330,
      Boston, MA  02111-1307
      USA
 */

package com.ettrema.zsync;

import com.bradmcevoy.http.Range;
import org.base64coder.Base64Coder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HTTP connection with Range support class
 * @author Tomáš Hlavnička
 */
public class HttpConnection {
    private String rangeRequest;
    private String username;
    private String password;
    private HttpURLConnection connection;
    private URL address;
    private String boundary;
    private byte[] boundaryBytes;
    private long contLen;

    private long allData = 0;

    public HttpConnection(String url) {
        try {
            address = new URL(url);
        } catch (MalformedURLException e) {
            failed(url);
        }
    }

    /**
     * Returns HTTP status code of response
     * @return HTTP code
     */
    private int getHttpStatusCode(){
        int code=0;
        try {
            code = connection.getResponseCode();
        } catch (IOException e) {

            failed(address.toString());
        }
        return code;
    }

    /**
     * Opens HTTP connection
     */
    public void openConnection(){
        try {
            connection = (HttpURLConnection)address.openConnection();
        } catch (MalformedURLException e) {
            failed(address.toString());
        } catch (IOException e) {
            failed(address.toString());
        }
    }

    /**
     * Sends HTTP GET request
     */
    public void sendRequest(){
        try{
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "jazsync");
            if(username!=null && password!=null){
                String encoding = Base64Coder.encodeLines((username+":"+password).getBytes());
                connection.setRequestProperty("Authorization", 
                        "Basic "+encoding.substring(0, encoding.length()-1));
            }
            if (rangeRequest!=null){
                connection.setRequestProperty("Range", "bytes="+rangeRequest);
            }
        } catch (IOException e) {
           failed(address.toString());
        }
    }

    /**
     * Sets ranges for http request
     * @param ranges ArrayList of DataRange objects containing block ranges
     */
    public void setRangesRequest(ArrayList<Range> ranges){
        StringBuilder sb = new StringBuilder();
        for(Range d : ranges){
            sb.append(d.getRange()).append(",");
        }
        sb.delete(sb.length()-1,sb.length());
        rangeRequest=sb.toString();
    }

    /**
     * Sets login for authentication for HTTP request
     * @param username Authentication username
     * @param password Authentication password
     */
    public void setAuthentication(String username, String password){
        this.username=username;
        this.password=password;
    }

    /**
     * Comparing to find boundaries in byte stream
     * @param src Byte array with data
     * @param srcOff Offset in byte array with data
     * @param bound Byte array with boundary value
     * @return
     */
    private boolean boundaryCompare(byte[] src, int srcOff, byte[] bound){
        int j = srcOff;
        for(int i=0; i<bound.length; i++){
            if(src[j]!=bound[i]){
                return false;
            }
            j++;
        }
        return true;
    }

    /**
     * Method that looks through byte array and figure out where boundaries are
     * and where relevant data starts
     * @param src Array where we are trying to find data boundaries
     * @param i Offset of src array where we are starting the look up
     * @return Offset where the data starts
     */
    private int dataBegin(byte[] src, int i){
        int newLine=0;
        int offset=i;
        for(;offset<src.length;offset++){
            if(src[offset]==13 && src[offset+1]==10){
                newLine++;
                if(newLine==4){
                    offset+=2;
                    break;
                }
            }
        }
        return offset;
    }

    /**
     * Downloads data block or ranges of blocks
     * @param blockLength Length of a data block that we are downloading
     * @return Content of body in byte array
     */
    public byte[] getResponseBody(int blockLength){
        byte[] bytes = new byte[(int)contLen];
        try {
            InputStream in = connection.getInputStream();
            for (int i = 0; i < bytes.length; i++) {
                bytes[i]=(byte)in.read();
            }
        } catch (IOException e) {
            failed(address.toString());
        }
        allData+=contLen;

        //pripad, kdy data obsahuji hranice (code 206 - partial content)
        if(boundary!=null){
            int range=0;
            byte[] rangeBytes = new byte[(int)contLen+blockLength];
                for(int i = 0; i <bytes.length;i++){
                    //jestlize jsou ve streamu "--"
                    if(bytes[i]==45 && bytes[i+1]==45){
                        //zkontrolujeme jestli za "--" je boundary hodnota
                        if(boundaryCompare(bytes, i+2, boundaryBytes)){
                            i+=2+boundaryBytes.length; //presuneme se za boundary
                            /* pokud je za boundary dalsi "--" jde o konec streamu
                             * v opacnem pripade si data zkopirujeme
                             */
                            if(bytes[i]!=45 && bytes[i+1]!=45){
                                try{
                                    System.arraycopy(bytes, dataBegin(bytes,i), rangeBytes, range, blockLength);
                                } catch (ArrayIndexOutOfBoundsException e){
                                    /*osetreni vyjimky v pripade kopirovani kratsiho bloku dat */
                                    System.arraycopy(bytes, dataBegin(bytes,i), rangeBytes, range, bytes.length-dataBegin(bytes,i));
                                }
                                range+=blockLength;
                            }
                        }
                    }
                }
            byte[] ranges = new byte[range];
            System.arraycopy(rangeBytes, 0, ranges, 0, ranges.length);
            return ranges;
        }
        
        return bytes;
    }

    /**
     * Downloads whole file
     * @param length Length of the file
     * @param filename Name of the downloaded and saved file
     */
    public void getFile(Long length, String filename){
        try {
            FileOutputStream fos=new FileOutputStream(filename, true);
            InputStream in = connection.getInputStream();
            for(int i=0;i<length;i++){
                fos.write((byte)in.read());
            }
        } catch (IOException e) {
            failed(address.toString());
        }
    }

    /**
     * Returns http response header and looks up for a boundary and length keys,
     * saving their values into the variables
     * @return Returns header in String format
     */
    public String getResponseHeader(){
        String header="";
        Map responseHeader = connection.getHeaderFields();

            for (Iterator iterator = responseHeader.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                if(key!=null) {
                    header+=key + " = ";
                }
                List values = (List) responseHeader.get(key);
                for (int i = 0; i < values.size(); i++) {
                    Object o = values.get(i);
                    header+=o.toString();
                    parseBoundary(key,o.toString());
                    parseLength(key,o.toString());
                }
                header+="\n";
            }
        allData+=header.length();
        return header;
    }

    /**
     * Parse the length of content send in body
     * @param key Key name of header line
     * @param values Values of key header line
     */
    private void parseLength(String key, String values){
        if(key!=null && key.equals("Content-Length")==true){
            contLen=Integer.valueOf(values);
        }
    }

    /**
     * Gets boundary sequence from response header for identificating the range
     * boundaries
     * @param key Key name of header line
     * @param values Values of key header line
     */
    private void parseBoundary(String key, String values){
        if(getHttpStatusCode()==206 && key!=null && key.equals("Content-Type")==true){
            int index=values.indexOf("boundary");
            if(index!=-1){
                boundary=values.substring(index+"boundary=".length());
                boundaryBytes=boundary.getBytes();
            }
        }
    }

    /**
     * Closes HTTP connection
     */
    public void closeConnection(){
        connection.disconnect();
    }

    /**
     * Prints out warning message, if connecting to <code>url</code> fails
     * @param url URL (in String format) that we are trying to contact
     */
    private void failed(String url){
        System.out.println("Failed on url "+url);
        System.out.println("Could not read file from URL "+url);
        System.exit(1);
    }

    public long getAllTransferedDataLength(){
        return allData;
    }

}

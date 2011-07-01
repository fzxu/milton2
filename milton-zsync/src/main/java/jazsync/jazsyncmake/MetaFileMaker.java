/* MetaFileMaker.java

   MetaFileMaker: Metafile making class (jazsyncmake)
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

package jazsync.jazsyncmake;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import java.util.ArrayList;
import java.util.List;

import jazsync.jazsync.Rsum;

import org.jarsync.ChecksumPair;
import org.jarsync.Configuration;
import org.jarsync.Generator;
import org.jarsync.JarsyncProvider;

/**
 * Metafile making class
 * @author Tomáš Hlavnička
 */
public class MetaFileMaker {
    
    /** Default length of strong checksum (MD4) */
    private final int STRONG_SUM_LENGTH=16;


    private String url;
    private int blocksize;
    private File file;
    

    private String header;

    /****************************************/
    /* Hash-lengths and number of sequence matches */
    /* index 0 - seq_matches
     * index 1 - weakSum length
     * index 2 - strongSum length
     */

    private int[] hashLengths = new int[3];
    /****************************************/
    /** File length */
    private long fileLength;
    private boolean useBlockDefault=true;
    
    
    public MetaFileMaker(String url, int blocksize, File file) {
        Security.addProvider(new JarsyncProvider());
        hashLengths[2]=STRONG_SUM_LENGTH;

		this.url = url;
		this.blocksize = blocksize;
		
		this.file = file;
    }
	
	public File make() {

		fileLength=file.length();

		//vypocet optimalniho blocksize podle velikosti souboru
		if(useBlockDefault){
			computeBlockSize();
		}
		File outputFile = new File(file.getName() + ".zsynch");

        /**
         * zde provedeme analyzu souboru a podle toho urcime velikost hash length
         * a pocet navazujicich bloku
         */
        analyzeFile();
        //creating header and saving it into the created metafile
        HeaderMaker hm=new HeaderMaker(file,url,blocksize,hashLengths);
        header=hm.getFullHeader();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
            header.replaceAll("\n", System.getProperty("line.separator"));
            out.write(header);
            out.close();
        } catch (IOException e){
            System.out.println("Can't create .zsync metafile, check your permissions");
            System.exit(1);
        }

        //appending block checksums into the metafile
        try {
            FileOutputStream fos=new FileOutputStream(outputFile, true);
            Configuration config = new Configuration();
            config.strongSum = MessageDigest.getInstance("MD4");
            config.weakSum = new Rsum();
            config.blockLength = blocksize;
            config.strongSumLength = hashLengths[2];
            Generator gen = new Generator(config);
            List<ChecksumPair> list = new ArrayList<ChecksumPair>((int)Math.ceil((double)file.length() / (double)blocksize));
            list = gen.generateSums(file);
            for(ChecksumPair p : list){
                fos.write(intToBytes(p.getWeak(),hashLengths[1]));
                fos.write(p.getStrong());
            }
			return outputFile;
        } catch (IOException ioe){
			throw new RuntimeException(ioe);
        } catch (NoSuchAlgorithmException nae){
			throw new RuntimeException(nae);
        }
		
	}

    /**
     * File analysis, computing lengths of weak and strong checksums and 
     * sequence matches, storing the values into the array for easier handle
     */
    private void analyzeFile(){
        hashLengths[0] = fileLength > blocksize ? 2 : 1;
        hashLengths[1] = (int) Math.ceil(((Math.log(fileLength)
                + Math.log(blocksize)) / Math.log(2) - 8.6) / 8);

        if (hashLengths[1] > 4) hashLengths[1] = 4;
        if (hashLengths[1] < 2) hashLengths[1] = 2;
        hashLengths[2] = (int) Math.ceil(
                (20 + (Math.log(fileLength) + Math.log(1 + fileLength / blocksize)) / Math.log(2))
                / hashLengths[0] / 8);

        int strongSumLength2 =
            (int) ((7.9 + (20 + Math.log(1 + fileLength / blocksize) / Math.log(2))) / 8);
        if (hashLengths[2] < strongSumLength2)
            hashLengths[2] = strongSumLength2;
    }

    /**
     * Converting integer weakSum into byte array that zsync can read
     * (htons byte order)
     * @param number weakSum in integer form
     * @return converted to byte array compatible with zsync (htons byte order)
     */
    private byte[] intToBytes(int number, int rsum_bytes){
        byte[] rsum = new byte[rsum_bytes];
        switch (rsum_bytes){
            case 2:
                rsum = new byte[]{(byte)( number >> 24),        //[0]
                                  (byte)((number << 8) >> 24)}; //[1]
                break;
            case 3:
                rsum = new byte[]{(byte)((number << 24) >> 24), //[2]
                                  (byte)( number >> 24),        //[0]
                                  (byte)((number << 8) >> 24)}; //[1]
                break;
            case 4:
                rsum = new byte[]{(byte)((number << 16) >> 24), //[2]
                                  (byte)((number << 24) >> 24), //[3]
                                  (byte)( number >> 24),        //[0]
                                  (byte)((number << 8) >> 24)}; //[1]
                break;
        }
        return rsum;
    }

    /**
     * Calculates optimal blocksize for a file
     */
    private void computeBlockSize(){
        int[][] array = new int[10][2];
        array[0][0]=2048;
        array[0][1]=2048;
        for(int i=1;i<array.length;i++){
            array[i][0]=array[i-1][0]*2;
            array[i][1]=array[i][0];
        }
        //zarucime, ze se soubor rozdeli priblize na 50000 bloku
        long constant = fileLength/50000;
        for(int i=0;i<array.length;i++){
                array[i][0]=(int) Math.abs(array[i][0]-constant);
        }
        int min = array[0][0];
        for(int i=0;i<array.length;i++){
            if(array[i][0] < min){
                min = array[i][0];
            }
        }
        for(int i=0;i<array.length;i++){
            if(array[i][0]==min){
                blocksize=array[i][1];
            }
        }
    }
}

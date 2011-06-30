package jazsync.jazsync;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 *
 * @author brad
 */
public class Downloader {

	private String boundary = ""; // TODO: get from header
	
    /**
     * Downloads data block or ranges of blocks
     * @param blockLength Length of a data block that we are downloading
     * @return Content of body in byte array
     */
    private byte[] getResponseBody(int blockLength, InputStream in, long contLen){
		byte[] boundaryBytes = boundary.getBytes();
		long allData = 0;
        byte[] bytes = new byte[(int)contLen];
        try {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i]=(byte)in.read();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
     * Gets boundary sequence from response header for identificating the range
     * boundaries
     * @param key Key name of header line
     * @param values Values of key header line
     */
    private void parseBoundary(String key, String values){
//        if(getHttpStatusCode()==206 && key!=null && key.equals("Content-Type")==true){
//            int index=values.indexOf("boundary");
//            if(index!=-1){
//                boundary=values.substring(index+"boundary=".length());
//                boundaryBytes=boundary.getBytes();
//            }
//        }
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

	public byte[] getRanges(int blockLength, ArrayList<DataRange> rangeList) {
		// TODO: get the url from somewhere and GET the specified ranges
		return null;
	}
}

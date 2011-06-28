package jazsync.jazsync;

import java.io.InputStream;

/**
 *
 * @author HP
 */
public class HeaderService {

    
    
    public void writeHeaders(Headers headers, Transport transport) {
        transport.writeHeaderInt("Blocksize", headers.getBlockSize());
        
    }
    
    public Headers readHeaders(Transport transport) {
        return null;
    }
    
//	private boolean parseHeader(String s) {
//		String subs;
//		int colonIndex;
//		if (s.equals("")) {
//			//timto prazdnym radkem skoncil header, muzeme prestat cist
//			return true;
//		}
//		colonIndex = s.indexOf(":");
//		subs = s.substring(0, colonIndex);
//		if (subs.equalsIgnoreCase("Blocksize")) {
//			mf_blocksize = Integer.parseInt(s.substring(colonIndex + 2));
//		} else if (subs.equalsIgnoreCase("Length")) {
//			mf_length = Long.parseLong(s.substring(colonIndex + 2));
//		} else if (subs.equalsIgnoreCase("Hash-Lengths")) {
//			int comma = s.indexOf(",");
//			mf_seq_num = Integer.parseInt(s.substring((colonIndex + 2), comma));
//			int nextComma = s.indexOf(",", comma + 1);
//			mf_rsum_bytes = Integer.parseInt(s.substring(comma + 1, nextComma));
//			mf_checksum_bytes = Integer.parseInt(s.substring(nextComma + 1));
//			//zkontrolujeme validni hash-lengths
//			if ((mf_seq_num < 1 || mf_seq_num > 2)
//					|| (mf_rsum_bytes < 1 || mf_rsum_bytes > 4)
//					|| (mf_checksum_bytes < 3 || mf_checksum_bytes > 16)) {
//				System.out.println("Nonsensical hash lengths line " + s.substring(colonIndex + 2));
//				System.exit(1);
//			}
//
//		} else if (subs.equalsIgnoreCase("URL")) {
//			mf_url = s.substring(colonIndex + 2);
//		} else if (subs.equalsIgnoreCase("Z-URL")) {
//			//not implemented yet
//		} else if (subs.equalsIgnoreCase("SHA-1")) {
//			mf_sha1 = s.substring(colonIndex + 2);
//		} else if (subs.equalsIgnoreCase("Z-Map2")) {
//			//not implemented yet
//		}
//		return false;
//	}
    
    
    public class Headers {
	private int blocksize;
	private long length;
	private int seq_num;
	private int rsum_bytes;
	private int checksum_bytes;
	
	private String sha1;
        int mf_checksum_bytes;
        int mf_rsum_bytes;
        
        public int getBlockSize() {
            return 300;
        }
        
        public int getLength() {
            return -1;
        }
    }
}

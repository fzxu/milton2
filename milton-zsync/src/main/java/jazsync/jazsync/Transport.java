package jazsync.jazsync;

/**
 *
 * @author HP
 */
public interface Transport {
    void writeHeader(String name, String value);
    
    String readHeader(String name);

    public void writeHeaderInt(String string, int blockSize);
}

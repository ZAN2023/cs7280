public class DataFile {
    private PFSFile pfsFile;
    private Map<Integer, Integer> keyBlockMap;

    public DataFile(PFSFile pfsFile) {
        this.pfsFile = pfsFile;
        this.keyBlockMap = new HashMap<>();
    }

    public void insert(int key, byte[] value) {
        // Insert key-value pair
    }

    public byte[] get(int key) {
        // Get value by key
    }

    public void delete(int key) {
        // Delete key-value pair
    }
}
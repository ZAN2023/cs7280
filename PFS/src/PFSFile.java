public class PFSFile {
    private String fileName;
    private byte[] data;
    private List<FCB> fcbList;
    private List<Integer> freeBlocks;

    public PFSFile(String fileName) {
        this.fileName = fileName;
        this.data = new byte[1024 * 1024]; // 1MB
        this.fcbList = new ArrayList<>();
        this.freeBlocks = new ArrayList<>();
        // Initialize free blocks
        for (int i = 0; i < data.length / 256; i++) {
            freeBlocks.add(i);
        }
    }

    public void writeBlock(int blockNumber, byte[] blockData) {
        // Write block data to file
    }

    public byte[] readBlock(int blockNumber) {
        // Read block data from file
    }

    public void addFCB(FCB fcb) {
        fcbList.add(fcb);
    }

    public FCB getFCB(String fileName) {
        // Get FCB by file name
    }

    public int allocateBlock() {
        // Allocate a free block
    }

    public void freeBlock(int blockNumber) {
        // Free a block
    }
}
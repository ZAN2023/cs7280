import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class BlockHandler {
    private RandomAccessFile file;
    private FCBHandler fcbHandler;
    private MetadataHandler metadataHandler;
    private BTreeIndex indexTree;
    private List<FCB> fcbList;
    private int usedBlocks;
    private Block currentBlock;
//    private BitSet bitmap;

    public BlockHandler(RandomAccessFile file, FCBHandler fcbHandler, MetadataHandler metadataHandler, BTreeIndex indexTree) {
        this.file = file;
        this.fcbHandler = fcbHandler;
        this.metadataHandler = metadataHandler;
        this.indexTree = indexTree;
        this.fcbList = new ArrayList<>();
        this.usedBlocks = 0;
        this.currentBlock = null;
    }

    public void write(byte[] data) throws IOException {
        int remainingData = data.length;
        int offset = 0;

        while (remainingData > 0) {
            if (currentBlock == null || currentBlock.isFull()) {
                currentBlock = allocateNewBlock();
            }

            int writeSize = Math.min(remainingData, currentBlock.remainingSpace());
            currentBlock.write(data, offset, writeSize);
            remainingData -= writeSize;
            offset += writeSize;

            if (currentBlock.isFull()) {
                writeBlockToFile(currentBlock);
            }
        }
    }

    private Block allocateNewBlock() throws IOException {
        int blockIndex = findFreeBlock();
        if (blockIndex == -1) {
            blockIndex = usedBlocks;
            usedBlocks++;
        }
        return new Block(blockIndex);
    }

    private int findFreeBlock() throws IOException {
        // Find a free block from the free block list in metadata
        List<Integer> freeBlockList = metadataHandler.readFreeBlockListMetadata(file);
        if (!freeBlockList.isEmpty()) {
            int blockIndex = freeBlockList.remove(0);
            metadataHandler.writeFreeBlockListMetadata(file, freeBlockList);
            return blockIndex;
        }
        return -1;
    }

    private void writeBlockToFile(Block block) throws IOException {
        // Write the block to the file at the corresponding block index
        long blockPosition = (long) block.getBlockIndex() * 256;
        file.seek(blockPosition);
        file.write(block.getData());
    }

    public int updateHeaderBlock(int fileSize) throws IOException {
        // Update the file size in the header block
        byte[] headerData = new byte[256];
        file.seek(0);
        file.read(headerData);

        String headerString = new String(headerData);
        int fileSizeIndex = headerString.indexOf("File size:");
        if (fileSizeIndex != -1) {
            file.seek(fileSizeIndex + 11);
        } else {
            file.seek(256);
        }
        file.writeBytes("File size: " + fileSize + "\n");
        return fileSize;
    }

    public byte[] readData(int blockIndex, int offset, int length) throws IOException {
        // Read data from the specified block
        long blockPosition = (long) blockIndex * 256;
        file.seek(blockPosition + offset);
        byte[] data = new byte[length];
        file.read(data);
        return data;
    }

    public void updateMetadata(FCB fcb) throws IOException {
        // Update the FCB in metadata
        fcbHandler.updateFCBInMetadata(file, fcb);
    }

    public void addFCB(FCB fcb) {
        // Add the FCB to the FCB list
        fcbList.add(fcb);
    }

    public FCB getCurrentFCB() {
    }
}
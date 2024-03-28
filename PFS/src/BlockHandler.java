import java.util.BitSet;

public class BlockHandler {
    private BitSet bitmap;
    private int totalBlocks;
    private static final int BLOCK_SIZE = 256;

    public BlockHandler(int fileSize) {
        this.totalBlocks = (int) Math.ceil((double) fileSize / BLOCK_SIZE);
        initializeBitmap();
    }

    private void initializeBitmap() {
        bitmap = new BitSet(totalBlocks);
    }

    public synchronized void expand(long newFileSize) {
        int newTotalBlocks = (int) Math.ceil((double) newFileSize / BLOCK_SIZE);
        if (newTotalBlocks > totalBlocks) {
            BitSet newBitmap = new BitSet(newTotalBlocks);
            newBitmap.or(bitmap);
            bitmap = newBitmap;
            totalBlocks = newTotalBlocks;
        }
    }

    public synchronized int allocateBlock(int startIndex) {
        int index = bitmap.nextClearBit(startIndex);
        if (index < totalBlocks) {
            bitmap.set(index);
            return index;
        }
        return -1;
    }

    public synchronized void freeBlock(int blockIndex) {
        bitmap.clear(blockIndex);
    }

    public boolean isBlockUsed(int blockIndex) {
        return bitmap.get(blockIndex);
    }

    public void setBlockUsed(int blockIndex, boolean used) {
        bitmap.set(blockIndex, used);
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public byte[] getBitmapAsBytes() {
        return bitmap.toByteArray();
    }
}
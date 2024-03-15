public class Block {
    private int blockNumber;
    private byte[] data;

    public Block(int blockNumber) {
        this.blockNumber = blockNumber;
        this.data = new byte[256];
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isEmpty() {
        // Check if the block is empty
    }
}
public class Block {
    private int blockIndex;
    private byte[] data;
    private int currentPosition;

    public Block(int blockIndex) {
        this.blockIndex = blockIndex;
        this.data = new byte[256];
        this.currentPosition = 0;
    }

    // Getters and Setters
    public int getBlockIndex() {
        return blockIndex;
    }

    public byte[] getData() {
        return data;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public boolean isFull() {
        return currentPosition == 256;
    }

    public int remainingSpace() {
        return 256 - currentPosition;
    }

    public void write(byte[] data, int offset, int length) {
        System.arraycopy(data, offset, this.data, currentPosition, length);
        currentPosition += length;
    }

    public byte[] read(int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, offset, result, 0, length);
        return result;
    }
}
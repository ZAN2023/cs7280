package com.neu.nosql;

public class Block {
    private byte[] data;
    private int nextBlock;

    public static final int BLOCK_SIZE = 256; // BlockSize is defined as a constant

    public Block() {
        this.data = new byte[BLOCK_SIZE];
        this.nextBlock = -1; // -1 represents no next block initially
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(int nextBlock) {
        this.nextBlock = nextBlock;
    }
}

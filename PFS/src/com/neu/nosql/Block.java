package com.neu.nosql;

import static com.neu.nosql.DB.BLOCK_SIZE;
import static com.neu.nosql.DB.ENTRY_SIZE;

public class Block {
    public byte[] data;

    private static final byte DEFAULT_VALUE = 0;

    private int writePosition;

    public void initializeDefaultBytes() {
        for (int i = 0; i < 36; i++) {
            data[i] = DEFAULT_VALUE;
        }
    }

    public void write(byte[] data, int length) {
        if (data.length != ENTRY_SIZE) {
            throw new IllegalArgumentException("Invalid entry data size");
        }

        if (writePosition + ENTRY_SIZE > BLOCK_SIZE) {
            throw new IllegalStateException("Block is full");
        }

        System.arraycopy(data, 0, this.data, writePosition, length);
        writePosition += ENTRY_SIZE;
    }

    public boolean isFull() {
        return writePosition >= BLOCK_SIZE;
    }
}

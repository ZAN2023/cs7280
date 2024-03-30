package com.neu.nosql;

import static com.neu.nosql.DB.BLOCK_SIZE;

public class Block {
    public byte[] data;
    public int nextBlock;

    public Block() {
        this.data = new byte[BLOCK_SIZE];
        this.nextBlock = -1; // -1 represents no next block initially
    }
}

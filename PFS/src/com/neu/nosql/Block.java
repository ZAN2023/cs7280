package com.neu.nosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.neu.nosql.DB.BLOCK_SIZE;
import static com.neu.nosql.DB.ENTRY_SIZE;

/**
 * This class represents a block in the database.
 * A block is a fixed-size unit of storage that holds data entries.
 * The size of a block is determined by the BLOCK_SIZE constant.
 *
 * The Block class provides methods for writing data to the block,
 * checking if the block is full, initializing the block with default values,
 * and retrieving the data entries stored in the block.
 *
 * The block is implemented as a byte array, where each byte represents a single unit of data.
 * The class keeps track of the current write position within the block,
 * and provides methods to fill the remaining bytes with default values.
 *
 * The getDataEntries() method allows retrieving the valid data entries stored in the block,
 * skipping any default or empty entries.
 *
 * The getValidLength() method returns the valid length of the block,
 * excluding any trailing */
public class Block {
    public byte[] data = new byte[BLOCK_SIZE];

    private static final byte DEFAULT_VALUE = ' ';

    private int writePosition = 0;

    /**
     * Fills the remaining bytes in the block with default values.
     * This method is used to ensure that the block is completely filled,
     * even if the actual data doesn't occupy the entire block.
     */
    public void fillUpWithDefaultBytes() {
        for (int i = writePosition; i < BLOCK_SIZE; i++) {
            data[i] = DEFAULT_VALUE;
        }
    }

    /**
     * Initializes the block with default values.
     * This method is used to set the initial state of the block,
     * where the first 36 bytes are filled with default values,
     * and the write position is set to 36.
     */
    public void initializeDefaultBytes() {
        for (int i = 0; i < 36; i++) {
            data[i] = DEFAULT_VALUE;
        }
        writePosition = 36;
    }

    /**
     * Writes the specified data to the block.
     * The data is written starting from the current write position.
     * If the block doesn't have enough space to accommodate the data,
     * an IllegalStateException is thrown.
     *
     * @param data   the data to be written
     * @param length the length of the data
     * @throws IllegalStateException if the block is full and cannot accommodate the data
     */
    public void write(byte[] data, int length) {
        if (writePosition + ENTRY_SIZE > BLOCK_SIZE) {
            throw new IllegalStateException("Block is full");
        }

        System.arraycopy(data, 0, this.data, writePosition, length);
        writePosition += length;
    }

    /**
     * Checks if the block is full.
     * A block is considered full if the write position has reached or exceeded the block size.
     *
     * @return true if the block is full, false otherwise
     */
    public boolean isFull() {
        return writePosition >= BLOCK_SIZE;
    }


    /**
     * Retrieves the valid data entries stored in the block.
     * This method iterates over the block, skipping the first 36 bytes of default values,
     * and extracts the valid data entries.
     * A data entry is considered valid if it is not entirely composed of default values.
     *
     * @return a list of valid data entries stored in the block
     */
    public List<DataEntry> getDataEntries() {
        List<DataEntry> entries = new ArrayList<>();
        int offset = 36; // 跳过前 36 个字节的默认值

        while (offset + 44 <= data.length) {
            byte[] entryData = Arrays.copyOfRange(data, offset, offset + 44);

            // 检查 entryData 是否全为默认值 0
            boolean isDefaultValue = true;
            for (byte b : entryData) {
                if (b != ' ') {
                    isDefaultValue = false;
                    break;
                }
            }

            if (!isDefaultValue) {
                DataEntry entry = DataEntry.deserialize(entryData);
                entries.add(entry);
            }

            offset += 44; // 移动到下一个 data entry 的位置
        }

        return entries;
    }

    /**
     * Returns the valid length of the block.
     * The valid length is determined by excluding any trailing default values.
     * This method iterates over the block from the end, skipping any default values,
     * until it reaches a non-default value or the beginning of the block.
     *
     * @return the valid length of the block
     */
    public int getValidLength() {
        int length = this.data.length;
        while (length > 0 && data[length - 1] == ' ') {
            length--;
        }
        return length;
    }
}

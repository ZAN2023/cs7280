package com.neu.nosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.neu.nosql.DB.BLOCK_SIZE;
import static com.neu.nosql.DB.ENTRY_SIZE;

public class Block {
    public byte[] data;

    private static final byte DEFAULT_VALUE = ' ';

    private int writePosition;

    public void fillUpWithDefaultBytes() {
        for (int i = writePosition; i < BLOCK_SIZE; i++) {
            data[i] = DEFAULT_VALUE;
        }
    }

    public void initializeDefaultBytes() {
        for (int i = 0; i < 36; i++) {
            data[i] = DEFAULT_VALUE;
        }
        writePosition = 36;
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

    public int getValidDataSize() {
        int size = data.length;
        while (size > 0 && data[size - 1] == ' ') {
            size--;
        }
        return size;
    }
}

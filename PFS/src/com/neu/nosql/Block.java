package com.neu.nosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.neu.nosql.DB.BLOCK_SIZE;
import static com.neu.nosql.DB.ENTRY_SIZE;

public class Block {
    public byte[] data = new byte[BLOCK_SIZE];

    private static final byte DEFAULT_VALUE = ' ';

    private int writePosition = 0;

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
        if (writePosition + ENTRY_SIZE > BLOCK_SIZE) {
            throw new IllegalStateException("Block is full");
        }

        System.arraycopy(data, 0, this.data, writePosition, length);
        writePosition += length;
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

    public int getValidLength() {
        int length = this.data.length;
        while (length > 0 && data[length - 1] == ' ') {
            length--;
        }
        return length;
    }
}

package com.neu.nosql;

public class BitMap {
    // 序列化 bitmap 字段为 512 字节
    public static byte[] serializeBitmap(boolean[] bitmap) {
        byte[] bitmapBytes = new byte[512];
        for (int i = 0; i < 4096; i++) {
            if (bitmap[i]) {
                bitmapBytes[i / 8] |= (1 << (i % 8));
            }
        }
        return bitmapBytes;
    }

    // 反序列化 512 字节为 bitmap 字段
    public static boolean[] deserializeBitmap(byte[] bitmapBytes) {
        boolean[] bitmap = new boolean[4096];
        for (int i = 0; i < 4096; i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;
            bitmap[i] = ((bitmapBytes[byteIndex] >> bitIndex) & 1) == 1;
        }
        return bitmap;
    }
}

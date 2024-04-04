package com.neu.nosql;

/**
 * This class provides methods for serializing and deserializing the bitmap field.
 * The bitmap is a boolean array that represents the state of each block in the database.
 * Each element in the bitmap corresponds to a block, indicating whether it is allocated or free.
 *
 * The BitMap class provides two static methods:
 * - serializeBitmap(): Serializes the bitmap boolean array into a byte array of 512 bytes.
 * - deserializeBitmap(): Deserializes a byte array of 512 bytes into a bitmap boolean array.
 *
 * The serialization process converts the boolean values of the bitmap into bits,
 * where each byte in the serialized array represents 8 blocks.
 * The bits are set using bitwise operations.
 *
 * The deserialization process reverses the serialization by extracting the bits from each byte
 * and reconstructing the boolean values of the bitmap.
 *
 * The size of the bitmap is fixed at 4096 elements, corresponding to the total number of blocks
 * in the database. The serialized byte array has a fixed size of 512 bytes.
 */
public class BitMap {
    /**
     * Serializes the bitmap boolean array into a byte array of 512 bytes.
     *
     * @param bitmap the bitmap boolean array to be serialized
     * @return the serialized byte array of 512 bytes
     */
    public static byte[] serializeBitmap(boolean[] bitmap) {
        byte[] bitmapBytes = new byte[512];
        for (int i = 0; i < 4096; i++) {
            if (bitmap[i]) {
                bitmapBytes[i / 8] |= (1 << (i % 8));
            }
        }
        return bitmapBytes;
    }

    /**
     * Deserializes a byte array of 512 bytes into a bitmap boolean array.
     *
     * @param bitmapBytes the serialized byte array of 512 bytes
     * @return the deserialized bitmap boolean array
     */
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

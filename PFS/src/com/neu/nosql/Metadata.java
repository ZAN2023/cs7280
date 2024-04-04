package com.neu.nosql;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.neu.nosql.DB.BLOCK_SIZE;
import static com.neu.nosql.DB.METADATA_DB_NAME_LEN;

/**
 * This class represents the metadata of a database.
 * Metadata contains information about the database, such as its name and suffix.
 * It provides methods to serialize and deserialize metadata objects to/from byte arrays.
 */
public class Metadata {
    public String dbName = "";
    public int suffix = 0;

    /**
     * Constructs a Metadata object with the specified database name and suffix.
     *
     * @param dbName the database name
     * @param suffix the suffix
     */
    public Metadata(String dbName, int suffix) {
        this.dbName = dbName;
        this.suffix = suffix;
    }

    /**
     * Serializes the metadata object to a byte array.
     * The serialization format is as follows:
     * - The first 128 bytes represent the database name, padded with null bytes if necessary.
     * - The next 4 bytes represent the suffix as an integer.
     * - The remaining bytes up to the block size (256 bytes) are filled with zeros.
     *
     * @param metadata the metadata object to be serialized
     * @return the serialized byte array
     */
    public static byte[] serialize(Metadata metadata) {
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

        // 将名称转换为字节数组
        byte[] nameBytes = metadata.dbName.getBytes(StandardCharsets.UTF_8);
        // 确保名称长度为 128 字节
        byte[] paddedNameBytes = new byte[METADATA_DB_NAME_LEN];
        System.arraycopy(nameBytes, 0, paddedNameBytes, 0, Math.min(nameBytes.length, paddedNameBytes.length));
        buffer.put(paddedNameBytes);

        // 将后缀转换为 4 个字节的整数
        buffer.putInt(metadata.suffix);

        // 填充剩余的字节为 0
        for (int i = 132; i < 256; i++) {
            buffer.put((byte) 0);
        }

        // 将缓冲区的内容转换为字节数组并返回
        return buffer.array();
    }

    /**
     * Deserializes the metadata object from a byte array.
     * The deserialization process assumes the byte array follows the format described in the `serialize` method.
     *
     * @param bytes the byte array to be deserialized
     * @return the deserialized metadata object
     */
    public static Metadata deserialize(byte[] bytes) {
        // 创建一个 ByteBuffer 对象，用于解析字节数组
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // 从字节数组中读取名称
        byte[] nameBytes = new byte[METADATA_DB_NAME_LEN];
        buffer.get(nameBytes);
        // 将字节数组转换为字符串，使用 UTF-8 编码
        String name = new String(nameBytes, StandardCharsets.UTF_8).trim();

        // 从字节数组中读取后缀
        int suffix = buffer.getInt();

        // 创建并返回 Metadata 对象
        return new Metadata(name, suffix);
    }

    /**
     * Returns a string representation of the metadata object.
     * The string includes the database name and suffix enclosed in curly braces.
     *
     * @return the string representation of the metadata object
     */
    @Override
    public String toString() {
        return "Metadata{" +
                "dbName='" + dbName + '\'' +
                ", suffix=" + suffix +
                '}';
    }
}

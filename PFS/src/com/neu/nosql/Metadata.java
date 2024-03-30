package com.neu.nosql;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.neu.nosql.DB.BLOCK_SIZE;
import static com.neu.nosql.DB.METADATA_DB_NAME_LEN;

public class Metadata {
    public String dbName = "";
    public int suffix = 0;

    public Metadata(String dbName, int suffix) {
        this.dbName = dbName;
        this.suffix = suffix;
    }

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

    @Override
    public String toString() {
        return "Metadata{" +
                "dbName='" + dbName + '\'' +
                ", suffix=" + suffix +
                '}';
    }
}

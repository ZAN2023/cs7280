package com.neu.nosql;

/**
 * This class represents a data entry in the database.
 * A data entry consists of an ID and a value.
 * The ID is an integer that uniquely identifies the entry.
 * The value is a string that holds the actual data associated with the entry.
 *
 * The class provides methods for serializing and deserializing data entries to/from byte arrays.
 * Serialization is the process of converting an object into a sequence of bytes,
 * which can be stored or transmitted over a network.
 * Deserialization is the reverse process of reconstructing an object from a sequence of bytes.
 *
 * The serialization format for a data entry is as follows:
 * - The first 4 bytes represent the ID, stored as an integer.
 * - The next 40 bytes represent the value, stored as a string.
 * - If the value is shorter than 40 bytes, the remaining bytes are padded with null characters.
 *
 * The class also includes utility methods for converting between integers and byte arrays.
 * These methods are used internally during serialization and deserialization.
 */
public class DataEntry {
    public int id;
    public String val;

    private static final int ID_SIZE = 4;
    private static final int VAL_SIZE = 40;

    /**
     * Constructs a new DataEntry object with the specified ID and value.
     *
     * @param id  the ID of the data entry
     * @param val the value of the data entry
     */
    public DataEntry(int id, String val) {
        this.id = id;
        this.val = val;
    }

    /**
     * Serializes a DataEntry object into a byte array.
     *
     * @param dataBlock the DataEntry object to serialize
     * @return a byte array representing the serialized DataEntry object
     */
    // 序列化方法
    public static byte[] serialize(DataEntry dataBlock) {
        byte[] data = new byte[ID_SIZE + VAL_SIZE];

        // 序列化 id
        byte[] idBytes = intToBytes(dataBlock.id);
        System.arraycopy(idBytes, 0, data, 0, ID_SIZE);

        // 序列化 val
        byte[] valBytes = dataBlock.val.getBytes();
        System.arraycopy(valBytes, 0, data, ID_SIZE, Math.min(valBytes.length, VAL_SIZE));

        return data;
    }

    /**
     * Deserializes a byte array into a DataEntry object.
     *
     * @param data the byte array to deserialize
     * @return a DataEntry object reconstructed from the byte array
     * @throws IllegalArgumentException if the length of the byte array is invalid
     */
    // 反序列化方法
    public static DataEntry deserialize(byte[] data) {
        if (data.length != ID_SIZE + VAL_SIZE) {
            throw new IllegalArgumentException("Invalid data length");
        }

        // 反序列化 id
        byte[] idBytes = new byte[ID_SIZE];
        System.arraycopy(data, 0, idBytes, 0, ID_SIZE);
        int id = bytesToInt(idBytes);

        // 反序列化 val
        byte[] valBytes = new byte[VAL_SIZE];
        System.arraycopy(data, ID_SIZE, valBytes, 0, VAL_SIZE);
        String val = new String(valBytes).trim();

        return new DataEntry(id, val);
    }

    /**
     * Converts an integer to a byte array.
     *
     * @param value the integer to convert
     * @return a byte array representing the integer
     */
    // 将整数转换为字节数组
    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value};
    }

    /**
     * Converts a byte array to an integer.
     *
     * @param bytes the byte array to convert
     * @return an integer reconstructed from the byte array
     */
    // 将字节数组转换为整数
    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}
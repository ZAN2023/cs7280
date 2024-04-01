package com.neu.nosql;

public class DataEntry {
    public int id;
    public String val;

    private static final int ID_SIZE = 4;
    private static final int VAL_SIZE = 40;

    public DataEntry(int id, String val) {
        this.id = id;
        this.val = val;
    }

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

    // 将整数转换为字节数组
    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value};
    }

    // 将字节数组转换为整数
    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }
}
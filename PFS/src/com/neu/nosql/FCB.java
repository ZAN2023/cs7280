package com.neu.nosql;

import java.io.*;
import java.util.ArrayList;


public class FCB {
    public String name;
    public String type;
    public ArrayList<Integer> indexBlocks;
    public ArrayList<Integer> dataBlocks;

    private static final int NAME_SIZE = 20;
    private static final int TYPE_SIZE = 10;
    private static final int INDEX_BLOCK_SIZE = 50;
    private static final int DATA_BLOCK_SIZE = 176;

    public FCB() {
        this.name = "";
        this.type = "";
        this.indexBlocks = new ArrayList<>();
        this.dataBlocks = new ArrayList<>();
    }

    public FCB(String name, String type, ArrayList<Integer> indexBlocks, ArrayList<Integer> dataBlocks) {
        this.name = name;
        this.type = type;
        this.indexBlocks = indexBlocks;
        this.dataBlocks = dataBlocks;
    }

    // 序列化方法
    public static byte[] serialize(FCB fcb) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);

        // 序列化 name
        byte[] nameBytes = new byte[NAME_SIZE];
        byte[] nameBytesData = fcb.name.getBytes();
        System.arraycopy(nameBytesData, 0, nameBytes, 0, Math.min(nameBytesData.length, NAME_SIZE));
        dataOut.write(nameBytes);

        // 序列化 type
        byte[] typeBytes = new byte[TYPE_SIZE];
        byte[] typeBytesData = fcb.type.getBytes();
        System.arraycopy(typeBytesData, 0, typeBytes, 0, Math.min(typeBytesData.length, TYPE_SIZE));
        dataOut.write(typeBytes);

        // 序列化 indexBlocks
        byte[] indexBlockBytes = new byte[INDEX_BLOCK_SIZE];
        ByteArrayOutputStream indexBlockOut = new ByteArrayOutputStream();
        DataOutputStream indexBlockDataOut = new DataOutputStream(indexBlockOut);
        indexBlockDataOut.writeInt(fcb.indexBlocks.size());
        for (Integer block : fcb.indexBlocks) {
            indexBlockDataOut.writeInt(block);
        }
        byte[] indexBlockData = indexBlockOut.toByteArray();
        System.arraycopy(indexBlockData, 0, indexBlockBytes, 0, Math.min(indexBlockData.length, INDEX_BLOCK_SIZE));
        dataOut.write(indexBlockBytes);

        // 序列化 dataBlocks
        byte[] dataBlockBytes = new byte[DATA_BLOCK_SIZE];
        ByteArrayOutputStream dataBlockOut = new ByteArrayOutputStream();
        DataOutputStream dataBlockDataOut = new DataOutputStream(dataBlockOut);
        dataBlockDataOut.writeInt(fcb.dataBlocks.size());
        for (Integer block : fcb.dataBlocks) {
            dataBlockDataOut.writeInt(block);
        }
        byte[] dataBlockData = dataBlockOut.toByteArray();
        System.arraycopy(dataBlockData, 0, dataBlockBytes, 0, Math.min(dataBlockData.length, DATA_BLOCK_SIZE));
        dataOut.write(dataBlockBytes);

        dataOut.close();
        return byteOut.toByteArray();
    }

    // 反序列化方法
    public static FCB deserialize(byte[] data) throws IOException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        DataInputStream dataIn = new DataInputStream(byteIn);

        // 反序列化 name
        byte[] nameBytes = new byte[NAME_SIZE];
        dataIn.readFully(nameBytes);
        String name = new String(nameBytes).trim();

        // 反序列化 type
        byte[] typeBytes = new byte[TYPE_SIZE];
        dataIn.readFully(typeBytes);
        String type = new String(typeBytes).trim();

        // 反序列化 indexBlocks
        byte[] indexBlockBytes = new byte[INDEX_BLOCK_SIZE];
        dataIn.readFully(indexBlockBytes);
        ByteArrayInputStream indexBlockIn = new ByteArrayInputStream(indexBlockBytes);
        DataInputStream indexBlockDataIn = new DataInputStream(indexBlockIn);
        ArrayList<Integer> indexBlocks = new ArrayList<>();
        int indexBlocksCount = indexBlockDataIn.readInt();
        for (int i = 0; i < indexBlocksCount; i++) {
            indexBlocks.add(indexBlockDataIn.readInt());
        }

        // 反序列化 dataBlocks
        byte[] dataBlockBytes = new byte[DATA_BLOCK_SIZE];
        dataIn.readFully(dataBlockBytes);
        ByteArrayInputStream dataBlockIn = new ByteArrayInputStream(dataBlockBytes);
        DataInputStream dataBlockDataIn = new DataInputStream(dataBlockIn);
        ArrayList<Integer> dataBlocks = new ArrayList<>();
        int dataBlocksCount = dataBlockDataIn.readInt();
        for (int i = 0; i < dataBlocksCount; i++) {
            dataBlocks.add(dataBlockDataIn.readInt());
        }

        dataIn.close();
        return new FCB(name, type, indexBlocks, dataBlocks);
    }

    @Override
    public String toString() {
        return "FCB{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", indexBlocks=" + indexBlocks +
                ", dataBlocks=" + dataBlocks +
                '}';
    }
}
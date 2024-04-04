package com.neu.nosql;

import java.io.*;
import java.util.ArrayList;

/**
 * This class represents the file control block (FCB) of a file.
 * The FCB contains metadata and structural information about a file,
 * including its name, type, index blocks, and data blocks.
 * It provides methods to serialize and deserialize FCB objects to/from byte arrays.
 */
public class FCB {
    public String name;
    public String type;
    public ArrayList<Integer> indexBlocks;
    public ArrayList<Integer> dataBlocks;

    private static final int NAME_SIZE = 20;
    private static final int TYPE_SIZE = 10;
    private static final int INDEX_BLOCK_SIZE = 50;
    private static final int DATA_BLOCK_SIZE = 176;

    /**
     * Constructs an FCB object with default values.
     * The name and type are set to empty strings,
     * and the index blocks and data blocks are initialized as empty ArrayLists.
     */
    public FCB() {
        this.name = "";
        this.type = "";
        this.indexBlocks = new ArrayList<>();
        this.dataBlocks = new ArrayList<>();
    }

    /**
     * Constructs an FCB object with the specified name, type, index blocks, and data blocks.
     *
     * @param name        the name of the file
     * @param type        the type of the file
     * @param indexBlocks the list of index block numbers associated with the file
     * @param dataBlocks  the list of data block numbers associated with the file
     */
    public FCB(String name, String type, ArrayList<Integer> indexBlocks, ArrayList<Integer> dataBlocks) {
        this.name = name;
        this.type = type;
        this.indexBlocks = indexBlocks;
        this.dataBlocks = dataBlocks;
    }

    /**
     * Serializes the FCB object to a byte array.
     * The serialization format is as follows:
     * - The first 20 bytes represent the file name, padded with null bytes if necessary.
     * - The next 10 bytes represent the file type, padded with null bytes if necessary.
     * - The next 50 bytes represent the index blocks, where the first 4 bytes indicate the number of index blocks,
     *   followed by the index block numbers, each occupying 4 bytes.
     * - The last 176 bytes represent the data blocks, where the first 4 bytes indicate the number of data blocks,
     *   followed by the data block numbers, each occupying 4 bytes.
     *
     * @param fcb the FCB object to be serialized
     * @return the serialized byte array
     * @throws IOException if an I/O error occurs during serialization
     */
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

    /**
     * Deserializes the FCB object from a byte array.
     * The deserialization process assumes the byte array follows the format described in the `serialize` method.
     *
     * @param data the byte array to be deserialized
     * @return the deserialized FCB object
     * @throws IOException if an I/O error occurs during deserialization
     */
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

    /**
     * Returns a string representation of the FCB object.
     * The string includes the file name, type, index blocks, and data blocks enclosed in curly braces.
     *
     * @return a string representation of the FCB object
     */
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
package com.neu.nosql;

import com.neu.nosql.index.BTree;
import com.neu.nosql.index.BTreeSerializer;
import com.neu.nosql.io.MovieReader;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DB {
    public final ArrayList<Block> blocks = new ArrayList<>(BLOCK_CNT);
    public Metadata metadata = null;
    public final boolean[] bitmap = new boolean[BLOCK_CNT];
    public final ArrayList<FCB> fcbs = new ArrayList<>(FCB_SIZE);

    public static final int BLOCK_CNT = 4096; // 一个db file有4096个block
    public static final int METADATA_BLOCK_CNT = 1; // 1个block存储medata
    public static final int BITMAP_BLOCK_CNT = 2; // 2个block存储medata
    public static final int METADATA_DB_NAME_LEN = 128; // db_name在metadata序列化时最多占用128 bytes
    public static final int BLOCK_SIZE = 256; // 一个block占用256 bytes
    public static final int FCB_SIZE = 8; // 一个db文件最多8个fcb
    public static final int BLOCK_ENTRY_NUM = 5; // 一个block能放5个data entry
    public static final int ENTRY_SIZE = 44;
    private static final int FILE_SIZE = 1024 * 1024; // 1MB

    public static DB open(String dbName) throws Exception {
        return open(dbName, 0);
    }

    public static DB open(String dbName, int suffix) throws Exception {
        String dbPath = "./src/com/neu/nosql/file/" + dbName + " _" + suffix;
        if (Files.notExists(Paths.get(dbPath))) {
            return newDB(dbName, 0);
        }

        DB db = new DB();

        // 初始化blocks
        byte[] data = Files.readAllBytes(Paths.get(dbPath));
        for (int i = 0; i < BLOCK_CNT; i++) {
            db.blocks.set(i, new Block());
            System.arraycopy(data, i * BLOCK_SIZE, db.blocks.get(i).data, 0, BLOCK_SIZE);
        }

        // 初始化metadata
        db.metadata = Metadata.deserialize(db.blocks.get(0).data);

        // 初始化bitmap
        for (int i = 0; i < BLOCK_CNT; i++) {
            if (i < BLOCK_CNT / 2) {
                db.bitmap[i] = (db.blocks.get(METADATA_BLOCK_CNT).data[i / 8] & (1 << (i % 8))) != 0;
            } else {
                db.bitmap[i] =
                        (db.blocks.get(METADATA_BLOCK_CNT + 1).data[(i - BLOCK_CNT / 2) / 8] & (1 << ((i - BLOCK_CNT / 2) % 8))) != 0;
            }
        }

        // 初始化FCB
        for (int i = 0; i < FCB_SIZE; i++) {
            db.fcbs.set(i, FCB.deserialize(db.blocks.get(1 + i).data));
        }

        return db;
    }

    private static DB newDB(String dbName, int suffix) {
        DB db = new DB();
        db.metadata = new Metadata(dbName, suffix);
        Arrays.fill(db.bitmap, 0, METADATA_BLOCK_CNT + BITMAP_BLOCK_CNT, true);

        return db;
    }

    public int countEmptyBlock() {
        int cnt = 0;
        for (int i = 11; i < this.bitmap.length; i++) {
            if (!this.bitmap[i]) {
                cnt++;
            }
        }
        return cnt;
    }

    public int nextFCB() {
        for (int i = 3; i < 11; i++) {
            if (!this.bitmap[i]) {
                this.bitmap[i] = true;
                return i;
            }
        }
        return -1;
    }

    public ArrayList<Integer> allocateBlocks(int num) {
        ArrayList<Integer> blocks = new ArrayList<>();
        for (int i = 11; i < this.bitmap.length && num > 0; i++) {
            if (!this.bitmap[i]) {
                blocks.add(i);
                this.bitmap[i] = true;
                num--;
            }
        }
        return blocks;
    }

    public static DB selectDBFile(String dbName, String fileName) throws Exception {
        int blockNeeded = calcBlockNeeded(fileName);
        DB db = null;
        for (int i = 0; ; i++) {
            db = open(dbName, i);
            if (db.countEmptyBlock() >= blockNeeded) {
                break;
            }
        }
        return db;
    }

    private static int calcBlockNeeded(String fileName) {
        Map<Integer, String> lines = MovieReader.readMoviesFromCSV("./src/com/neu/nosql/io/" + fileName);

        int dataBlockNum = lines.size() / BLOCK_ENTRY_NUM;
        if (lines.size() % BLOCK_ENTRY_NUM != 0) {
            dataBlockNum++;
        }

        BTree bTree = new BTree();
        for (Map.Entry<Integer, String> line : lines.entrySet()) {
            bTree.insert(line.getKey(), -1);
        }
        byte[] indexBytes = new BTreeSerializer().serialize(bTree.getRoot()).getBytes();
        int indexBlockNum = indexBytes.length / BLOCK_SIZE;
        if (indexBytes.length % BLOCK_SIZE != 0) {
            indexBlockNum++;
        }

        return dataBlockNum + indexBlockNum;
    }

    public void put(String fileName) throws Exception {
        // 解析输入csv文件为map，每一个entry代表一行，key是ID，val是内容
        Map<Integer, String> lines = MovieReader.readMoviesFromCSV("./src/com/neu/nosql/io/" + fileName);
        int dataBlockNum = lines.size() / BLOCK_ENTRY_NUM;
        if (lines.size() % BLOCK_ENTRY_NUM != 0) {
            dataBlockNum++;
        }

        // 分配空的block列表，把文件内容放到对应的data block中
        // 记录每行id对应的block ID，给后边index使用
        Map<Integer, Integer> id2Block = new HashMap<>(); // 输入文件里，每行id对应的数据在当前db的block id
        ArrayList<Integer> dataBlocks = allocateBlocks(dataBlockNum);
        int p = 0;
        for (Map.Entry<Integer, String> line : lines.entrySet()) {
            DataEntry entry = new DataEntry(line.getKey(), line.getValue());
            if (this.blocks.get(dataBlocks.get(p)).isFull()) {
                p++;
                this.blocks.get(dataBlocks.get(p)).initializeDefaultBytes();
            }
            this.blocks.get(dataBlocks.get(p)).write(DataEntry.serialize(entry), ENTRY_SIZE);
            id2Block.put(line.getKey(), dataBlocks.get(p));
        }
        // 分配索引需要的block列表，创建索引，并将索引放到block
        BTree bTree = new BTree();
        for (Map.Entry<Integer, String> line : lines.entrySet()) {
            bTree.insert(line.getKey(), id2Block.get(line.getKey()));
        }
        byte[] indexBytes = new BTreeSerializer().serialize(bTree.getRoot()).getBytes();
        int indexBlockNum = indexBytes.length / BLOCK_SIZE;
        if (indexBytes.length % BLOCK_SIZE != 0) {
            indexBlockNum++;
        }
        ArrayList<Integer> indexBlocks = allocateBlocks(indexBlockNum);
        for (int i = 0; i < indexBlocks.size(); i++) {
            int from = i * BLOCK_SIZE;
            if (i == indexBlocks.size() - 1) {
                this.blocks.get(indexBlocks.get(i)).
                        write(Arrays.copyOfRange(indexBytes, from, indexBytes.length), indexBytes.length - from);
            } else {
                this.blocks.get(indexBlocks.get(i)).
                        write(Arrays.copyOfRange(indexBytes, from, from + BLOCK_SIZE), BLOCK_SIZE);
            }
        }
        // 分配空fcb，并初始化当前文件的fcb，放到db中
        int fcbBlockID = nextFCB();
        this.fcbs.set(fcbBlockID - 3, new FCB(fileName.substring(0, fileName.lastIndexOf('.')), "csv", indexBlocks, dataBlocks));

        // flush当前db到磁盘
        this.flush();
    }

    private byte[] serializeBitmap() {
        byte[] bytes = new byte[BLOCK_CNT];
        for (int i = 0; i < this.bitmap.length; i++) {
            if (this.bitmap[i]) {
                bytes[i / 8] |= (1 << (i % 8));
            }
        }
        return bytes;
    }

    private void flush() {
        String fileName = this.metadata.dbName + ".db" + this.metadata.suffix;
        try (FileOutputStream fos = new FileOutputStream(fileName);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(FILE_SIZE);

            // Write metadata
            byte[] metadataBytes = Metadata.serialize(this.metadata);
            buffer.put(metadataBytes);

            // Write bitmap
            byte[] bitmapBytes = serializeBitmap();
            buffer.put(bitmapBytes);

            // Write FCBs
            for (FCB fcb : this.fcbs) {
                byte[] fcbBytes = FCB.serialize(fcb);
                buffer.put(fcbBytes);
            }

            // Write blocks
            for (int i = 11; i < BLOCK_CNT; i++) {
                buffer.put(this.blocks.get(i).data);
            }

            buffer.flip();
            channel.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get(String fileName) throws Exception {

    }

    public String find(String fileName, int key) throws Exception {
        return null;
    }

    public ArrayList<String> dir() throws Exception {
        return null;
    }

    public void kill(String dbName) throws Exception {

    }
}

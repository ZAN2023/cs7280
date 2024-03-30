package com.neu.nosql;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class DB {
    private ArrayList<Block> blocks = new ArrayList<>(BLOCK_CNT);
    private Metadata metadata = null;
    private boolean[] bitmap = new boolean[BLOCK_CNT];
    private ArrayList<FCB> fcbs = new ArrayList<>(FCB_SIZE);

    public static final int BLOCK_CNT = 4096; // 一个db file有4096个block
    public static final int METADATA_BLOCK_CNT = 1; // 1个block存储medata
    public static final int BITMAP_BLOCK_CNT = 2; // 2个block存储medata
    public static final int METADATA_DB_NAME_LEN = 128; // db_name在metadata序列化时最多占用128 bytes
    public static final int BLOCK_SIZE = 256; // 一个block占用256 bytes
    public static final int FCB_SIZE = 8; // 一个db文件最多8个fcb

    public static DB open(String dbName) throws Exception {
        String dbPath = "./src/com/neu/nosql/file/" + dbName + " _0 ";
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

    public void put(String fileName) throws Exception {

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

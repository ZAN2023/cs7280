package com.neu.nosql;

import com.neu.nosql.index.BTree;
import com.neu.nosql.index.BTreeNode;
import com.neu.nosql.index.BTreeSerializer;
import com.neu.nosql.io.MovieReader;
import com.neu.nosql.io.MovieWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.neu.nosql.Utils.isMatchingFile;

/**
 * This class represents a database.
 * It manages the blocks, metadata, bitmap, and file control blocks (FCBs) of the database.
 * It provides methods to open, create, and manipulate the database, including putting, getting,
 * removing, and finding files within the database.
 */
public class DB {
    public ArrayList<Block> blocks = new ArrayList<>(BLOCK_CNT);
    public Metadata metadata = null;
    public final boolean[] bitmap = new boolean[BLOCK_CNT];
    public ArrayList<FCB> fcbs = new ArrayList<>(FCB_SIZE);

    public static final int BLOCK_CNT = 4096; // 一个db file有4096个block
    public static final int METADATA_BLOCK_CNT = 1; // 1个block存储medata
    public static final int BITMAP_BLOCK_CNT = 2; // 2个block存储medata
    public static final int METADATA_DB_NAME_LEN = 128; // db_name在metadata序列化时最多占用128 bytes
    public static final int BLOCK_SIZE = 256; // 一个block占用256 bytes
    public static final int FCB_SIZE = 8; // 一个db文件最多8个fcb
    public static final int BLOCK_ENTRY_NUM = 5; // 一个block能放5个data entry
    public static final int ENTRY_SIZE = 44;
    private static final int FILE_SIZE = 1024 * 1024; // 1MB

    /**
     * Opens an existing database with the specified name.
     *
     * @param dbName the name of the database
     * @return the opened database
     * @throws Exception if an error occurs while opening the database
     */
    public static DB open(String dbName) throws Exception {
        return open(dbName, 0);
    }

    /**
     * Opens an existing database with the specified name and suffix.
     * If the database file does not exist, a new database is created.
     *
     * @param dbName the name of the database
     * @param suffix the suffix of the database file
     * @return the opened or created database
     * @throws Exception if an error occurs while opening or creating the database
     */
    public static DB open(String dbName, int suffix) throws Exception {
        String dbPath = "./src/com/neu/nosql/file/" + dbName + ".db" + suffix;
        if (Files.notExists(Paths.get(dbPath))) {
            return newDB(dbName, 0);
        }

        DB db = new DB();

        // Initialize blocks
        byte[] data = Files.readAllBytes(Paths.get(dbPath));
        for (int i = 0; i < BLOCK_CNT; i++) {
            db.blocks.add(new Block());
            System.arraycopy(data, i * BLOCK_SIZE, db.blocks.get(i).data, 0, BLOCK_SIZE);
        }

        // Initialize metadata
        db.metadata = Metadata.deserialize(db.blocks.get(0).data);

        // Initialize bitmap
        for (int i = 0; i < BLOCK_CNT; i++) {
            if (i < BLOCK_CNT / 2) {
                db.bitmap[i] = (db.blocks.get(METADATA_BLOCK_CNT).data[i / 8] & (1 << (i % 8))) != 0;
            } else {
                db.bitmap[i] =
                        (db.blocks.get(METADATA_BLOCK_CNT + 1).data[(i - BLOCK_CNT / 2) / 8] & (1 << ((i - BLOCK_CNT / 2) % 8))) != 0;
            }
        }

        // Initialize FCBs
        for (int i = 0; i < FCB_SIZE; i++) {
            db.fcbs.add(new FCB());
            db.fcbs.set(i, FCB.deserialize(db.blocks.get(3 + i).data));
        }

        return db;
    }

    /**
     * Puts the specified file into the database.
     * The file is split into data entries and stored in data blocks.
     * An index is created using a B-tree to map the entry IDs to their corresponding block IDs.
     * The file control block (FCB) for the file is updated with the index and data block information.
     *
     * @param fileName the name of the file to be put into the database
     * @throws Exception if an error occurs while putting the file into the database
     */
    public void put(String fileName) throws Exception {
        // Parse the input CSV file into a map, where each entry represents a row
        // The key is the ID, and the value is the content
        Map<Integer, String> lines = MovieReader.readMoviesFromCSV("./src/com/neu/nosql/io/" + fileName);
        int dataBlockNum = lines.size() / BLOCK_ENTRY_NUM;
        if (lines.size() % BLOCK_ENTRY_NUM != 0) {
            dataBlockNum++;
        }

        // Allocate empty blocks and store the file content in the corresponding data blocks
        // Record the block ID for each row ID for later indexing
        Map<Integer, Integer> id2Block = new HashMap<>(); // Mapping of row ID to block ID in the current database
        ArrayList<Integer> dataBlocks = allocateBlocks(dataBlockNum);
        int p = -1;
        for (Map.Entry<Integer, String> line : lines.entrySet()) {
            DataEntry entry = new DataEntry(line.getKey(), line.getValue());
            if (p == -1 || this.blocks.get(dataBlocks.get(p)).isFull()) {
                p++;
                this.blocks.get(dataBlocks.get(p)).initializeDefaultBytes();
            }
            this.blocks.get(dataBlocks.get(p)).write(DataEntry.serialize(entry), ENTRY_SIZE);
            id2Block.put(line.getKey(), dataBlocks.get(p));
        }
        this.blocks.get(dataBlocks.get(p)).fillUpWithDefaultBytes();
        // Allocate blocks for the index, create the index using a B-tree, and store the index in the blocks
        BTree bTree = new BTree();
        for (Map.Entry<Integer, String> line : lines.entrySet()) {
            bTree.insert(line.getKey(), id2Block.get(line.getKey()));
        }
        new BTree().print(bTree.getRoot());
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
                this.blocks.get(indexBlocks.get(i)).fillUpWithDefaultBytes();
            } else {
                this.blocks.get(indexBlocks.get(i)).
                        write(Arrays.copyOfRange(indexBytes, from, from + BLOCK_SIZE), BLOCK_SIZE);
            }
        }
        // Allocate an empty FCB and initialize the FCB for the current file
        int fcbBlockID = nextFCB();
        this.fcbs.set(fcbBlockID - 3, new FCB(Utils.parseInputFileName(fileName), "csv", indexBlocks, dataBlocks));

        // Flush the current database to disk
        this.flush();
    }

    /**
     * Gets the specified file from the database.
     * The file content is retrieved from the data blocks and written to an output CSV file.
     *
     * @param fileName the name of the file to be retrieved from the database
     * @throws Exception if an error occurs while getting the file from the database
     */
    public void get(String fileName) throws Exception {
        for (FCB fcb : this.fcbs) {
            if (fcb.name.equals(Utils.parseInputFileName(fileName)) && fcb.type.equals(Utils.parseInputFileType(fileName))) {
                Map<Integer, String> lines = new HashMap<>();
                for (int blockID : fcb.dataBlocks) {
                    Block block = this.blocks.get(blockID);
                    for (DataEntry entry : block.getDataEntries()) {
                        lines.put(entry.id, entry.val);
                    }
                }
                String directory = "./src/com/neu/nosql/io/";
                String outputPath = directory + "/" + fileName + ".output";
                MovieWriter.writeToCSV(lines, outputPath);
            }
        }
    }

    /**
     * Removes the specified file from the database.
     * The file's index blocks and data blocks are deleted, and the corresponding FCB is reset.
     *
     * @param fileName the name of the file to be removed from the database
     * @throws Exception if an error occurs while removing the file from the database
     */
    public void remove(String fileName) throws Exception {
        for (int i = 0; i < this.fcbs.size(); i++) {
            FCB fcb = this.fcbs.get(i);
            if (fcb.name.equals(Utils.parseInputFileName(fileName)) && fcb.type.equals(Utils.parseInputFileType(fileName))) {
                ArrayList<Integer> indexBlocks = fcb.indexBlocks;
                for (int id : indexBlocks) {
                    this.blocks.set(id, new Block());
                    this.bitmap[id] = false;
                }
                ArrayList<Integer> dataBlocks = fcb.indexBlocks;
                for (int id : indexBlocks) {
                    this.blocks.set(id, new Block());
                    this.bitmap[id] = false;
                }

                this.fcbs.set(i, new FCB());
                break;
            }
        }
        this.flush();
    }

    /**
     * Finds the specified file from the database and returns the value associated with the given ID.
     *
     * @param fileName the name of the file to be searched
     * @param id       the ID to be searched within the file
     * @return the value associated with the given ID in the file, or null if not found
     * @throws Exception if an error occurs while finding the file or ID
     */
    public String find(String fileName, int id) throws Exception {
        for (FCB fcb : this.fcbs) {
            if (fcb.name.equals(Utils.parseInputFileName(fileName)) && fcb.type.equals(Utils.parseInputFileType(fileName))) {
                int totalLength = 0;
                for (int blockID : fcb.indexBlocks) {
                    Block block = this.blocks.get(blockID);
                    int validLength = block.getValidLength();
                    totalLength += validLength;
                }

                byte[] result = new byte[totalLength];
                int destPos = 0;
                for (int blockID : fcb.indexBlocks) {
                    Block block = this.blocks.get(blockID);
                    int validLength = block.getValidLength();
                    System.arraycopy(block.data, 0, result, destPos, validLength);
                    destPos += validLength;
                }
                BTreeNode root = new BTreeSerializer().deserialize(new String(result));
                int blockID = BTree.findKey(root, id);
                Block block = this.blocks.get(blockID);
                for (DataEntry entry : block.getDataEntries()) {
                    if (entry.id == id) {
                        return entry.val;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Lists all the files in the database directory.
     *
     * @return the list of file names in the database directory
     * @throws Exception if an error occurs while listing the files
     */
    public static ArrayList<String> dir() throws Exception {
        ArrayList<String> ans = new ArrayList<>();

        File[] files = new File("./src/com/neu/nosql/file/").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    ans.add(file.getName());
                }
            }
        }
        return ans;
    }

    /**
     * Kills (deletes) all the database files with the specified name.
     *
     * @param dbName the name of the database to be killed
     * @throws Exception if an error occurs while killing the database
     */
    public void kill(String dbName) throws Exception {
        File directory = new File("./src/com/neu/nosql/file/");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isMatchingFile(file, dbName)) {
                        file.delete();
                    }
                }
            }
        }
    }

    /**
     * Creates a new database with the specified name and suffix.
     * Initializes the blocks, FCBs, metadata, and bitmap for the new database.
     *
     * @param dbName the name of the new database
     * @param suffix the suffix of the new database
     * @return the newly created database
     */
    private static DB newDB(String dbName, int suffix) {
        DB db = new DB();

        for (int i = 0; i < BLOCK_CNT; i++) {
            db.blocks.add(new Block());
        }
        for (int i = 0; i < FCB_SIZE; i++) {
            db.fcbs.add(new FCB());
        }

        db.metadata = new Metadata(dbName, suffix);

        Arrays.fill(db.bitmap, 0, METADATA_BLOCK_CNT + BITMAP_BLOCK_CNT, true);

        db.flush();
        return db;
    }

    /**
     * Counts the number of empty blocks in the database.
     *
     * @return the number of empty blocks
     */
    public int countEmptyBlock() {
        int cnt = 0;
        for (int i = 11; i < this.bitmap.length; i++) {
            if (!this.bitmap[i]) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Allocates the specified number of blocks from the database.
     * Returns the block IDs of the allocated blocks.
     *
     * @param num the number of blocks to allocate
     * @return the list of block IDs of the allocated blocks
     */
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

    /**
     * Finds the next available file control block (FCB) in the database.
     * Returns the index of the next available FCB, or -1 if no FCB is available.
     *
     * @return the index of the next available FCB, or -1 if no FCB is available
     */
    public int nextFCB() {
        for (int i = 3; i < 11; i++) {
            if (!this.bitmap[i]) {
                this.bitmap[i] = true;
                return i;
            }
        }
        return -1;
    }

    /**
     * Selects a database file with the specified name and file name.
     * Returns the database file that has enough empty blocks to store the file.
     *
     * @param dbName   the name of the database
     * @param fileName the name of the file to be stored
     * @return the database file that has enough empty blocks to store the file
     * @throws Exception if an error occurs while selecting the database file
     */
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

    /**
     * Calculates the number of blocks needed to store the specified file.
     *
     * @param fileName the name of the file to be stored
     * @return the number of blocks needed to store the file
     */
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

    /**
     * Locates the database file with the specified name and file name.
     * Returns the database file that contains the specified file.
     *
     * @param dbName   the name of the database
     * @param fileName the name of the file to be located
     * @return the database file that contains the specified file
     * @throws Exception if an error occurs while locating the database file
     */
    public static DB locateDB(String dbName, String fileName) throws Exception {
        DB db = null;
        for (int i = 0; ; i++) {
            String dbPath = "./src/com/neu/nosql/file/" + dbName + ".db" + i;
            if (Files.notExists(Paths.get(dbPath))) {
                break;
            }
            db = open(dbName, i);
            for (FCB fcb : db.fcbs) {
                if (fcb.name.equals(Utils.parseInputFileName(fileName)) && fcb.type.equals(Utils.parseInputFileType(fileName))) {
                    return db;
                }
            }
        }
        return null;
    }

    /**
     * Flushes the current state of the database to disk.
     * The method writes the metadata, bitmap, file control blocks (FCBs), and blocks to the database file.
     * The database file is named according to the database name and suffix.
     * The file is written using a ByteBuffer and FileChannel for efficient I/O operations.
     */
    private void flush() {
        String fileName = "./src/com/neu/nosql/file/" + this.metadata.dbName + ".db" + this.metadata.suffix;
        try (FileOutputStream fos = new FileOutputStream(fileName);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(FILE_SIZE);

            // Write metadata
            byte[] metadataBytes = Metadata.serialize(this.metadata);
            buffer.put(metadataBytes);

            // Write bitmap
            byte[] bitmapBytes = BitMap.serializeBitmap(this.bitmap);
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
}

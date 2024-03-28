public class PFSFileSystem {
    private FileHandler fileHandler;
    private FCBHandler fcbHandler;
    private MetadataHandler metadataHandler;
    private BTreeIndex indexTree;

    public PFSFileSystem(String fileName) {
        this.fileHandler = new FileHandler(fileName);
        this.fcbHandler = new FCBHandler();
        this.metadataHandler = new MetadataHandler();
        this.indexTree = new BTreeIndex();
    }

    public void createFile(String fileName) {
        // 创建新文件
        FCB fcb = new FCB(fileName, 0, 0, 0, 0, 0);
        fileHandler.addFCB(fcb);
        fcbHandler.updateOrAddFCBInMetadata(fcb);
    }

    public void openFile(String fileName) {
        // 打开文件
        FCB fcb = fcbHandler.findFCBByFileName(fileName);
        if (fcb != null) {
            fileHandler.setCurrentFCB(fcb);
        }
    }

    public void closeFile() {
        // 关闭文件
        fileHandler.close();
    }

    public void insertData(int key, byte[] value) {
        // 插入键值对数据
        fileHandler.write(value);
        indexTree.insert(key, fileHandler.getCurrentBlockIndex());
    }

    public byte[] getData(int key) {
        // 获取键值对数据
        int blockIndex = indexTree.search(key);
        if (blockIndex != -1) {
            return fileHandler.readData(blockIndex, 0, FileHandler.BLOCK_SIZE);
        }
        return null;
    }

    public void deleteData(int key) {
        // 删除键值对数据
        int blockIndex = indexTree.search(key);
        if (blockIndex != -1) {
            indexTree.delete(key);
            fileHandler.freeBlock(blockIndex);
        }
    }
}
import java.nio.ByteBuffer;

public class FCB {
    private String fileName;
    private int startBlock;
    private int usedBlocks;
    private int fileSize;
    private long indexStartPosition;
    private long indexEndPosition;

    public FCB(String fileName, int startBlock, int usedBlocks, int fileSize, long indexStartPosition, long indexEndPosition) {
        this.fileName = fileName;
        this.startBlock = startBlock;
        this.usedBlocks = usedBlocks;
        this.fileSize = fileSize;
        this.indexStartPosition = indexStartPosition;
        this.indexEndPosition = indexEndPosition;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(int startBlock) {
        this.startBlock = startBlock;
    }

    public int getUsedBlocks() {
        return usedBlocks;
    }

    public void setUsedBlocks(int usedBlocks) {
        this.usedBlocks = usedBlocks;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public long getIndexStartPosition() {
        return indexStartPosition;
    }

    public void setIndexStartPosition(long indexStartPosition) {
        this.indexStartPosition = indexStartPosition;
    }

    public long getIndexEndPosition() {
        return indexEndPosition;
    }

    public void setIndexEndPosition(long indexEndPosition) {
        this.indexEndPosition = indexEndPosition;
    }

    public static FCB fromBytes(byte[] bytes) {
        // 从字节数组中读取 FCB 对象
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] fileNameBytes = new byte[64];
        buffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes).trim();
        int startBlock = buffer.getInt();
        int usedBlocks = buffer.getInt();
        int fileSize = buffer.getInt();
        long indexStartPosition = buffer.getLong();
        long indexEndPosition = buffer.getLong();
        return new FCB(fileName, startBlock, usedBlocks, fileSize, indexStartPosition, indexEndPosition);
    }

    public byte[] toBytes() {
        // 将 FCB 对象转换为字节数组
        ByteBuffer buffer = ByteBuffer.allocate(256);
        buffer.put(fileName.getBytes());
        buffer.putInt(startBlock);
        buffer.putInt(usedBlocks);
        buffer.putInt(fileSize);
        buffer.putLong(indexStartPosition);
        buffer.putLong(indexEndPosition);
        return buffer.array();
    }
}

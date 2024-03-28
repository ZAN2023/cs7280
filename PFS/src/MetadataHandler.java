import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MetadataHandler {
    private static final int FILE_NAME_SIZE = 256;
    private static final int HEADER_BLOCKS = 1;
    private static final int BLOCK_SIZE = 256;
    private static final int HEADER_SIZE = HEADER_BLOCKS * BLOCK_SIZE;
    private static final int FCB_SIZE = 256;
    private static final int FCB_LIST_OFFSET = FILE_NAME_SIZE;
    private static final int FCB_LIST_SIZE_OFFSET = 0;
    private static final int FCB_LIST_SIZE_BYTES = 4;
    private static final int FREE_BLOCK_LIST_OFFSET = FCB_LIST_OFFSET + FCB_LIST_SIZE_BYTES;
    private static final int FREE_BLOCK_LIST_SIZE_BYTES = 4;

    public void writeInitialMetadata(RandomAccessFile file, String fileName) throws IOException {
        // 写入初始元数据到文件头部
        file.seek(0);
        // 写入文件名
        byte[] fileNameBytes = fileName.getBytes();
        byte[] paddedFileNameBytes = new byte[FILE_NAME_SIZE];
        System.arraycopy(fileNameBytes, 0, paddedFileNameBytes, 0, fileNameBytes.length);
        file.write(paddedFileNameBytes);
        // 写入 FCB 列表大小 (初始为0)
        file.writeInt(0);
        // 写入空闲块列表大小 (初始为0)
        file.writeInt(0);
    }

    public void writeFCBListMetadata(RandomAccessFile file, List<FCB> fcbList) throws IOException {
        // 写入 FCB 列表到元数据
        file.seek(FCB_LIST_OFFSET + FCB_LIST_SIZE_BYTES);
        // 写入 FCB 列表大小
        file.writeInt(fcbList.size());
        // 写入每个 FCB 的字节数组
        for (FCB fcb : fcbList) {
            file.write(fcb.toBytes());
        }
    }

    public List<FCB> readFCBListMetadata(RandomAccessFile file) throws IOException {
        // 从元数据中读取 FCB 列表
        file.seek(FCB_LIST_OFFSET);
        int fcbListSize = file.readInt();
        List<FCB> fcbList = new ArrayList<>();
        for (int i = 0; i < fcbListSize; i++) {
            byte[] fcbBytes = new byte[FCB_SIZE];
            file.read(fcbBytes);
            FCB fcb = FCB.fromBytes(fcbBytes);
            fcbList.add(fcb);
        }
        return fcbList;
    }

    public void writeFreeBlockListMetadata(RandomAccessFile file, List<Integer> freeBlockList) throws IOException {
        // 写入空闲块列表到元数据
        file.seek(FREE_BLOCK_LIST_OFFSET + FREE_BLOCK_LIST_SIZE_BYTES);
        // 写入空闲块列表大小
        file.writeInt(freeBlockList.size());
        // 写入每个空闲块的索引
        for (int blockIndex : freeBlockList) {
            file.writeInt(blockIndex);
        }
    }

    public List<Integer> readFreeBlockListMetadata(RandomAccessFile file) throws IOException {
        // 从元数据中读取空闲块列表
        file.seek(FREE_BLOCK_LIST_OFFSET);
        int freeBlockListSize = file.readInt();
        List<Integer> freeBlockList = new ArrayList<>();
        for (int i = 0; i < freeBlockListSize; i++) {
            int blockIndex = file.readInt();
            freeBlockList.add(blockIndex);
        }
        return freeBlockList;
    }
}
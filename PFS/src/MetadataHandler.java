import java.io.RandomAccessFile;
import java.util.List;

public class MetadataHandler {
    private static final int FILE_NAME_SIZE = 256;
    private static final int HEADER_BLOCKS = 1;
    private static final int BLOCK_SIZE = 256;
    private static final int HEADER_SIZE = HEADER_BLOCKS * BLOCK_SIZE;
    // 其他元数据相关的常量...

    public void writeMetadata(RandomAccessFile file, List<FCB> fcbList, BlockHandler blockHandler) {
        // 写入元数据到文件
        // ...
    }

    public List<FCB> readMetadata(RandomAccessFile file, BlockHandler blockHandler) {
        // 从文件读取元数据
        // ...
    }
}
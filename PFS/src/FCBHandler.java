import java.util.List;

public class FCBHandler {
    public void updateOrAddFCBInMetadata(FCB fcb) {
        // 将新的 FCB 写入元数据中的 FCB 列表
        // 包括写入 FCB 列表的长度、写入每个 FCB 对象的字节数组、将字节数组写入到文件的元数据区域
        // ...
    }

    public List<FCB> readFCBListFromMetadata() {
        // 从元数据中读取 FCB 列表
        // ...
    }

    public FCB findFCBByFileName(String fileName) {
        // 根据文件名查找 FCB
        // ...
    }
}
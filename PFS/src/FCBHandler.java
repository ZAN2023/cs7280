import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FCBHandler {
    private MetadataHandler metadataHandler;
    private List<FCB> fcbList;

    public FCBHandler(MetadataHandler metadataHandler) {
        this.metadataHandler = metadataHandler;
        this.fcbList = new ArrayList<>();
    }

//    public FCBHandler() {
//
//    }

    public void addFCB(FCB fcb) {
        fcbList.add(fcb);
    }

    public void writeFCBListMetadata(RandomAccessFile file) throws IOException {
        metadataHandler.writeFCBListMetadata(file, fcbList);
    }

    public void updateFCBInMetadata(RandomAccessFile file, FCB fcb) throws IOException {
        fcbList = metadataHandler.readFCBListMetadata(file);
        for (int i = 0; i < fcbList.size(); i++) {
            if (fcbList.get(i).getFileName().equals(fcb.getFileName())) {
                fcbList.set(i, fcb);
                break;
            }
        }
        metadataHandler.writeFCBListMetadata(file, fcbList);
    }

    public List<FCB> readFCBListFromMetadata(RandomAccessFile file) throws IOException {
        fcbList = metadataHandler.readFCBListMetadata(file);
        return fcbList;
    }

    public FCB findFCBByFileName(String fileName) {
        for (FCB fcb : fcbList) {
            if (fcb.getFileName().equals(fileName)) {
                return fcb;
            }
        }
        return null;
    }
}
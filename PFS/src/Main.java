/*
* 文件的创建、打开之类的 operation
* */

public class Main {
    public static void main(String[] args) {
        PFSFileSystem pfs = new PFSFileSystem("test.db");
        pfs.createFile("data.txt", "Data");
        pfs.createFile("index.idx", "Index");

        // Test data file
        pfs.writeData(1, "Hello".getBytes());
        pfs.writeData(2, "World".getBytes());
        System.out.println(new String(pfs.readData(1))); // Hello
        System.out.println(new String(pfs.readData(2))); // World

        // Test index file
        // ...

        pfs.closeFile("data.txt");
        pfs.closeFile("index.idx");
    }
}
package com.neu.nosql;

import com.neu.nosql.index.BTree;
import com.neu.nosql.index.BTreeNode;
import com.neu.nosql.index.BTreeSerializer;
import com.neu.nosql.io.MovieReader;
import com.neu.nosql.io.MovieWriter;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("============== Test B-Tree =============");
        BTree bTree = new BTree();
        int[] keys = {34, 11, 76, 53, 29, 48, 65, 95, 81, 92, 68, 59, 87, 20, 45, 26, 83, 70, 37, 7, 17, 73, 42, 96, 23, 58, 8, 50, 94, 61};
//        int[] keys = {29, 41, 44, 62, 46};
        for (int key : keys) {
            bTree.insert(key, key * 2);
        }
        System.out.println("B-tree structure:");
        bTree.print(bTree.getRoot());
        System.out.print("key为87的value是：");
        System.out.println(bTree.find(87));

        System.out.println();
        System.out.println("============= Test B-Tree Serializer ============");
        BTreeSerializer bTreeSerializer = new BTreeSerializer();
        String serialized = bTreeSerializer.serialize(bTree.getRoot());
        System.out.print("序列化的b树：");
        System.out.println(serialized);

        BTreeNode deserialized = bTreeSerializer.deserialize(serialized);
        System.out.println("反序列化的b树：");
        new BTree().print(deserialized);

        System.out.println();
        System.out.println("============= Test File Reader ==============");
        String filePath = "./src/com/neu/nosql/io/movies.csv";
        Map<Integer, String> lines = MovieReader.readMoviesFromCSV(filePath);
        System.out.println(lines);

        System.out.println();
        System.out.println("=========== Test File Writer ===============");
        String directory = "./src/com/neu/nosql/io/";
        String fileName = "output.csv";
        String outputPath = directory + "/" + fileName;
        MovieWriter.writeToCSV(lines, outputPath);
    }

    public static void main_command(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        DB db = null;

        while (scanner.hasNextLine()) {
            String cmd = scanner.nextLine();
            String[] tokens = cmd.split("\\s+");

            if (tokens.length == 0) {
                continue;
            }

            switch (tokens[0]) {
                case "open":
                    if (tokens.length != 2) {
                        System.out.println("Usage: open <db_name>");
                        continue;
                    }
                    db = DB.open(tokens[1]);
                    break;
                case "put":
                    if (tokens.length != 2 || db == null) {
                        System.out.println("Usage: put <local_file>");
                        continue;
                    }
                    db.put(tokens[1]);
                    break;
                case "get":
                    if (tokens.length != 2 || db == null) {
                        System.out.println("Usage: get <local_file>");
                        continue;
                    }
                    db.get(tokens[1]);
                    break;
                case "dir":
                    List<String> files = db.dir();
                    for (String file : files) {
                        System.out.println(file);
                    }
                    break;
                case "find":
                    if (tokens.length != 3 || db == null) {
                        System.out.println("Usage: find <local_file> <key>");
                        continue;
                    }
                    int key = Integer.parseInt(tokens[2]);
                    String result = db.find(tokens[1], key);
                    System.out.printf("Value: %s\n", result);
                    break;
                case "kill":
                    if (tokens.length != 2) {
                        System.out.println("Usage: kill <db_name>");
                        continue;
                    }
                    db.kill(tokens[1]);
                    break;
                case "quit":
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Unknown command");
            }
        }
        scanner.close();
    }
}

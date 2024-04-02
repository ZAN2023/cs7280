package com.neu.nosql;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {
    public static String parseInputFileType(String fileName) {
        String type = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            type = fileName.substring(dotIndex + 1).toLowerCase();
        }
        return type;
    }

    public static String parseInputFileName(String fileName) {
        String ans = fileName.substring(0, fileName.lastIndexOf('.'));
        return ans;
    }

    public static boolean isMatchingFile(File file, String dbName) {
        String fileName = file.getName();
        return fileName.matches(dbName + "\\.db\\d+");
    }

    public static boolean validatePut(String fileName) {
        String path = "./src/com/neu/nosql/io/" + fileName;
        if (Files.notExists(Paths.get(path))) {
            System.out.println("Cannot find " + fileName);
            return false;
        }

        if (!Utils.parseInputFileType(fileName).equals("csv")) {
            System.out.println("Unsupported file type.");
            return false;
        }
        return true;
    }
}

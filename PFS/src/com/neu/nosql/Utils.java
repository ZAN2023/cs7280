package com.neu.nosql;

import java.io.File;

public class Utils {
    public static String parseInputFileName(String fileName) {
        String type = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            type = fileName.substring(dotIndex + 1).toLowerCase();
        }
        return type;
    }

    public static String parseInputFileType(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static boolean isMatchingFile(File file, String dbName) {
        String fileName = file.getName();
        return fileName.matches(dbName + "\\.db\\d+");
    }
}

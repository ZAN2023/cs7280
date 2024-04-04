package com.neu.nosql;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class provides utility methods.
 */
public class Utils {
    /**
     * Parse the input file type.
     *
     * @param fileName the file name
     * @return the file type
     */
    public static String parseInputFileType(String fileName) {
        String type = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            type = fileName.substring(dotIndex + 1).toLowerCase();
        }
        return type;
    }

    /**
     * Parse the input file name.
     *
     * @param fileName the file name
     * @return the file name
     */
    public static String parseInputFileName(String fileName) {
        String ans = fileName.substring(0, fileName.lastIndexOf('.'));
        return ans;
    }

/**
     * Check if the file is a matching file.
     *
     * @param file  the file
     * @param dbName the database name
     * @return true if the file is a matching file, false otherwise
     */
    public static boolean isMatchingFile(File file, String dbName) {
        String fileName = file.getName();
        return fileName.matches(dbName + "\\.db\\d+");
    }

    /**
     * Validates the file name for a "put" operation.
     * Checks if the specified file exists and if it has a supported file type (CSV).
     *
     * @param fileName The name of the file to be validated.
     * @return True if the file exists and has a supported file type (CSV), false otherwise.
     */
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

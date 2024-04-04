package com.neu.nosql.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class reads the movie data from the CSV file.
 * It provides a static method to read the movie data and return it as a map.
 * Each line of the CSV file should have three parts: movie ID, movie name, and movie type.
 * The movie ID is used as the key in the map, and the movie name and type are concatenated as the value.
 * If the concatenated value exceeds 40 bytes, it is truncated to fit within the limit.
 */
public class MovieReader {
    private static final int MAX_LENGTH = 40; // 40 bytes

    /**
     * Reads the movie data from the CSV file and returns it as a map.
     *
     * @param filePath the file path of the CSV file
     * @return a map containing the movie data, where the key is the movie ID and the value is the concatenated movie name and type
     */
    public static Map<Integer, String> readMoviesFromCSV(String filePath) {
        Map<Integer, String> lines = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    int movieId = Integer.parseInt(parts[0].trim());
                    String movieName = parts[1].trim();
                    String movieType = parts[2].trim();
                    String val = movieName + "," + movieType;

                    int totalLength = val.getBytes().length;
                    if (totalLength > MAX_LENGTH) {
                        val = truncateString(val);
                    }

                    lines.put(movieId, val);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    /**
     * Truncates the given string to fit within the maximum length of 40 bytes.
     *
     * @param str the string to be truncated
     * @return the truncated string
     */
    private static String truncateString(String str) {
        byte[] bytes = str.getBytes();
        if (bytes.length <= MovieReader.MAX_LENGTH) {
            return str;
        }
        return new String(bytes, 0, MovieReader.MAX_LENGTH);
    }
}

package com.neu.nosql.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MovieReader {
    private static final int MAX_LENGTH = 40; // 40 bytes

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

    private static String truncateString(String str) {
        byte[] bytes = str.getBytes();
        if (bytes.length <= MovieReader.MAX_LENGTH) {
            return str;
        }
        return new String(bytes, 0, MovieReader.MAX_LENGTH);
    }
}

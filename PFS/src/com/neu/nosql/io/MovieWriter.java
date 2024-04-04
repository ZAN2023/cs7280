package com.neu.nosql.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * This class writes the movie data to a CSV file.
 * It provides a static method to write the movie data from a map to a specified file path.
 * Each entry in the map represents a movie, where the key is the movie ID and the value is the concatenated movie name and type.
 * The data is written to the CSV file in the format: movie ID, movie name, movie type.
 */
public class MovieWriter {
    /**
     * Writes the movie data from the given map to the specified CSV file.
     *
     * @param map      the map containing the movie data, where the key is the movie ID and the value is the concatenated movie name and type
     * @param filePath the file path of the CSV file to write the data to
     */
    public static void writeToCSV(Map<Integer, String> map, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write map entries
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                String value = entry.getValue();
                String[] parts = value.split(",", 2);

                writer.write(entry.getKey() + ",");

                if (parts.length == 1) {
                    writer.write(parts[0] + ",");
                } else {
                    writer.write(parts[0] + "," + parts[1]);
                }

                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

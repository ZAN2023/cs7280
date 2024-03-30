package com.neu.nosql.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class MovieWriter {
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

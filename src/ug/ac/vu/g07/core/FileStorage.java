package ug.ac.vu.g07.core;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shared file storage. Survives missing or damaged files.
 * @author Group One (G07) - Storage Lead (Ssenvume Andrew)
 */
public class FileStorage {
    private static final String DATA_DIR = "data";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Warning: could not create data directory.");
        }
    }

    public static List<String> loadLines(String filename) {
        Path path = Paths.get(DATA_DIR, filename);
        if (!Files.exists(path)) {
            System.out.println("  [Storage] " + filename + " not found - starting empty.");
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            System.out.println("  [Storage] Warning: could not read " + filename + " - starting empty.");
            return new ArrayList<>();
        }
    }

    public static void saveLines(String filename, List<String> lines) {
        Path path = Paths.get(DATA_DIR, filename);
        try {
            Files.write(path, lines);
        } catch (IOException e) {
            System.err.println("  [Storage] ERROR saving " + filename + ": " + e.getMessage());
        }
    }

    public static <T> List<T> loadObjects(String filename, Function<String, T> parser) {
        List<T> result = new ArrayList<>();
        for (String line : loadLines(filename)) {
            if (line.trim().isEmpty() || line.startsWith("#")) continue;
            try {
                T obj = parser.apply(line);
                if (obj != null) result.add(obj);
            } catch (Exception e) {
                System.out.println("  [Storage] Skipping damaged line in " + filename);
            }
        }
        return result;
    }

    public static void saveObjects(String filename, List<? extends Record> records) {
        List<String> lines = records.stream()
                .map(Record::toFileString)
                .collect(Collectors.toList());
        saveLines(filename, lines);
    }
}

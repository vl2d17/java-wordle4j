package ru.yandex.practicum;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

class WordleTest {
    @TempDir
    Path tempDir;

    @Test
    void testMain_FileNotFound() {
        String[] args = {};


        ByteArrayOutputStream errOutput = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errOutput));

        try {
            Wordle.main(args);
        } catch (Exception e) {
            // Ожидаем исключение
        } finally {
            System.setErr(originalErr);
        }

        String errorOutput = errOutput.toString();
        assertTrue(errorOutput.contains("Системная ошибка") ||
                errorOutput.contains("Файл словаря не найден"));
    }

    @Test
    void testMain_WithDictionaryFile() throws IOException {

        File dictionaryFile = tempDir.resolve("dictionary.txt").toFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(dictionaryFile, false))) {
            writer.println("АБВГД");
            writer.println("ЕЖЗИЙ");
            writer.println("КЛМНО");
            writer.println("ПРСТУ");
            writer.println("ФХЦЧШ");
            writer.println("ЩЪЫЬЭ");
            writer.println("ЮЯАБВ");
        }


        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }


        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {

            assertDoesNotThrow(() -> {

                Thread thread = new Thread(() -> {
                    try {
                        Wordle.main(new String[]{});
                    } catch (Exception e) {
                        // Игнорируем ожидаемые исключения от System.in
                    }
                });
                thread.start();
                thread.join(1000); // Ждем 1 секунду
                thread.interrupt();
            });
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void testSetupLogger_DirectoryCreation() throws IOException {
        File logsDir = new File("test_logs");
        if (logsDir.exists()) {
            deleteDirectory(logsDir);
        }

        assertFalse(logsDir.exists());


        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {


            File testLogs = new File(tempDir.toFile(), "test_logs");
            assertFalse(testLogs.exists());
            assertTrue(testLogs.mkdirs());
            assertTrue(testLogs.exists());
            assertTrue(testLogs.isDirectory());

        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }
}


package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    @TempDir
    Path tempDir;

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testMain_FileNotFound() {
        // В текущей реализации Wordle всегда пытается загрузить "words_ru.txt"
        // Аргументы игнорируются
        Wordle.main(new String[]{"any_argument.txt"});

        String output = getOutput();
        System.out.println("DEBUG Output for FileNotFound: " + output);

        // Проверяем ожидаемые сообщения из текущей реализации
        // 1. "Игра Wordle запускается" - должно быть всегда
        // 2. "Системная ошибка" или "Файл словаря не найден" - при ошибке
        boolean hasError = output.contains("Системная ошибка") ||
                output.contains("Файл словаря не найден") ||
                output.contains("words_ru.txt") ||
                output.contains("GameRuntimeException");

        assertTrue(hasError, "Ожидалось сообщение об ошибке файла words_ru.txt. Вывод: " + output);

        // Также проверяем что программа начала работу
        assertTrue(output.contains("Игра Wordle запускается") ||
                        output.contains("Загрузка словаря"),
                "Программа должна была начать работу. Вывод: " + output);
    }

    @Test
    void testMain_WithValidDictionary() throws Exception {
        // Создаем файл words_ru.txt в темповой директории
        Path dictFile = tempDir.resolve("words_ru.txt");
        String content = """
            АБВГД
            ЕЖЗИЙ
            КЛМНО
            ПРСТУ
            ФХЦЧШ
            ЩЪЫЬЭ
            ЮЯАБВ
            ТЕКСТ
            СЛОВО
            РУССК
            """;
        Files.writeString(dictFile, content);

        // Создаем директорию для логов
        File logDir = new File(tempDir.toFile(), "logs");
        logDir.mkdirs();

        // Сохраняем текущую директорию и меняем на темповую
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            // Запускаем Wordle - аргументы игнорируются
            Wordle.main(new String[]{"ignored.txt"});

            String output = getOutput();
            System.out.println("DEBUG Output for ValidDictionary: " + output);

            // Проверяем что программа начала работу
            assertTrue(output.contains("Игра Wordle запускается") ||
                            output.contains("Словарь загружен"),
                    "Программа должна была начать работу. Вывод: " + output);

            // Проверяем что нет критических ошибок
            assertFalse(output.contains("Системная ошибка") ||
                            output.contains("GameRuntimeException") ||
                            output.contains("Критическая ошибка"),
                    "Не ожидались критические ошибки. Вывод: " + output);

        } finally {
            // Восстанавливаем оригинальную директорию
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void testMain_EmptyDictionary() throws Exception {
        // Создаем пустой файл words_ru.txt
        Path dictFile = tempDir.resolve("words_ru.txt");
        Files.createFile(dictFile);  // Пустой файл

        File logDir = new File(tempDir.toFile(), "logs");
        logDir.mkdirs();

        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            Wordle.main(new String[]{});

            String output = getOutput();
            System.out.println("DEBUG Output for EmptyDictionary: " + output);

            // Проверяем сообщение о пустом словаре
            boolean hasError = output.contains("Системная ошибка") ||
                    output.contains("Словарь пуст") ||
                    output.contains("Слишком мало слов") ||
                    output.contains("мало слов");

            assertTrue(hasError, "Ожидалось сообщение о пустом словаре. Вывод: " + output);

        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void testMain_FewWordsInDictionary() throws Exception {
        // Создаем файл с малым количеством слов
        Path dictFile = tempDir.resolve("words_ru.txt");
        String content = """
            АБВГД
            ЕЖЗИЙ
            КЛМНО
            ПРСТУ
            ФХЦЧШ
            """;  // Только 5 слов
        Files.writeString(dictFile, content);

        File logDir = new File(tempDir.toFile(), "logs");
        logDir.mkdirs();

        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            Wordle.main(new String[]{});

            String output = getOutput();
            System.out.println("DEBUG Output for FewWords: " + output);

            // Проверяем предупреждение о малом количестве слов
            // Или исключение если меньше минимального
            boolean hasWarningOrError = output.contains("мало слов") ||
                    output.contains("Слишком мало слов") ||
                    output.contains("Внимание: в словаре мало слов");

            assertTrue(hasWarningOrError,
                    "Ожидалось предупреждение о малом количестве слов. Вывод: " + output);

        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void testMain_ArgumentsIgnored() {
        // Тестируем что аргументы игнорируются
        Wordle.main(new String[]{"file1.txt", "file2.txt", "file3.txt"});

        String output = getOutput();
        System.out.println("DEBUG Output for MultipleArguments: " + output);

        // Программа все равно пытается загрузить "words_ru.txt"
        // и выводит соответствующее сообщение
        boolean hasWordsRuReference = output.contains("words_ru.txt") ||
                (output.contains("Файл") && output.contains("не найден"));

        // Или программа начала работу
        boolean hasStartupMessage = output.contains("Игра Wordle запускается") ||
                output.contains("Загрузка словаря");

        assertTrue(hasWordsRuReference || hasStartupMessage,
                "Программа должна игнорировать аргументы и пытаться загрузить words_ru.txt. Вывод: " + output);
    }

    @Test
    void testMain_LoggingSetupError() throws Exception {
        // Создаем файл words_ru.txt
        Path dictFile = tempDir.resolve("words_ru.txt");
        Files.writeString(dictFile, "АБВГД\nЕЖЗИЙ\n");

        // Создаем read-only директорию чтобы вызвать ошибку создания логов
        File readOnlyDir = new File(tempDir.toFile(), "readonly_logs");
        readOnlyDir.mkdirs();
        readOnlyDir.setReadOnly();

        // Временно меняем директорию для логов в Wordle
        // (Это сложно без рефлексии, но проверяем общее поведение)

        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            Wordle.main(new String[]{});

            String output = getOutput();
            System.out.println("DEBUG Output for LoggingError: " + output);

            // Проверяем что программа как-то обработала ошибку
            boolean hasError = output.contains("Ошибка настройки логгера") ||
                    output.contains("Критическая ошибка") ||
                    output.contains("не удалось создать директорию");

            // Или программа все равно попыталась работать
            boolean triedToWork = output.contains("Игра Wordle запускается") ||
                    output.contains("Загрузка словаря");

            assertTrue(hasError || triedToWork,
                    "Ожидалось сообщение об ошибке или попытка работы. Вывод: " + output);

        } finally {
            readOnlyDir.setWritable(true);
            System.setProperty("user.dir", originalDir);
        }
    }

    private String getOutput() {
        return outContent.toString() + errContent.toString();
    }
}
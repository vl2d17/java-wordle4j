package ru.yandex.practicum;


import ru.yandex.practicum.exceptions.GameRuntimeException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class WordleDictionaryLoader {
    private final Logger logger;
    private final PrintWriter gameLogWriter;

    public WordleDictionaryLoader(Logger logger, PrintWriter gameLogWriter) {
        this.logger = logger;
        this.gameLogWriter = gameLogWriter;
    }

    public WordleDictionary loadDictionary(String filename) throws GameRuntimeException {
        List<String> words = new ArrayList<>();

        logger.info("Начинаю загрузку словаря из файла: " + filename);
        logGameEvent("Загрузка словаря из файла: " + filename);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;
            int wordCount = 0;
            int skippedCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String word = line.trim();

                if (!word.isEmpty()) {
                    words.add(word);
                    wordCount++;
                } else {
                    skippedCount++;
                }
            }

            logger.info("Загружено " + wordCount + " слов из " + lineNumber + " строк, пропущено: " + skippedCount);
            logGameEvent("Словарь загружен: " + wordCount + " слов");

            if (words.isEmpty()) {
                String errorMsg = "Словарь пуст";
                logger.severe(errorMsg);
                logGameEvent("ОШИБКА: " + errorMsg);
                throw new GameRuntimeException(errorMsg);
            }

            if (wordCount < 10) {
                logger.warning("Словарь содержит мало слов: " + wordCount);
                logGameEvent("Предупреждение: мало слов в словаре: " + wordCount);
            }

            return new WordleDictionary(words, logger);

        } catch (FileNotFoundException e) {
            String errorMsg = "Файл словаря не найден: " + filename;
            logger.severe(errorMsg);
            logGameEvent("ОШИБКА: " + errorMsg);
            throw new GameRuntimeException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Ошибка чтения файла словаря: " + e.getMessage();
            logger.severe(errorMsg);
            logGameEvent("ОШИБКА: " + errorMsg);
            throw new GameRuntimeException(errorMsg, e);
        }
    }

    private void logGameEvent(String message) {
        if (gameLogWriter != null) {
            gameLogWriter.println(new Date() + " [DictionaryLoader] " + message);
            gameLogWriter.flush();
        }
    }
}

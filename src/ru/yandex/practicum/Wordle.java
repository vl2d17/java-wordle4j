package ru.yandex.practicum;


import ru.yandex.practicum.exceptions.GameRuntimeException;

import java.io.*;
import java.util.logging.*;


public class Wordle {
    private static final Logger logger = Logger.getLogger(Wordle.class.getName());
    private static PrintWriter gameLogWriter;

    public static void main(String[] args) {
        try {
            setupLogger();
            logger.info("Игра Wordle запускается");

            // Создание загрузчика словарей с передачей лог-файла
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logger, gameLogWriter);

            // Загрузка словаря
            logger.info("Загрузка словаря...");
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");
            logger.info("Словарь загружен. Слов для игры: " + dictionary.getGameWordsCount());

            // Проверка на минимальное количество слов
            if (dictionary.getGameWordsCount() < 10) {
                logger.warning("Мало слов в словаре: " + dictionary.getGameWordsCount());
                System.out.println("Внимание: в словаре мало слов для интересной игры.");
            }

            // Создание и запуск игры
            WordleGame game = new WordleGame(dictionary, logger, gameLogWriter);
            game.play();

        } catch (GameRuntimeException e) {
            // Системные ошибки - логируем и показываем пользователю
            logger.severe("Системная ошибка игры: " + e.getMessage());
            System.err.println("Системная ошибка: " + e.getMessage());
            System.err.println("Проверьте наличие файла dictionary.txt и права доступа.");
        } catch (Exception e) {
            // Все остальные исключения
            logger.severe("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Произошла непредвиденная ошибка. Подробности в лог-файлах.");
        } finally {
            // Гарантированное закрытие ресурсов
            closeResources();
            logger.info("✋bye bye✋");
        }
    }

    private static void setupLogger() throws IOException {
        try {
            // Настройка стандартного логгера
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Создаем директорию для логов, если её нет
            File logDir = new File("logs");
            if (!logDir.exists() && !logDir.mkdirs()) {
                throw new IOException("Не удалось создать директорию для логов");
            }

            // Логгер для системных событий
            FileHandler fileHandler = new FileHandler("logs/wordle_system.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            // Консольный вывод только для INFO и выше
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%s] %s%n",
                            record.getLevel().getName(), record.getMessage());
                }
            });
            consoleHandler.setLevel(Level.INFO);

            logger.setLevel(Level.ALL);
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);

            // Лог-файл для игровых событий (через PrintWriter)
            gameLogWriter = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream("logs/wordle_game.log", true),
                            "UTF-8"));

            logger.info("Логгеры успешно настроены");

        } catch (IOException e) {
            System.err.println("Ошибка настройки логгера: " + e.getMessage());
            throw e;
        }
    }

    private static void closeResources() {
        if (gameLogWriter != null) {
            try {
                gameLogWriter.close();
            } catch (Exception e) {
                System.err.println("Ошибка при закрытии лог-файла: " + e.getMessage());
            }
        }
    }
}






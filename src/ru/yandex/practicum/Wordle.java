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

            // Проверка аргументов командной строки
            if (args.length == 0) {
                System.err.println("Ошибка: не указан файл словаря.");
                System.err.println("Использование: java Wordle <файл_словаря>");
                System.err.println("Пример: java Wordle dictionary.txt");
                return;
            }

            String dictionaryFile = args[0];
            logger.info("Используется файл словаря: " + dictionaryFile);

            WordleDictionaryLoader loader = new WordleDictionaryLoader(logger, gameLogWriter);

            logger.info("Загрузка словаря...");
            WordleDictionary dictionary = loader.loadDictionary(dictionaryFile);
            logger.info("Словарь загружен. Слов для игры: " + dictionary.getGameWordsCount());

            if (dictionary.getGameWordsCount() < 10) {
                logger.warning("Мало слов в словаре: " + dictionary.getGameWordsCount());
                System.out.println("Внимание: в словаре мало слов для интересной игры.");
            }

            WordleGame game = new WordleGame(dictionary, logger, gameLogWriter);

            // Проверка на тестовый режим
            boolean isTestMode = System.getProperty("test.mode") != null;
            if (isTestMode) {
                System.out.println("Тестовый режим: игра запущена успешно");
                logger.info("Тестовый режим: игра не запускается интерактивно");
            } else {
                game.play();
            }

        } catch (GameRuntimeException e) {
            logger.severe("Системная ошибка игры: " + e.getMessage());
            System.err.println("Системная ошибка: " + e.getMessage());
            System.err.println("Проверьте наличие файла словаря и права доступа.");
        } catch (Exception e) {
            logger.severe("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Произошла непредвиденная ошибка. Подробности в лог-файлах.");
        } finally {
            closeResources();
            logger.info("✋bye bye✋");
        }
    }

    private static void setupLogger() throws IOException {
        try {
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            File logDir = new File("logs");
            if (!logDir.exists() && !logDir.mkdirs()) {
                throw new IOException("Не удалось создать директорию для логов: " + logDir.getAbsolutePath());
            }

            FileHandler fileHandler = new FileHandler("logs/wordle_system.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

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

            gameLogWriter = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream("logs/wordle_game.log", true),
                            "UTF-8"));

            logger.info("Логгеры успешно настроены");

        } catch (IOException e) {
            System.err.println("Ошибка настройки логгера: " + e.getMessage());
            // Не можем переприсвоить final logger, просто продолжаем с текущим
            if (gameLogWriter == null) {
                gameLogWriter = new PrintWriter(System.out); // Используем консоль как fallback
            }
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
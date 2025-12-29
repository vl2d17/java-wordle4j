package ru.yandex.practicum;

import org.junit.jupiter.api.Test;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class WordleGameTest {


    private List<String> createTestWords() {
        return Arrays.asList(
                "книга", "ручка", "ветер", "машина", "солнце",
                "пятно", "город", "речка", "лесок", "поле",
                "море", "песня", "домок", "травы", "цветы"
        );
    }

    @Test
    void testConstructorThrowsExceptionWithNullDictionary() {
        Logger logger = Logger.getLogger("test");
        PrintWriter writer = new PrintWriter(new StringWriter());

        Exception exception = assertThrows(NullPointerException.class, () -> {
            new WordleGame(null, logger, writer);
        });

        assertTrue(exception.getMessage().contains("Словарь не может быть null"));
    }

    @Test
    void testConstructorThrowsExceptionWithNullLogger() {
        List<String> words = createTestWords();
        WordleDictionary dictionary = new WordleDictionary(words, Logger.getLogger("dict"));
        PrintWriter writer = new PrintWriter(new StringWriter());

        Exception exception = assertThrows(NullPointerException.class, () -> {
            new WordleGame(dictionary, null, writer);
        });

        assertTrue(exception.getMessage().contains("Логгер не может быть null"));
    }

    @Test
    void testConstructorWithValidParameters() {
        List<String> words = createTestWords();
        WordleDictionary dictionary = new WordleDictionary(words, Logger.getLogger("dict"));
        Logger logger = Logger.getLogger("test");
        PrintWriter writer = new PrintWriter(new StringWriter());

        WordleGame game = new WordleGame(dictionary, logger, writer);
        assertNotNull(game);
    }

    @Test
    void testGetRemainingAttemptsInitialValue() {
        List<String> words = createTestWords();
        WordleDictionary dictionary = new WordleDictionary(words, Logger.getLogger("dict"));
        Logger logger = Logger.getLogger("test");
        PrintWriter writer = new PrintWriter(new StringWriter());
        WordleGame game = new WordleGame(dictionary, logger, writer);


        int attempts = game.getRemainingAttempts();
        System.out.println("Remaining attempts: " + attempts);

        assertTrue(attempts >= 0, "Количество попыток не должно быть отрицательным: " + attempts);


    }

    @Test
    void testIsGameWonInitiallyFalse() {
        List<String> words = createTestWords();
        WordleDictionary dictionary = new WordleDictionary(words, Logger.getLogger("dict"));
        Logger logger = Logger.getLogger("test");
        PrintWriter writer = new PrintWriter(new StringWriter());
        WordleGame game = new WordleGame(dictionary, logger, writer);


        boolean isWon = game.isGameWon();
        System.out.println("Is game won initially: " + isWon);

        assertFalse(isWon);
    }

    @Test
    void testGetGameHistoryInitiallyEmpty() {
        List<String> words = createTestWords();
        WordleDictionary dictionary = new WordleDictionary(words, Logger.getLogger("dict"));
        Logger logger = Logger.getLogger("test");
        PrintWriter writer = new PrintWriter(new StringWriter());
        WordleGame game = new WordleGame(dictionary, logger, writer);


        System.out.println("Game history size: " + game.getGameHistory().size());

        assertTrue(game.getGameHistory().isEmpty());
    }

    @Test
    void testGetGameHistoryReturnsUnmodifiableList() {
        List<String> words = createTestWords();
        WordleDictionary dictionary = new WordleDictionary(words, Logger.getLogger("dict"));
        Logger logger = Logger.getLogger("test");
        PrintWriter writer = new PrintWriter(new StringWriter());
        WordleGame game = new WordleGame(dictionary, logger, writer);

        assertThrows(UnsupportedOperationException.class, () -> {
            game.getGameHistory().add(null);
        });
    }


    @Test
    void testGameStateAfterInitialization() {
        List<String> words = createTestWords();
        WordleDictionary dictionary = new WordleDictionary(words, Logger.getLogger("dict"));
        Logger logger = Logger.getLogger("test");
        PrintWriter writer = new PrintWriter(new StringWriter());
        WordleGame game = new WordleGame(dictionary, logger, writer);


        System.out.println("=== Game State ===");
        System.out.println("Remaining attempts: " + game.getRemainingAttempts());
        System.out.println("Is game won: " + game.isGameWon());
        System.out.println("Game history size: " + game.getGameHistory().size());


        assertNotNull(game);
        assertFalse(game.isGameWon());
        assertTrue(game.getGameHistory().isEmpty());
    }
}
package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exceptions.GameRuntimeException;
import ru.yandex.practicum.models.LetterResult;
import ru.yandex.practicum.models.WordComparisonResult;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {
    private WordleDictionary dictionary;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger(WordleDictionaryTest.class.getName());


        List<String> words = Arrays.asList(
                "АБВГД",
                "ЕЖЗИЙ",
                "КЛМНО",
                "ПРСТУ",
                "ФХЦЧШ",
                "ЩЪЫЬЭ"
        );

        dictionary = new WordleDictionary(words, logger);
    }


    @Test
    void testDictionaryCreation() {
        assertNotNull(dictionary);
        assertTrue(dictionary.getGameWordsCount() >= 5);
    }


    @Test
    void testTooFewWords() {
        List<String> fewWords = Arrays.asList("АБВГД", "ЕЖЗИЙ", "КЛМНО");
        assertThrows(GameRuntimeException.class, () ->
                new WordleDictionary(fewWords, logger));
    }


    @Test
    void testNormalizeWord() {
        assertEquals("абвгд", WordleDictionary.normalizeWord("АБВГД"));
        assertEquals("абвгд", WordleDictionary.normalizeWord("абвгд"));
        assertEquals("елка", WordleDictionary.normalizeWord("ёлка"));
        assertEquals("", WordleDictionary.normalizeWord(null));
        assertEquals("", WordleDictionary.normalizeWord("   "));
    }


    @Test
    void testGetRandomWord() {
        String word = dictionary.getRandomWord();
        assertNotNull(word);
        assertEquals(5, word.length());
        assertTrue(dictionary.getGameWords().contains(word));
    }


    @Test
    void testIsValidGameWord() {
        assertTrue(dictionary.isValidGameWord("АБВГД"));
        assertTrue(dictionary.isValidGameWord("абвгд"));
        assertFalse(dictionary.isValidGameWord("МАМА")); // 4 буквы
        assertFalse(dictionary.isValidGameWord("12345")); // цифры
        assertFalse(dictionary.isValidGameWord(null)); // null
    }


    @Test
    void testCompareWords_AllCorrect() {
        WordComparisonResult result = dictionary.compareWords("абвгд", "абвгд");

        assertTrue(result.isCorrect());
        assertEquals("✓✓✓✓✓", result.getAnalysis());

        // Проверяем, что все буквы отмечены как правильные
        for (LetterResult letterResult : result.getLetterResults()) {
            assertEquals(LetterResult.CORRECT_POSITION, letterResult);
        }
    }


    @Test
    void testGetAllWords() {
        List<String> allWords = dictionary.getAllWords();
        assertEquals(6, allWords.size()); // 6 слов в нашем тестовом словаре
    }


    @Test
    void testEmptyDictionary() {
        List<String> emptyList = Collections.emptyList();
        assertThrows(GameRuntimeException.class, () ->
                new WordleDictionary(emptyList, logger));
    }
}
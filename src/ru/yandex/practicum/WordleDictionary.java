package ru.yandex.practicum;


import ru.yandex.practicum.exceptions.GameRuntimeException;
import ru.yandex.practicum.models.WordComparisonResult;
import ru.yandex.practicum.models.LetterResult;

import java.util.*;
import java.util.logging.Logger;

import static ru.yandex.practicum.models.LetterResult.*;


public class WordleDictionary {
    private final List<String> words;
    private final List<String> gameWords;
    private final Logger logger;
    private final Set<Character> russianAlphabet;

    public WordleDictionary(List<String> words, Logger logger) {
        this.logger = logger;
        this.words = Collections.unmodifiableList(words);
        this.russianAlphabet = initializeRussianAlphabet();
        this.gameWords = prepareGameWords();
        logger.info("Подготовлено " + gameWords.size() + " слов длиной 5 букв");

        // Проверка на достаточное количество слов
        if (gameWords.size() < 5) {
            throw new GameRuntimeException("Слишком мало слов для игры: " + gameWords.size());
        }
    }

    private Set<Character> initializeRussianAlphabet() {
        Set<Character> alphabet = new HashSet<>();
        for (char c = 'а'; c <= 'я'; c++) {
            alphabet.add(c);
        }
        alphabet.add('ё');
        return Collections.unmodifiableSet(alphabet);
    }

    private List<String> prepareGameWords() {
        List<String> result = new ArrayList<>();
        Set<String> uniqueWords = new HashSet<>(); // Для устранения дубликатов

        for (String word : words) {
            String processed = normalizeWord(word);
            if (processed.length() == 5 &&
                    isValidRussianWord(processed) &&
                    !uniqueWords.contains(processed)) {
                result.add(processed);
                uniqueWords.add(processed);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static String normalizeWord(String word) {
        if (word == null) return "";
        return word.toLowerCase()
                .replace('ё', 'е')
                .trim();
    }

    private boolean isValidRussianWord(String word) {
        for (char c : word.toCharArray()) {
            if (!russianAlphabet.contains(c)) {
                return false;
            }
        }
        return true;
    }

    public List<String> getAllWords() {
        return words;
    }

    public List<String> getGameWords() {
        return gameWords;
    }

    public int getGameWordsCount() {
        return gameWords.size();
    }

    public String getRandomWord() {
        if (gameWords.isEmpty()) {
            throw new GameRuntimeException("Нет подходящих слов для игры");
        }
        Random random = new Random();
        return gameWords.get(random.nextInt(gameWords.size()));
    }

    public boolean isValidGameWord(String word) {
        String normalized = normalizeWord(word);
        return normalized.length() == 5 &&
                isValidRussianWord(normalized) &&
                gameWords.contains(normalized);
    }

    public WordComparisonResult compareWords(String guess, String answer) {
        if (guess == null || answer == null) {
            throw new IllegalArgumentException("Слова не могут быть null");
        }
        if (guess.length() != 5 || answer.length() != 5) {
            throw new IllegalArgumentException("Слова должны быть длиной 5 букв");
        }

        char[] answerChars = answer.toCharArray();
        char[] guessChars = guess.toCharArray();
        boolean[] answerUsed = new boolean[5];
        LetterResult[] results = new LetterResult[5];
        StringBuilder analysis = new StringBuilder();

        // Первый проход: правильные позиции
        for (int i = 0; i < 5; i++) {
            if (guessChars[i] == answerChars[i]) {
                results[i] = CORRECT_POSITION;
                answerUsed[i] = true;
                analysis.append("✓");
            } else {
                analysis.append(" ");
            }
        }

        // Второй проход: правильные буквы на неправильных позициях
        for (int i = 0; i < 5; i++) {
            if (results[i] == CORRECT_POSITION) {
                continue;
            }

            boolean found = false;
            for (int j = 0; j < 5; j++) {
                if (!answerUsed[j] && guessChars[i] == answerChars[j]) {
                    found = true;
                    answerUsed[j] = true;
                    break;
                }
            }

            if (found) {
                results[i] = WRONG_POSITION;
                analysis.setCharAt(i, '~');
            } else {
                results[i] = NOT_FOUND;
                analysis.setCharAt(i, '✗');
            }
        }

        boolean correct = guess.equals(answer);
        return new WordComparisonResult(
                Arrays.asList(results),
                analysis.toString(),
                correct
        );
    }

    public List<String> getPossibleWords(List<WordComparisonResult> previousResults) {
        // Оптимизация: кеширование результатов
        if (previousResults.isEmpty()) {
            return new ArrayList<>(gameWords);
        }

        List<String> candidates = new ArrayList<>(gameWords);

        for (WordComparisonResult result : previousResults) {
            candidates = filterCandidatesOptimized(candidates, result);
            if (candidates.isEmpty()) {
                break; // Нет смысла продолжать
            }
        }

        return candidates;
    }

    // Оптимизированная версия фильтрации
    private List<String> filterCandidatesOptimized(List<String> candidates, WordComparisonResult result) {
        List<String> filtered = new ArrayList<>();
        String guess = result.getGuess();
        List<LetterResult> letterResults = result.getLetterResults();

        // Предварительные вычисления
        Map<Character, Integer> exactPositions = new HashMap<>();
        Map<Character, Integer> wrongPositions = new HashMap<>();
        Set<Character> notPresent = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            char letter = guess.charAt(i);
            switch (letterResults.get(i)) {
                case CORRECT_POSITION:
                    exactPositions.putIfAbsent(letter, i);
                    break;
                case WRONG_POSITION:
                    wrongPositions.put(letter, wrongPositions.getOrDefault(letter, 0) + 1);
                    break;
                case NOT_FOUND:
                    notPresent.add(letter);
                    break;
            }
        }

        // Фильтрация кандидатов
        for (String candidate : candidates) {
            if (matchesAllConditions(candidate, guess, exactPositions, wrongPositions, notPresent, letterResults)) {
                filtered.add(candidate);
            }
        }

        return filtered;
    }

    private boolean matchesAllConditions(String candidate, String guess,
                                         Map<Character, Integer> exactPositions,
                                         Map<Character, Integer> wrongPositions,
                                         Set<Character> notPresent,
                                         List<LetterResult> letterResults) {
        // Проверка точных позиций
        for (Map.Entry<Character, Integer> entry : exactPositions.entrySet()) {
            char letter = entry.getKey();
            int position = entry.getValue();
            if (candidate.charAt(position) != letter) {
                return false;
            }
        }

        // Подсчет букв в кандидате для проверки минимальных количеств
        Map<Character, Integer> candidateCounts = new HashMap<>();
        for (char c : candidate.toCharArray()) {
            candidateCounts.put(c, candidateCounts.getOrDefault(c, 0) + 1);
        }

        // Проверка букв на неправильных позициях
        for (Map.Entry<Character, Integer> entry : wrongPositions.entrySet()) {
            char letter = entry.getKey();
            int requiredCount = entry.getValue();

            // Буква не может быть на тех же позициях, где она отмечена как неправильная
            for (int i = 0; i < 5; i++) {
                if (letterResults.get(i) == WRONG_POSITION &&
                        guess.charAt(i) == letter &&
                        candidate.charAt(i) == letter) {
                    return false;
                }
            }

            // Проверка минимального количества
            int actualCount = candidateCounts.getOrDefault(letter, 0);
            if (actualCount < requiredCount) {
                return false;
            }
        }

        // Проверка отсутствующих букв (но только если они не отмечены как правильные в других позициях)
        for (char letter : notPresent) {
            if (candidateCounts.containsKey(letter)) {
                // Проверяем, не является ли эта буква правильной в другой позиции
                boolean isCorrectElsewhere = false;
                for (int i = 0; i < 5; i++) {
                    if (letterResults.get(i) == CORRECT_POSITION &&
                            guess.charAt(i) == letter) {
                        isCorrectElsewhere = true;
                        break;
                    }
                }

                if (!isCorrectElsewhere) {
                    return false;
                }
            }
        }

        return true;
    }
}



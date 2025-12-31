package ru.yandex.practicum;


import ru.yandex.practicum.exceptions.GameRuntimeException;
import ru.yandex.practicum.exceptions.InvalidWordException;
import ru.yandex.practicum.exceptions.WordNotFoundException;
import ru.yandex.practicum.models.LetterResult;
import ru.yandex.practicum.models.WordComparisonResult;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.NoSuchElementException;

public class WordleGame {
    private String answer;
    private int remainingAttempts;
    private final WordleDictionary dictionary;
    private final Logger logger;
    private final PrintWriter gameLogWriter;
    private final List<WordComparisonResult> gameHistory;
    private boolean gameWon;
    private static final int MAX_ATTEMPTS = 6;
    private Scanner scanner;
    private final Set<String> previousHints;

    public WordleGame(WordleDictionary dictionary, Logger logger, PrintWriter gameLogWriter) {
        this.dictionary = Objects.requireNonNull(dictionary, "Словарь не может быть null");
        this.logger = Objects.requireNonNull(logger, "Логгер не может быть null");
        this.gameLogWriter = gameLogWriter;
        this.gameHistory = new ArrayList<>();
        this.previousHints = new HashSet<>();
        logGameEvent("Создан новый экземпляр игры");
    }

    public void play() {
        this.scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            boolean playAgain = true;

            while (playAgain) {
                startNewGame();
                displayWelcomeMessage();


                while (remainingAttempts > 0 && !gameWon) {
                    displayGameStatus();

                    try {
                        String input = getPlayerInput();

                        if ("выход".equals(input)) {
                            System.out.println("\nИгра прервана пользователем");
                            return;
                        }

                        if (input.isEmpty()) {
                            giveHint();
                        } else {
                            processPlayerGuess(input);
                            remainingAttempts--;

                            if (!gameHistory.isEmpty()) {
                                WordComparisonResult lastResult = gameHistory.get(gameHistory.size() - 1);
                                System.out.println("Результат: " + lastResult.getAnalysis());
                            }
                        }

                    } catch (WordNotFoundException | InvalidWordException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                        logGameEvent("Ошибка ввода: " + e.getMessage());
                    }
                }

                displayFinalResult();
                playAgain = askToPlayAgain();
            }
        } finally {
            cleanup();
        }
        logGameEvent("Игра завершена пользователем");
    }

    private void startNewGame() {
        this.answer = dictionary.getRandomWord();
        this.remainingAttempts = MAX_ATTEMPTS;
        this.gameWon = false;
        this.gameHistory.clear();
        this.previousHints.clear();

        logger.info("Начата новая игра. Загаданное слово: " + answer);
        logGameEvent("Новая игра. Загаданное слово: " + answer);


        if (answer == null || answer.length() != 5) {
            throw new GameRuntimeException("Некорректное загаданное слово: " + answer);
        }
    }

    private void displayWelcomeMessage() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("            ДОБРО ПОЖАЛОВАТЬ В WORDLE!");
        System.out.println("=".repeat(50));
        System.out.println("Угадайте слово из 5 русских букв за " + MAX_ATTEMPTS + " попыток.");
        System.out.println("\nСимволы подсказок:");
        System.out.println("  ✓ - буква на правильном месте");
        System.out.println("  ~ - буква есть в слове, но на другом месте");
        System.out.println("  ✗ - буквы нет в слове");
        System.out.println("\nНажмите Enter без ввода для подсказки.");
        System.out.println("=".repeat(50));
    }

    private void displayGameStatus() {
        System.out.println("\n" + "-".repeat(30));
        System.out.println("Осталось попыток: " + remainingAttempts);

        if (!gameHistory.isEmpty()) {
            System.out.println("\nИстория попыток:");
            for (int i = 0; i < gameHistory.size(); i++) {
                WordComparisonResult result = gameHistory.get(i);
                System.out.printf("%d. %s -> %s%n",
                        i + 1,
                        result.getGuess().toUpperCase(),
                        result.getAnalysis());
            }
        }
    }

    private String getPlayerInput() {
        System.out.print("\nВведите слово (5 букв): ");
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            // Пользователь закрыл ввод (Ctrl+D)
            System.out.println("\nВвод завершен пользователем");
            return "выход";
        }
    }

    public void processPlayerGuess(String input) throws WordNotFoundException, InvalidWordException {
        // Проверка на null
        if (input == null) {
            throw new InvalidWordException("Ввод не может быть null");
        }
        String normalizedInput = WordleDictionary.normalizeWord(input);
        String normalizedAnswer = WordleDictionary.normalizeWord(answer);

        // Быстрая проверка на победу (без учета регистра)
        if (normalizedInput.equals(normalizedAnswer)) {
            WordComparisonResult result = new WordComparisonResult(
                    Collections.nCopies(5, LetterResult.CORRECT_POSITION),
                    "✓✓✓✓✓",
                    true
            );
            result.setGuess(input);
            gameHistory.add(result);
            gameWon = true;
            logGameEvent("Игрок угадал слово: " + input);
            return;
        }


        WordValidator.validate(input);


        if (!dictionary.isValidGameWord(normalizedInput)) {
            throw new WordNotFoundException("Слово '" + input + "' не найдено в словаре");
        }


        WordComparisonResult result = dictionary.compareWords(normalizedInput, normalizedAnswer);
        result.setGuess(input);
        gameHistory.add(result);


        if (result.isCorrect()) {
            gameWon = true;
        }

        logGameEvent(String.format("Попытка %d: %s -> %s (осталось попыток: %d)",
                gameHistory.size(), input, result.getAnalysis(), remainingAttempts));
    }

    private void giveHint() {
        List<String> possibleWords = dictionary.getPossibleWords(gameHistory);

        if (possibleWords.isEmpty()) {
            System.out.println("\n⚠ Нет подходящих слов по текущим подсказкам!");
            System.out.println("Проверьте правильность введённых слов.");
            logGameEvent("Запрошена подсказка: нет возможных слов");
            return;
        }


        List<String> newHints = new ArrayList<>();
        for (String word : possibleWords) {
            if (!previousHints.contains(word)) {
                newHints.add(word);
            }
        }


        if (newHints.isEmpty()) {
            newHints = possibleWords;
            previousHints.clear();
        }


        Random random = new Random();
        String hint = newHints.get(random.nextInt(newHints.size()));
        previousHints.add(hint);

        displayHint(hint, possibleWords.size());
        logGameEvent("Показана подсказка: " + hint + " (возможных слов: " + possibleWords.size() + ")");
    }

    private void displayHint(String hint, int totalPossible) {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("🎯 ПОДСКАЗКА");
        System.out.println("=".repeat(30));
        System.out.println("Возможное слово: " + hint.toUpperCase());
        System.out.println("Всего возможных слов: " + totalPossible);

        if (!gameHistory.isEmpty()) {
            System.out.println("\nИзвестная информация:");
            WordComparisonResult lastResult = gameHistory.get(gameHistory.size() - 1);
            System.out.println("Последняя попытка: " + lastResult.getAnalysis());
        }
        System.out.println("=".repeat(30));
    }

    private void displayFinalResult() {
        System.out.println("\n" + "=".repeat(50));

        if (gameWon) {
            System.out.println("🎉 ВЫ ВЫИГРАЛИ! 🎉");
            System.out.println("Загаданное слово: " + answer.toUpperCase());
            System.out.println("Вы угадали за " + gameHistory.size() + " попыток!");
            logGameEvent("Игра завершена победой за " + gameHistory.size() + " попыток");
        } else {
            System.out.println("💔 ВЫ ПРОИГРАЛИ 💔");
            System.out.println("Загаданное слово было: " + answer.toUpperCase());
            logGameEvent("Игра завершена поражением. Слово: " + answer);
        }

        System.out.println("=".repeat(50));
    }

    private boolean askToPlayAgain() {
        while (true) {
            System.out.print("\nХотите сыграть еще раз? (да/нет): ");
            try {
                String choice = scanner.nextLine().trim().toLowerCase();

                if (choice.isEmpty()) {
                    System.out.println("Пожалуйста, введите 'да' или 'нет'");
                    continue;
                }

                boolean playAgain = choice.equals("да") || choice.equals("д") ||
                        choice.equals("yes") || choice.equals("y");

                if (!playAgain) {
                    System.out.println("\nСпасибо за игру! До новых встреч!");
                }

                return playAgain;

            } catch (NoSuchElementException e) {
                System.out.println("\nВвод завершен. Спасибо за игру!");
                return false;
            }
        }
    }

    private void cleanup() {
        try {
            if (scanner != null) {
                scanner.close();
            }
        } catch (Exception e) {
            logger.warning("Ошибка при закрытии сканера: " + e.getMessage());
        }

        if (gameLogWriter != null) {
            try {
                gameLogWriter.flush();
            } catch (Exception e) {
                logger.warning("Ошибка при сохранении логов: " + e.getMessage());
            }
        }
    }

    private void logGameEvent(String message) {
        if (gameLogWriter != null) {
            try {
                gameLogWriter.println(new Date() + " [Game] " + message);
                gameLogWriter.flush();
            } catch (Exception e) {
                logger.warning("Не удалось записать в лог-файл: " + e.getMessage());
            }
        }
    }

    // Методы для тестирования (должны быть package-private или публичные)
    public String getAnswer() {
        return answer;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public List<WordComparisonResult> getGameHistory() {
        return Collections.unmodifiableList(new ArrayList<>(gameHistory));
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setAnswerForTesting(String answer) {
        if (answer == null || answer.length() != 5) {
            throw new IllegalArgumentException("Ответ должен быть словом из 5 букв");
        }

        // Нормализуем слово
        String normalized = WordleDictionary.normalizeWord(answer);

        // Проверяем, что слово есть в словаре
        if (!dictionary.isValidGameWord(normalized)) {
            throw new IllegalArgumentException("Слово '" + answer + "' нет в словаре");
        }

        this.answer = normalized;
        this.gameHistory.clear();
        this.previousHints.clear();
        this.gameWon = false;
        this.remainingAttempts = MAX_ATTEMPTS;
    }
}

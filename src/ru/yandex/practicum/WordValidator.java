package ru.yandex.practicum;


import ru.yandex.practicum.exceptions.InvalidWordException;

public class WordValidator {
    public static void validate(String word) throws InvalidWordException {
        if (word == null) {
            throw new InvalidWordException("Слово не может быть null");
        }

        String trimmed = word.trim();

        if (trimmed.isEmpty()) {
            throw new InvalidWordException("Слово не может быть пустым");
        }

        if (trimmed.length() != 5) {
            throw new InvalidWordException("Слово должно содержать ровно 5 букв");
        }

        if (!trimmed.matches("[а-яёА-ЯЁ]+")) {
            throw new InvalidWordException("Слово должно содержать только русские буквы");
        }
    }
}

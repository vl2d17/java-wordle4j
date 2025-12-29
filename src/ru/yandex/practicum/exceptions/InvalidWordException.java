package ru.yandex.practicum.exceptions;

public class InvalidWordException extends Exception {
    public InvalidWordException(String message) {
        super(message);
    }

    public InvalidWordException(String message, Throwable cause) {
        super(message, cause);
    }
}


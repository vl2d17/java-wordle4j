package ru.yandex.practicum.exceptions;

public class GameRuntimeException extends RuntimeException {

    public GameRuntimeException(String message) {
        super(message);
    }

    public GameRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}


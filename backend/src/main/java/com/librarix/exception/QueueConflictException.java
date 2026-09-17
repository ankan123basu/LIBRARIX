package com.librarix.exception;

public class QueueConflictException extends RuntimeException {
    public QueueConflictException(String message) {
        super(message);
    }
}

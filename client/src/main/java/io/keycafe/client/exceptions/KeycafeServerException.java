package io.keycafe.client.exceptions;

public class KeycafeServerException extends RuntimeException {
    public KeycafeServerException(String message) {
        super(message);
    }

    public KeycafeServerException(Exception e) {
        super(e);
    }
}

package io.keycafe.client.exceptions;

public class KeycafeConnectionException extends KeycafeExeception {
    public KeycafeConnectionException(String message) {
        super(message);
    }

    public KeycafeConnectionException(Exception e) {
        super(e);
    }
}

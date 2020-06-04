package io.keycafe.client.exceptions;

public class KeycafeExeception extends RuntimeException {
    public KeycafeExeception(String message) {
        super(message);
    }

    public KeycafeExeception(Exception e) {
        super(e);
    }
}

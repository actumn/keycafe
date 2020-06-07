package io.keycafe.client.exceptions;

public class KeycafeClusterMaxAttemptsException extends KeycafeExeception {
    public KeycafeClusterMaxAttemptsException(String message) {
        super(message);
    }

    public KeycafeClusterMaxAttemptsException(Exception e) {
        super(e);
    }
}

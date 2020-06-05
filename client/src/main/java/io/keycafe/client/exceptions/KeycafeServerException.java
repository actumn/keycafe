package io.keycafe.client.exceptions;

public class KeycafeServerException extends KeycafeExeception {
    public KeycafeServerException(String message) {
        super(message);
    }

    public KeycafeServerException(Exception e) {
        super(e);
    }
}

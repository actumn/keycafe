package io.keycafe.client.exceptions;

public class KeycafeRedirectException extends KeycafeExeception {
    public KeycafeRedirectException(String message) {
        super(message);
    }

    public KeycafeRedirectException(Exception e) {
        super(e);
    }
}

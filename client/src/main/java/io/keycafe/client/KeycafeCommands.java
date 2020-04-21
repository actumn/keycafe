package io.keycafe.client;

public interface KeycafeCommands {
    String get(String key);

    String set(String key, String value);

    String delete(String key);
}

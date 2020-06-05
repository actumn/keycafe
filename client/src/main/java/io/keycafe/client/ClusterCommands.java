package io.keycafe.client;

public interface ClusterCommands {
    String get(String key);

    String set(String key, String value);

    String delete(String key);
}

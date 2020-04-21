package io.keycafe.client;

public interface Commands {
    void get(String key);

    void set(String key, String value);

    void delete(String key);
}

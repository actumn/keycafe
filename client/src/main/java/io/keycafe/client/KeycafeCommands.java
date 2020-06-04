package io.keycafe.client;

import java.util.List;

public interface KeycafeCommands {
    String get(String key);

    String set(String key, String value);

    String delete(String key);

    List<Object> clusterSlots();
}

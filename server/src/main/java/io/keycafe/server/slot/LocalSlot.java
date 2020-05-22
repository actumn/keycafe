package io.keycafe.server.slot;

import java.util.Map;

public class LocalSlot {
    public Map<String, String> kvStore;
    public Map<String, Long> expireStore;

    public LocalSlot(Map<String, String> kvStore, Map<String, Long> expireStore){
        this.kvStore = kvStore;
        this.expireStore = expireStore;
    }
}

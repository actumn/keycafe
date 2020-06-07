package io.keycafe.server.slot;

import java.util.Map;

public class LocalSlot {
    public Map<String, String> db;
    public Map<String, Long> expire;

    public LocalSlot(Map<String, String> db, Map<String, Long> expire){
        this.db = db;
        this.expire = expire;
    }
}

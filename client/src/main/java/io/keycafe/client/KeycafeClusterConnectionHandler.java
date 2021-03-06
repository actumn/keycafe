package io.keycafe.client;

import java.io.Closeable;

public class KeycafeClusterConnectionHandler implements Closeable  {
    private final KeycafeClusterInfoCache cache;

    public KeycafeClusterConnectionHandler(String host, int port) {
        this.cache = new KeycafeClusterInfoCache();
        initializeCache(host, port);
    }

    public Keycafe getConnectionFromSlot(int slot) {
        return cache.getKeycafeNode(slot);
    }

    private void initializeCache(String host, int port) {
        Keycafe keycafe = new Keycafe(host, port);
        keycafe.connect();
        cache.discoverCluster(keycafe);
        keycafe.close();
    }

    public void renewSlotCache(Keycafe keycafe) {
        cache.renewClusterSlots(keycafe);
    }

    @Override
    public void close() {
        cache.reset();
    }
}

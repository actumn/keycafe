package io.keycafe.client;

import io.keycafe.common.ClusterCRC16;

public abstract class KeycafeClusterCommand<T> {
    private final KeycafeClusterConnectionHandler connectionHandler;

    protected KeycafeClusterCommand(KeycafeClusterConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public abstract T execute(Keycafe connection);

    public T run(String key) {
        return run(ClusterCRC16.getSlot(key));
    }

    private T run(final int slot) {
        Keycafe connection = connectionHandler.getConnectionFromSlot(slot);
        return execute(connection);
    }
}

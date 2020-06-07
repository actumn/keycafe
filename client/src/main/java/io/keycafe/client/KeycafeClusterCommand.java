package io.keycafe.client;

import io.keycafe.client.exceptions.KeycafeClusterMaxAttemptsException;
import io.keycafe.client.exceptions.KeycafeRedirectException;
import io.keycafe.common.ClusterCRC16;

public abstract class KeycafeClusterCommand<T> {
    private final KeycafeClusterConnectionHandler connectionHandler;

    protected KeycafeClusterCommand(KeycafeClusterConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public abstract T execute(Keycafe connection);

    public T run(String key) {
        return run(ClusterCRC16.getSlot(key), 3);
    }

    private T run(final int slot, int attempts) {
        if (attempts <= 0) {
            throw new KeycafeClusterMaxAttemptsException("No more cluster attempts left.");
        }

        Keycafe connection = null;
        try {
            connection = connectionHandler.getConnectionFromSlot(slot);
            return execute(connection);
        } catch(KeycafeRedirectException redirectException) {
            connectionHandler.renewSlotCache(connection);
            return run(slot, attempts - 1);
        }
    }
}

package io.keycafe.client;

import java.io.Closeable;
import java.io.IOException;

public class KeycafeCluster implements ClusterCommands, Closeable {
    private final KeycafeClusterConnectionHandler connectionHandler;

    public KeycafeCluster(final String host, final int port) {
        this.connectionHandler = new KeycafeClusterConnectionHandler(host, port);
    }


    @Override
    public String get(String key) {
        return new KeycafeClusterCommand<String>(connectionHandler) {
            @Override
            public String execute(Keycafe connection) {
                return connection.get(key);
            }
        }.run(key);
    }

    @Override
    public String set(String key, String value) {
        return new KeycafeClusterCommand<String>(connectionHandler) {
            @Override
            public String execute(Keycafe connection) {
                return connection.set(key, value);
            }
        }.run(key);
    }

    @Override
    public String delete(String key) {
        return new KeycafeClusterCommand<String>(connectionHandler) {
            @Override
            public String execute(Keycafe connection) {
                return connection.delete(key);
            }
        }.run(key);
    }

    @Override
    public void close() {
        connectionHandler.close();
    }
}

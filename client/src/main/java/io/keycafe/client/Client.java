package io.keycafe.client;

import io.keycafe.client.network.Connection;
import io.keycafe.common.Protocol;

import java.io.IOException;

import static io.keycafe.common.Protocol.KEYCAFE_CHARSET;

public class Client extends Connection implements Commands {
    public Client() {
        super();
    }

    public Client(final String host) {
        super(host);
    }

    public Client(final String host, final int port) {
        super(host, port);
    }

    @Override
    public void get(String key) {
        try {
            this.sendCommand(Protocol.Command.GET, key.getBytes(KEYCAFE_CHARSET));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void set(String key, String value) {
        try {
            this.sendCommand(Protocol.Command.SET, key.getBytes(KEYCAFE_CHARSET), value.getBytes(KEYCAFE_CHARSET));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String key) {
        try {
            this.sendCommand(Protocol.Command.DELETE, key.getBytes(KEYCAFE_CHARSET));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clusterSlots() {
        try {
            this.sendCommand(Protocol.Command.CLUSTER, "slots".getBytes(KEYCAFE_CHARSET));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

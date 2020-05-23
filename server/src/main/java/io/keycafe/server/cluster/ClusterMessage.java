package io.keycafe.server.cluster;

import io.keycafe.server.Server;

public class ClusterMessage {
    private final byte[] signature = new byte[] {'K', 'C', 'm', 'b'};
    private ClusterMessageType type;
    private byte[] myslots = new byte[Server.CLUSTER_SLOTS / 8];
    private String sender;

    public ClusterMessage() {

    }

    public byte[] encode() {
        return null;
    }

    public static ClusterMessage decode(byte[] bytes) {
        return null;
    }

    public enum ClusterMessageType {
        PING, PONG
    }
}

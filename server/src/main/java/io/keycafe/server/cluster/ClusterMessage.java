package io.keycafe.server.cluster;

import io.keycafe.server.Server;

public class ClusterMessage {
    private final String signature = "KCmb";
    private ClusterMessageType type;
    private byte[] myslots = new byte[Server.CLUSTER_SLOTS / 8];
    private String sender;


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

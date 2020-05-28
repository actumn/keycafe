package io.keycafe.server.cluster;

public class ClusterMsg {
    private final ClusterMessageType type;
    private final byte[] myslots;
    private final String sender;

    public ClusterMsg(ClusterMessageType type, byte[] slots, String sender) {
        this.type = type;
        this.myslots = slots;
        this.sender = sender;
    }

    public ClusterMessageType getType() {
        return type;
    }

    public byte[] getMyslots() {
        return myslots;
    }

    public String getSender() {
        return sender;
    }

    public enum ClusterMessageType {
        PING, PONG
    }
}

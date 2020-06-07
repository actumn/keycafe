package io.keycafe.server.command.reply;

public class IntegerMessage implements ReplyMessage {
    private final int value;

    public IntegerMessage(int value) {
        this.value = value;
    }

    @Override
    public String message() {
        return ":" + value + "\r\n";
    }
}

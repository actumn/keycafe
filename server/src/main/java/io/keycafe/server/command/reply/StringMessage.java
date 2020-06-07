package io.keycafe.server.command.reply;

public class StringMessage implements ReplyMessage {
    public static ReplyMessage OkMessage = new StringMessage("ok");

    private final String message;

    public StringMessage(String message) {
        this.message = message;
    }

    @Override
    public String message() {
        return "+" + message + "\r\n";
    }
}

package io.keycafe.server.command.reply;

public class BulkStringMessage implements ReplyMessage {
    public static ReplyMessage NoKeyFoundMessage = new BulkStringMessage(null);

    private final String message;

    public BulkStringMessage(String message) {
        this.message = message;
    }

    @Override
    public String message() {
        if (message == null) {
            return "$-1\r\n";
        }

        return "$" + message.length() + "\r\n" + message + "\r\n";
    }
}

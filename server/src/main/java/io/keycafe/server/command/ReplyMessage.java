package io.keycafe.server.command;

public class ReplyMessage {
    public static ReplyMessage OkMessage = new ReplyMessage("ok");
    public static ReplyMessage WrongArgcMessage = new ReplyMessage("wrong argc numbers");
    public static ReplyMessage NoKeyFoundMessage = new ReplyMessage("no value found in this bucket");

    private final String message;

    public ReplyMessage(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

}

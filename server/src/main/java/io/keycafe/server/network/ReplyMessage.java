package io.keycafe.server.network;

public class ReplyMessage {
    public static ReplyMessage OkMessage = new ReplyMessage("ok");
    public static ReplyMessage WrongArgcMessage = new ReplyMessage("wrong argc numbers");

    private String message;

    public ReplyMessage(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

}

package io.keycafe.server.command.reply;


public class ErrorMessage implements ReplyMessage {
    public static ReplyMessage WrongArgcMessage = new ErrorMessage("wrong argc numbers");
    public static ReplyMessage SyntaxErrorMessage = new ErrorMessage("syntax error");

    private final String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public String message() {
        return "-" + message + "\r\n";
    }
}

package io.keycafe.server.command.reply;

import java.util.ArrayList;
import java.util.List;

public class ArrayMessage implements ReplyMessage {
    private final List<ReplyMessage> replyMessageList = new ArrayList<>();

    public void add(ReplyMessage message) {
        replyMessageList.add(message);
    }

    @Override
    public String message() {
        StringBuilder result = new StringBuilder();
        result.append('*');
        result.append(replyMessageList.size());

        for(ReplyMessage message : replyMessageList) {
            result.append(message.message());
        }


        return result.toString();
    }
}

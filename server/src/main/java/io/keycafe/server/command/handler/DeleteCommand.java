package io.keycafe.server.command.handler;

import io.keycafe.common.Protocol;
import io.keycafe.server.command.ReplyMessage;

import java.util.Map;

public class DeleteCommand implements CommandRunnable {

    private final Map<String, String> map;

    public DeleteCommand(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public ReplyMessage run(int argc, byte[][] argv) throws Exception {
        if (argc != 2) {
            return ReplyMessage.WrongArgcMessage;
        }

        String val = map.remove(new String(argv[1], Protocol.KEYCAFE_CHARSET));

        if (val != null) {
            return new ReplyMessage(val);
        } else {
            return ReplyMessage.OkMessage;
        }
//        throw new RuntimeException("Not implemented here");
    }
}

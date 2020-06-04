package io.keycafe.server.command.handler;

import io.keycafe.common.Protocol;
import io.keycafe.server.command.reply.BulkStringMessage;
import io.keycafe.server.command.reply.ErrorMessage;
import io.keycafe.server.command.reply.ReplyMessage;
import io.keycafe.server.command.reply.StringMessage;

import java.util.Map;

public class DeleteCommand implements CommandRunnable {

    private final Map<String, String> map;

    public DeleteCommand(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public ReplyMessage run(int argc, byte[][] argv) throws Exception {
        if (argc != 2) {
            return ErrorMessage.WrongArgcMessage;
        }

        String val = map.remove(new String(argv[1], Protocol.KEYCAFE_CHARSET));

        if (val != null) {
            return new BulkStringMessage(val);
        } else {
            return StringMessage.OkMessage;
        }
    }
}

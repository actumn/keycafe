package io.keycafe.server.command.handler;

import io.keycafe.common.Protocol;
import io.keycafe.server.command.reply.BulkStringMessage;
import io.keycafe.server.command.reply.ErrorMessage;
import io.keycafe.server.command.reply.ReplyMessage;
import io.keycafe.server.command.reply.StringMessage;

import java.util.Map;

public class SetCommand implements CommandRunnable {

    private Map<String, String> kvMap;
    private Map<String, Long> tMap;

    public SetCommand(Map<String, String> kvMap, Map<String, Long> tMap) {
        this.kvMap = kvMap;
        this.tMap = tMap;
    }

    @Override
    public ReplyMessage run(int argc, byte[][] argv) throws Exception {
        if (argc != 3) {
            return ErrorMessage.WrongArgcMessage;
        }

        String k = new String(argv[1], Protocol.KEYCAFE_CHARSET);
        String v = new String(argv[2], Protocol.KEYCAFE_CHARSET);
        String val = kvMap.put(k, v);
        tMap.put(k, System.currentTimeMillis());

        if (val != null) {
            return new BulkStringMessage(val);
        } else {
            return StringMessage.OkMessage;
        }
    }
}

package io.keycafe.server.command;

import io.keycafe.common.Protocol;
import io.keycafe.server.network.ReplyMessage;

import java.util.Date;
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
            return ReplyMessage.WrongArgcMessage;
        }

        String k = new String(argv[1], Protocol.KEYCAFE_CHARSET);
        String v = new String(argv[2], Protocol.KEYCAFE_CHARSET);
        String val = kvMap.put(k, v);
        tMap.put(k, System.currentTimeMillis());

        if (val != null) {
            return new ReplyMessage(val);
        } else {
            return ReplyMessage.OkMessage;
        }
    }
}

package io.keycafe.server.command;

import io.keycafe.common.Protocol;
import io.keycafe.server.network.ReplyMessage;

import java.util.Map;

public class GetCommand implements CommandRunnable {

    private Map<String, String> map;

    public GetCommand(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public ReplyMessage run(int argc, byte[][] argv) throws Exception {
        if (argc != 2) {
            return ReplyMessage.WrongArgcMessage;
        }

        String val = map.get(new String(argv[1], Protocol.KEYCAFE_CHARSET));
        if (val != null) {
            return new ReplyMessage(val);
        } else {
            return ReplyMessage.NoKeyFoundMessage;
        }
//      throw new RuntimeException("Not implemented yet.");
    }
}

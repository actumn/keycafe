package io.keycafe.server.command.handler;

import io.keycafe.common.Protocol;
import io.keycafe.server.command.reply.ErrorMessage;
import io.keycafe.server.command.reply.ReplyMessage;
import io.keycafe.server.command.reply.StringMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SetCommand implements CommandRunnable {
    private static final Logger logger = LogManager.getLogger(SetCommand.class);

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

        String key = new String(argv[1], Protocol.KEYCAFE_CHARSET);
        String val = new String(argv[2], Protocol.KEYCAFE_CHARSET);
        kvMap.put(key, val);
        logger.info("set key: {} - result: {}", key, val);
        tMap.put(key, System.currentTimeMillis());

        return StringMessage.OkMessage;
    }

    @Override
    public int keyIndex() {
        return 1;
    }
}

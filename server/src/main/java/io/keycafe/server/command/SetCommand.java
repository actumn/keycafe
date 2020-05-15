package io.keycafe.server.command;

import io.keycafe.common.Protocol;
import io.keycafe.server.network.ReplyMessage;
import java.util.Map;


public class SetCommand implements CommandRunnable {

  Map<String, String> map;

  public SetCommand(Map<String, String> map) {
    this.map = map;
  }

  @Override
  public ReplyMessage run(int argc, byte[][] argv) throws Exception {
    if (argc != 3) {
      return ReplyMessage.WrongArgcMessage;
    }

    String val = map.put(new String(argv[1], Protocol.KEYCAFE_CHARSET),
        new String(argv[2], Protocol.KEYCAFE_CHARSET));

    if (val != null) {
      return new ReplyMessage(val);
    } else {
      return ReplyMessage.OkMessage;
    }
  }
}

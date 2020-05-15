package io.keycafe.server.command;

import io.keycafe.common.Protocol.Command;
import io.keycafe.server.network.ReplyMessage;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeleteCommandTest {

  @Test
  public void test() throws Exception {
    byte[][] args = new byte[2][];
    args[0] = new byte[]{(byte) Command.DELETE.ordinal()};
    args[1] = "abc".getBytes();
    DeleteCommand d = new DeleteCommand(null);
    ReplyMessage reply = d.run(1, args);
    assertEquals(reply, ReplyMessage.WrongArgcMessage);
  }

  @Test
  public void test2() throws Exception {
    byte[][] args = new byte[1][];
    args[0] = new byte[]{(byte) Command.DELETE.ordinal()};
    DeleteCommand d = new DeleteCommand(null);
    ReplyMessage reply = d.run(2, args);
    assertEquals(reply, ReplyMessage.WrongArgcMessage);
  }

  @Test
  public void test3() throws Exception {
    Map<String, String> map = new HashMap<>();
    byte[][] args = new byte[2][];
    args[0] = new byte[]{(byte) Command.DELETE.ordinal()};
    args[1] = "no_key".getBytes();
    DeleteCommand d = new DeleteCommand(map);
    ReplyMessage reply = d.run(2, args);
    assertEquals(reply.message(), ReplyMessage.OkMessage.message());
  }

  @Test
  public void test4() throws Exception {
    Map<String, String> map = new HashMap<String, String>() {
      {
        put("TEST", "VALUE");
      }
    };
    byte[][] args = new byte[2][];
    args[0] = new byte[]{(byte) Command.DELETE.ordinal()};
    args[1] = "TEST".getBytes();
    DeleteCommand d = new DeleteCommand(map);
    ReplyMessage reply = d.run(2, args);
    assertEquals(reply.message(), new ReplyMessage("VALUE").message());
  }
}
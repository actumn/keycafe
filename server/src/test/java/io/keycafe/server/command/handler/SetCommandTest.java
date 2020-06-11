package io.keycafe.server.command.handler;

import static org.junit.Assert.assertEquals;

import io.keycafe.common.Protocol.Command;

import java.util.HashMap;
import java.util.Map;

import io.keycafe.server.command.reply.BulkStringMessage;
import io.keycafe.server.command.reply.ErrorMessage;
import io.keycafe.server.command.reply.ReplyMessage;
import io.keycafe.server.command.reply.StringMessage;
import org.junit.Test;

public class SetCommandTest {

    @Test
    public void test() throws Exception {
        byte[][] args = new byte[2][];
        args[0] = new byte[]{(byte) Command.SET.ordinal()};
        args[1] = "abc".getBytes();
        SetCommand s = new SetCommand(null, null);
        ReplyMessage reply = s.run(1, args);
        assertEquals(reply, ErrorMessage.WrongArgcMessage);
    }

    @Test
    public void test2() throws Exception {
        byte[][] args = new byte[2][];
        args[0] = new byte[]{(byte) Command.SET.ordinal()};
        args[1] = "abc".getBytes();
        SetCommand s = new SetCommand(null, null);
        ReplyMessage reply = s.run(2, args);
        assertEquals(reply, ErrorMessage.WrongArgcMessage);
    }

    @Test
    public void test3() throws Exception {
        byte[][] args = new byte[1][];
        args[0] = new byte[]{(byte) Command.SET.ordinal()};
        SetCommand s = new SetCommand(null, null);
        ReplyMessage reply = s.run(2, args);
        assertEquals(reply, ErrorMessage.WrongArgcMessage);
    }

    @Test
    public void test4() throws Exception {
        // Arrange
        Map<String, String> map = new HashMap<>();
        Map<String, Long> map2 = new HashMap<>();
        byte[][] args = new byte[3][];
        args[0] = new byte[]{(byte) Command.SET.ordinal()};
        args[1] = "TEST".getBytes();
        args[2] = "VALUE".getBytes();
        SetCommand s = new SetCommand(map, map2);

        // Action
        ReplyMessage reply = s.run(3, args);

        // Assert
        assertEquals(StringMessage.OkMessage.message(), reply.message());
    }

    @Test
    public void test5() throws Exception {
        // Arrange
        Map<String, String> map = new HashMap<String, String>() {
            {
                put("TEST", "PASTVAL");
            }
        };
        Map<String, Long> map2 = new HashMap<>();
        byte[][] args = new byte[3][];
        args[0] = new byte[]{(byte) Command.SET.ordinal()};
        args[1] = "TEST".getBytes();
        args[2] = "VALUE".getBytes();
        SetCommand s = new SetCommand(map, map2);

        // Action
        ReplyMessage reply = s.run(3, args);

        // Assert
        assertEquals(StringMessage.OkMessage.message(), reply.message());
    }
}

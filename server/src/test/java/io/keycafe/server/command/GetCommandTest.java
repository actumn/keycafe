package io.keycafe.server.command;

import static org.junit.Assert.assertEquals;

import io.keycafe.common.Protocol.Command;
import io.keycafe.server.network.ReplyMessage;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class GetCommandTest {

    @Test
    public void test() throws Exception {
        byte[][] args = new byte[2][];
        args[0] = new byte[]{(byte) Command.GET.ordinal()};
        args[1] = "abc".getBytes();
        GetCommand g = new GetCommand(null);
        ReplyMessage reply = g.run(1, args);
        assertEquals(reply, ReplyMessage.WrongArgcMessage);
    }

    @Test
    public void test2() throws Exception {
        byte[][] args = new byte[2][];
        args[0] = new byte[]{(byte) Command.GET.ordinal()};
        args[1] = "abc".getBytes();
        GetCommand g = new GetCommand(null);
        ReplyMessage reply = g.run(3, args);
        assertEquals(reply, ReplyMessage.WrongArgcMessage);
    }

    @Test
    public void test3() throws Exception {
        Map<String, String> map = new HashMap<>();
        byte[][] args = new byte[2][];
        args[0] = new byte[]{(byte) Command.GET.ordinal()};
        args[1] = "no_key".getBytes();
        GetCommand g = new GetCommand(map);
        ReplyMessage reply = g.run(2, args);
        assertEquals(reply.message(), ReplyMessage.NoKeyFoundMessage.message());
    }

    @Test
    public void test4() throws Exception {
        Map<String, String> map = new HashMap<String, String>() {
            {
                put("TEST", "VALUE");
            }
        };
        byte[][] args = new byte[2][];
        args[0] = new byte[]{(byte) Command.GET.ordinal()};
        args[1] = "TEST".getBytes();
        GetCommand g = new GetCommand(map);
        ReplyMessage reply = g.run(2, args);
        assertEquals(reply.message(), new ReplyMessage("VALUE").message());
    }
}

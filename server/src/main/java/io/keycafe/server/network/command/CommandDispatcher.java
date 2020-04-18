package io.keycafe.server.network.command;

import io.keycafe.common.Protocol;
import io.keycafe.server.network.CommandMessage;
import io.keycafe.server.network.ReplyMessage;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<Protocol.Command, RunnableCommand> commandMap;

    public CommandDispatcher() {
        this.commandMap = new HashMap<>() {
            {
                put(Protocol.Command.GET, new GetCommand());
                put(Protocol.Command.SET, new SetCommand());
            }
        };
    }

    private Protocol.Command lookup(byte[] command) {
        return Protocol.Command.values()[(int) command[0]];
    }


    public ReplyMessage dispatch(CommandMessage msg) {
        byte[][] argv = msg.getArgv();
        Protocol.Command command = lookup(argv[0]);

        return commandMap.get(command).run();
    }
}

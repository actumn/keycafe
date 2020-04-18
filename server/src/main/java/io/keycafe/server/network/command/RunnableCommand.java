package io.keycafe.server.network.command;

import io.keycafe.server.network.ReplyMessage;

public interface RunnableCommand {
    ReplyMessage run();
}

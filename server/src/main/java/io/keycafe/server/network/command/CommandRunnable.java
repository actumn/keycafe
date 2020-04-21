package io.keycafe.server.network.command;

import io.keycafe.server.network.ReplyMessage;

public interface CommandRunnable {
    ReplyMessage run(int argc, byte[][] argv);
}

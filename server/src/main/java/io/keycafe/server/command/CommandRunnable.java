package io.keycafe.server.command;

import io.keycafe.server.network.ReplyMessage;

public interface CommandRunnable {
    ReplyMessage run(int argc, byte[][] argv) throws Exception;
}

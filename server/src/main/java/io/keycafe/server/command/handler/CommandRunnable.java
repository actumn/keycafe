package io.keycafe.server.command.handler;

import io.keycafe.server.command.ReplyMessage;

public interface CommandRunnable {
    ReplyMessage run(int argc, byte[][] argv) throws Exception;
}

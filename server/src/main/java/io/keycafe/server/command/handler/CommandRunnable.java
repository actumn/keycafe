package io.keycafe.server.command.handler;

import io.keycafe.server.command.reply.ReplyMessage;

public interface CommandRunnable {
    ReplyMessage run(int argc, byte[][] argv) throws Exception;

    int keyIndex();
}

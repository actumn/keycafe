package io.keycafe.server.network.command;

import io.keycafe.server.network.ReplyMessage;

public class DeleteCommand implements CommandRunnable {
    @Override
    public ReplyMessage run(int argc, byte[][] argv) {
        throw new RuntimeException("Not implemented here");
    }
}

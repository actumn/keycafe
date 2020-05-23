package io.keycafe.server.command;

public class CommandMessage {
    private final int argc;
    private final byte[][] argv;

    public CommandMessage(final int argc, final byte[][] argv) {
        this.argc = argc;
        this.argv = argv;
    }

    public int getArgc() {
        return argc;
    }

    public byte[][] getArgv() {
        return argv;
    }
}

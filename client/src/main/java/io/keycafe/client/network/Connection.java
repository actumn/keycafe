package io.keycafe.client.network;

import io.keycafe.client.exceptions.KeycafeConnectionException;
import io.keycafe.client.stream.KeycafeInputStream;
import io.keycafe.client.stream.KeycafeOutputStream;
import io.keycafe.common.Protocol;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class Connection implements Closeable {

    private final String host;
    private final int port;

    private Socket socket;
    private KeycafeInputStream inputStream;
    private KeycafeOutputStream outputStream;

    public Connection() {
        this("localhost");
    }

    public Connection(String host) {
        this(host, Protocol.DEFAULT_PORT);
    }

    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        if (!isConnected()) {
            try {
                socket = new Socket();

                socket.setReuseAddress(true);
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);

                socket.connect(new InetSocketAddress(host, port));

                inputStream = new KeycafeInputStream(socket.getInputStream());
                outputStream = new KeycafeOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected()
                && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    public void sendCommand(Protocol.Command command, final byte[]... args) {
        try {
            outputStream.write(args.length + 1);
            outputStream.write(1);
            outputStream.write(command.ordinal());
            for (final byte[] arg : args) {
                outputStream.write(arg.length);
                outputStream.write(arg);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (isConnected()) {
            try {
                outputStream.flush();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public String getBulkReply() {
        final byte[] result = (byte[]) readObject();
        if (null == result) {
            return null;
        }
        try {
            return new String(result, Protocol.KEYCAFE_CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object readObject() {
        try {
            byte b = (byte) inputStream.read();
            switch (b) {
                case '+':
                    return readSimpleString();
                case '$':
                    return readBulkReply();
                case '*':
                    return readArray();
                case ':':
                    return readInteger();
                case '-':
                    readError();
                default:
                    throw new KeycafeConnectionException("Unknown reply: " + (char) b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] readSimpleString() {
        return null;
    }

    private byte[] readBulkReply() {
        return null;
    }

    private List<Object> readArray() {
        return null;
    }

    private Long readInteger() {
        return null;
    }

    private void readError() {

    }
}

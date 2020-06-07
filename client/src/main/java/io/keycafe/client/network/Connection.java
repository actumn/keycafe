package io.keycafe.client.network;

import io.keycafe.client.exceptions.KeycafeConnectionException;
import io.keycafe.client.exceptions.KeycafeExeception;
import io.keycafe.client.exceptions.KeycafeRedirectException;
import io.keycafe.client.exceptions.KeycafeServerException;
import io.keycafe.client.stream.KeycafeInputStream;
import io.keycafe.client.stream.KeycafeOutputStream;
import io.keycafe.client.util.StringCodec;
import io.keycafe.common.Protocol;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
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
                throw new KeycafeConnectionException(e);
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
            throw new KeycafeConnectionException(e);
        }
    }

    @Override
    public void close() {
        if (isConnected()) {
            try {
                outputStream.flush();
                socket.close();
            } catch (IOException e) {
                throw new KeycafeConnectionException(e);
            }
        }
    }

    public String getSimpleString() {
        return (String) readObject();
    }

    public String getBulkReply() {
        final byte[] result = (byte[]) readObject();
        if (null == result) {
            return null;
        }

        return StringCodec.decode(result);
    }

    public List<Object> getArray() {
        return (List<Object>) readObject();
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
            throw new KeycafeExeception(e);
        }
    }

    private String readSimpleString() {
        return inputStream.readLine();
    }

    private byte[] readBulkReply() {
        final int len = (int) inputStream.readLongCRLF();
        if (len == -1) {
            return null;
        }
        final byte[] read = new byte[len];

        try {
            inputStream.read(read);
            inputStream.read();
            inputStream.read();
            return read;
        } catch (IOException e) {
            throw new KeycafeConnectionException(e);
        }
    }

    private List<Object> readArray() {
        final int num = (int) inputStream.readLongCRLF();
        if (num == -1) {
            return null;
        }
        final List<Object> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            result.add(readObject());
        }
        return result;
    }

    private Long readInteger() {
        return inputStream.readLongCRLF();
    }

    private void readError() {
        String message = inputStream.readLine();
        if (message.startsWith("MOVED")) {
//            String[] movedInfo = message.split(" ");
            throw new KeycafeRedirectException(message);
        }
        throw new KeycafeServerException(inputStream.readLine());
    }
}

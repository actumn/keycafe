package io.keycafe.client.network;

import io.keycafe.common.Protocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connection implements Closeable {

  private final String host;
  private final int port;

  private Socket socket;
  private BufferedInputStream inputStream;
  private BufferedOutputStream outputStream;

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

        inputStream = new BufferedInputStream(socket.getInputStream());
        outputStream = new BufferedOutputStream(socket.getOutputStream());
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
    try {
      final byte[] len = new byte[1];
      inputStream.read(len);
      final byte[] result = new byte[len[0]];
      inputStream.read(result, 0, len[0]);
      return new String(result, Protocol.KEYCAFE_CHARSET);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}

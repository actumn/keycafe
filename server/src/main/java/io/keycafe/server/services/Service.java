package io.keycafe.server.services;

public interface Service {
    void run() throws Exception;
    void close();
}

package io.keycafe.client.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    @Test
    public void test() throws InterruptedException {
        Connection connection = new Connection();
        connection.connect();
//        assertEquals(1, 1);
    }

}

package main.java.communication;



import main.java.fachlogik.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
public class RoulettetableClientProxyTest {
    RoulettetableClientProxy roulettetableClientProxy;
    Player Player;
    @BeforeEach
    public void setUp() throws IOException {
        Socket socket = new Socket("127.0.0.1", 12345);
        roulettetableClientProxy = new RoulettetableClientProxy(socket);
        Player = new Player("Marcel");
        roulettetableClientProxy.enter(Player);
    }

    @AfterEach
    public void tearDown() throws IOException {
        roulettetableClientProxy.disconnect();
    }

    @Test
    public void enter() {
        try {
            roulettetableClientProxy.enter(Player);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(e.getMessage(), "Exception 1 Player Marcel already exists");
        }

    }

    @Test
    public void leave() {
        roulettetableClientProxy.leave(Player);
    }

    @Test
    public void post() {
        roulettetableClientProxy.post("message");
    }
}
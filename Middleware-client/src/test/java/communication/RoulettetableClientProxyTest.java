package communication;

import fachlogik.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
public class RoulettetableClientProxyTest {
    RoulettetableClientProxy RoulettetableClientProxy;
    Player Player;
    @BeforeEach
    public void setUp() throws IOException {
        Socket socket = new Socket("127.0.0.1", 12345);
        RoulettetableClientProxy = new RoulettetableClientProxy(socket);
        Player = new Player("Marcel");
        RoulettetableClientProxy.enter(Player);
    }

    @AfterEach
    public void tearDown() throws IOException {
        RoulettetableClientProxy.disconnect();
    }

    @Test
    public void enter() {
        try {
            RoulettetableClientProxy.enter(Player);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(e.getMessage(), "Exception 1 Player Marcel already exists");
        }

    }

    @Test
    public void leave() {
        RoulettetableClientProxy.leave(Player);
    }

    @Test
    public void post() {
        RoulettetableClientProxy.post(Player, "message");
    }
}
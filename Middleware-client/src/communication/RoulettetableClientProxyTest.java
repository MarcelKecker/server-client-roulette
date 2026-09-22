package communication;

import fachlogik.Chatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
public class RoulettetableClientProxyTest {
    RoulettetableClientProxy RoulettetableClientProxy;
    Chatter chatter;
    @BeforeEach
    public void setUp() throws IOException {
        Socket socket = new Socket("127.0.0.1", 12345);
        RoulettetableClientProxy = new RoulettetableClientProxy(socket);
        chatter = new Chatter("Marcel");
        RoulettetableClientProxy.enter(chatter);
    }

    @AfterEach
    public void tearDown() throws IOException {
        RoulettetableClientProxy.disconnect();
    }

    @Test
    public void enter() {
        try {
            RoulettetableClientProxy.enter(chatter);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(e.getMessage(), "Exception 1 Chatter Marcel already exists");
        }

    }

    @Test
    public void leave() {
        RoulettetableClientProxy.leave(chatter);
    }

    @Test
    public void post() {
        RoulettetableClientProxy.post(chatter, "message");
    }
}
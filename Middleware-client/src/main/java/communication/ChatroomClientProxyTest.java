package communication;

import fachlogik.Chatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
public class ChatroomClientProxyTest {
    ChatroomClientProxy chatroomClientProxy;
    Chatter chatter;
    @BeforeEach
    public void setUp() throws IOException {
        Socket socket = new Socket("127.0.0.1", 12345);
        chatroomClientProxy = new ChatroomClientProxy(socket);
        chatter = new Chatter("Marcel");
        chatroomClientProxy.enter(chatter);
    }

    @AfterEach
    public void tearDown() throws IOException {
        chatroomClientProxy.disconnect();
    }

    @Test
    public void enter() {
        try {
            chatroomClientProxy.enter(chatter);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals(e.getMessage(), "Exception 1 Chatter Marcel already exists");
        }

    }

    @Test
    public void leave() {
        chatroomClientProxy.leave(chatter);
    }

    @Test
    public void post() {
        chatroomClientProxy.post(chatter, "message");
    }
}
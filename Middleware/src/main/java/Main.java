import communication.ChatroomServerProxy;
import fachlogik.Chatroom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    static void main() throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        Chatroom chatroom = new Chatroom();
        while(true){
            Socket clientSocket = serverSocket.accept();
            ChatroomServerProxy chatroomServerProxy = new ChatroomServerProxy(clientSocket, chatroom);
            Thread t = new Thread(chatroomServerProxy);
            t.start();
        }
    }
}

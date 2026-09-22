import communication.RoulettetableServerProxy;
import fachlogik.Roulettetable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    static void main() throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        Roulettetable roulettetable = new Roulettetable();
        while(true){
            Socket clientSocket = serverSocket.accept();
            RoulettetableServerProxy roulettetableServerProxy = new RoulettetableServerProxy(clientSocket, roulettetable);
            Thread t = new Thread(roulettetableServerProxy);
            t.start();
        }
    }
}

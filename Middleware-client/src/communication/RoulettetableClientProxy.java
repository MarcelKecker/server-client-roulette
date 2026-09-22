package communication;

import fachlogik.Chatter;
import interfaces.IRoulettetable;
import interfaces.IChatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class RoulettetableClientProxy implements IRoulettetable {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    HashMap<IChatter, Integer> chatters;
    int chatterCount = 0;
    public RoulettetableClientProxy(Socket socket) throws IOException {
        this.socket = socket;
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
        writer = new LogWriter(socket.getOutputStream(), true);
        chatters = new HashMap<>();
        reader.readLine(); //Protokollzeile lesen (Welcome)
    }

    @Override
    public void enter(IChatter chatter) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("1");
            sendChatter(chatter);
            String returnCode = reader.readLine(); //Returncode lesen
            if (!returnCode.equals("0")) {
                handleException(returnCode);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();

        }
    }

    @Override
    public void leave(IChatter chatter) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("2");
            sendChatter(chatter);
            String returnCode = reader.readLine(); //Returncode lesen
            if (!returnCode.equals("0")) {
                handleException(returnCode);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void post(IChatter chatter, String message) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("3");
            sendChatter(chatter);
            reader.readLine(); //Messageanforderung lesen
            writer.println(message);
            String returnCode = reader.readLine(); //Returncode lesen
            if (!returnCode.equals("0")) {
                handleException(returnCode);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("4");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendChatter(IChatter chatter) throws IOException {
        reader.readLine(); //"Enter chatter id" lesen
        if (chatters.containsKey(chatter)) {
            writer.println(chatters.get(chatter) + "");
            return;
        }
        Integer id = chatterCount;
        chatterCount++;
        chatters.put(chatter, id);
        writer.println("" + id);
        ServerSocket serverSocket = new ServerSocket(0);
        reader.readLine(); //"Enter ServerSocket Port" lesen
        writer.println(serverSocket.getLocalPort() + "");
        writer.flush();
        Socket socket = serverSocket.accept();
        ChatterServerProxy chatterServerProxy = new ChatterServerProxy(socket, (Chatter) chatter);
        Thread t = new Thread(chatterServerProxy);
        t.start();
    }

    private void handleException(String returnCode) throws IOException {
        String exceptionMessage = reader.readLine(); //Fehlermeldung lesen
        throw new RuntimeException("Exception " + returnCode + " " + exceptionMessage);
    }
}
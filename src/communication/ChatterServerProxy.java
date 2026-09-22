package communication;

import fachlogik.Chatter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatterServerProxy implements Runnable{
    Socket socket;
    LogWriter writer;
    LogReader reader;
    Chatter chatter;

    public ChatterServerProxy(Socket socket, Chatter chatter) throws IOException {
        this.socket = socket;
        this.chatter = chatter;
        writer = new LogWriter(socket.getOutputStream(), true);
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        writer.println("Welcome to the Chatter Server Proxy");
        String input;
        do {
            writer.println("1: Hear; 2: Get Name; 3: Disconnect");
            try {
                input = reader.readLine();
                switch (input) {
                    case "1":
                        hear();
                        break;
                    case "2":
                        getName();
                        break;
                    case "3":
                        disconnect();
                        break;
                    default:
                        writer.println("Invalid input");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (!input.equals("3"));
    }

    private void hear() {
        try {
            writer.println("Enter message");
            String message = reader.readLine();
            chatter.hear(message);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void getName() {
        try {
            String name = chatter.getName();
            writer.println("0");
            writer.println(name);
        } catch (Exception e) {
            handleException(e);
        }

    }

    private void disconnect() {
        writer.println("Goodbye");
    }

    private void handleException(Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
        writer.println("1");
        writer.println(e.getMessage());
    }
}

package communication;

import fachlogik.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PlayerServerProxy implements Runnable{
    Socket socket;
    LogWriter writer;
    LogReader reader;
    Player player;

    public PlayerServerProxy(Socket socket, Player player) throws IOException {
        this.socket = socket;
        this.player = player;
        writer = new LogWriter(socket.getOutputStream(), true);
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        writer.println("Welcome to the Player Server Proxy");
        String input;
        do {
            writer.println("1: Hear; 2: Get Name; 3: Disconnect; 4: Get Id");
            try {
                input = reader.readLine();
                if (input == null) { // Verbindung wurde ohne Disconnect geschlossen
                    break;
                }
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
                    case "4":
                        getId();
                        break;
                    default:
                        writer.println("Invalid input");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (!input.equals("3"));
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void hear() {
        try {
            writer.println("Enter message");
            String message = reader.readLine();
            player.hear(message);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void getName() {
        try {
            String name = player.getName();
            writer.println("0");
            writer.println(name);
        } catch (Exception e) {
            handleException(e);
        }

    }

    private void getId() {
        try {
            String id = player.getId().toString();
            writer.println("0");
            writer.println(id);
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
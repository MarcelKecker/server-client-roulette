package communication;

import fachlogik.Player;
import interfaces.IRoulettetable;
import interfaces.IPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class RoulettetableServerProxy implements Runnable {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    IRoulettetable Roulettetable;
    HashMap<Integer, IPlayer> players;
    public RoulettetableServerProxy(Socket socket, IRoulettetable roulettetable) throws IOException {
        this.socket = socket;
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
        writer = new LogWriter(socket.getOutputStream(), true);
        this.roulettetable = roulettetable;
        players = new HashMap<>();
    }

    @Override
    public void run() {
<<<<<<< Updated upstream
        writer.println("Welcome to the Roulettetable Server");
=======
        writer.println("Willkommen am Pokertisch");
>>>>>>> Stashed changes
        boolean running = true;
        do {
            writer.println("1: Beitreten; 2: Verlassen; 3: Abschicken; 4: Verbindung trennen");
            String input;
            try {
                input = reader.readLine();
                switch (input) {
                    case "1": enter(); break;
                    case "2": leave(); break;
                    case "3": post(); break;
                    case "4": disconnect();
                    running = false;
                    break;
                    default:
                        writer.println("Ungültige Eingabe");
                        System.err.println("Ungültige Eingabe " + input + " von " + socket.getRemoteSocketAddress());
                        break;

                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.err.println(socket.getRemoteSocketAddress());
            }

        } while (running);
    }

    private void enter() throws IOException {
        IPlayer player = getPlayer();
        try {
            roulettetable.enter(player);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void leave() throws IOException {
        IPlayer player = getPlayer();
        try {
            chatroom.leave(player);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }
    private void post() throws IOException {
<<<<<<< Updated upstream
        IPlayer player = getPlayer();
        writer.println("Enter message");
=======
        IChatter chatter = getChatter();
        writer.println("Nachricht eingeben");
>>>>>>> Stashed changes
        String message = reader.readLine();
        try {
            roulettetable.post(player, message);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void disconnect() {
        for (IPlayer iplayer : players.values()) {
            PlayerClientProxy player = (PlayerClientProxy) iPlayer;
            player.deactivate();
        }
        writer.println("Verbindung vom Pokertisch trennen");
    }

    private void handleException(Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
        writer.println("1");
        writer.println(e.getMessage());
    }

<<<<<<< Updated upstream
    private IPlayer getPlayer() throws IOException {
        writer.println("Enter player id");
=======
    private IChatter getChatter() throws IOException {
        writer.println("Spieler Id eingeben");
>>>>>>> Stashed changes
        Integer id = Integer.valueOf(reader.readLine());
        if (players.containsKey(id)) {
            return players.get(id);
        }
        writer.println("ServerSocket Port eingeben");
        int port = Integer.parseInt(reader.readLine());
        System.out.println(this.socket.getInetAddress());
        Socket socket1 = new Socket(socket.getInetAddress(), port);
        PlayerClientProxy playerClientProxy = new PlayerClientProxy(socket1);
        players.put(id, chatterClientProxy);
        return playerClientProxy;
    }
}
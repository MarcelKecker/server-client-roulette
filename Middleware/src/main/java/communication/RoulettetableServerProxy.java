package communication;

import fachlogik.Bet;
import fachlogik.BetType;
import fachlogik.Player;
import interfaces.IRoulettetable;
import interfaces.IPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class RoulettetableServerProxy implements Runnable {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    IRoulettetable roulettetable;
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
        writer.println("Willkommen am Roulettetisch");
        boolean running = true;
        do {
            writer.println("1: Beitreten; 2: Verlassen; 3: Wette erstellen; 4: Verbindung trennen");
            String input;
            try {
                input = reader.readLine();
                switch (input) {
                    case "1": enter(); break;
                    case "2": leave(); break;
                    case "3": postBet(); break;
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
            roulettetable.leave(player);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void postBet() throws IOException {
        IPlayer player = getPlayer();
        Bet bet = new Bet();
        bet.setPlayerId(player.getId());
        writer.println("Wie viel möchtest du setzen?");
        String stakeInput = reader.readLine();
        try {
            int stake = Integer.parseInt(stakeInput.trim());
            if (stake < 0) {
                throw new NumberFormatException();
            }

            bet.setStake(stake);
        } catch (NumberFormatException e) {
            writer.println("Ungültige Eingabe");
            postBet();
            return;
        }

        writer.println("Auf was möchtest du wetten; 1: Schwarz, 2: Rot, 3: Gerade, 4: Ungerade, 5: Zahl");
        String input = reader.readLine();
        switch (input) {
            case "1":
                bet.setBetType(BetType.BLACK);
                break;
            case "2":
                bet.setBetType(BetType.RED);
                break;
            case "3":
                bet.setBetType(BetType.EVEN);
                break;
            case "4":
                bet.setBetType(BetType.ODD);
                break;
            case "5":
                writer.println("Auf welche Zahl (0-36)?");
                try {
                    int zahl = Integer.parseInt(reader.readLine().trim());
                    if (zahl < 0 || zahl > 36) {
                        writer.println("Zahl muss zwischen 0 und 36 liegen");
                        postBet();
                        return;
                    }
                    bet.setBetType(BetType.NUMBER);
                    bet.setNumber(zahl);
                } catch (NumberFormatException e) {
                    writer.println("Das war keine Zahl");
                    postBet();
                    return;
                }
                break;
            default:
                writer.println("Ungültige Eingabe");
                postBet();
                return;
        }
        try {
            roulettetable.addBet(bet);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void disconnect() {
        for (IPlayer iPlayer : players.values()) {
            ((PlayerClientProxy) iPlayer).disconnect();
        }
        writer.println("Verbindung vom Pokertisch trennen");
        try { socket.close(); } catch (IOException ignored) {}
    }

    private void handleException(Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
        writer.println("1");
        writer.println(e.getMessage());
    }

    private IPlayer getPlayer() throws IOException {
        writer.println("Spieler Id eingeben");
        Integer id = Integer.valueOf(reader.readLine());
        if (players.containsKey(id)) {
            return players.get(id);
        }
        writer.println("ServerSocket Port eingeben");
        int port = Integer.parseInt(reader.readLine());
        System.out.println(this.socket.getInetAddress());
        Socket socket1 = new Socket(socket.getInetAddress(), port);
        PlayerClientProxy playerClientProxy = new PlayerClientProxy(socket1);
        players.put(id, playerClientProxy);
        return playerClientProxy;
    }
}
package communication;


import fachlogik.Bet;
import fachlogik.BetType;
import fachlogik.Player;
import interfaces.IPlayer;
import interfaces.IRoulettetable;

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
    HashMap<IPlayer, Integer> players;
    int playerCount = 0;
    public RoulettetableClientProxy(Socket socket) throws IOException {
        this.socket = socket;
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
        writer = new LogWriter(socket.getOutputStream(), true);
        players = new HashMap<>();
        reader.readLine(); //Protokollzeile lesen (Welcome)
    }

    @Override
    public void enter(IPlayer player) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: Beitreten; 2: Verlassen; 3: Wette erstellen; 4: Verbindung trennen)
            writer.println("1");
            sendPlayer(player);
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
    public void leave(IPlayer player) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: Beitreten; 2: Verlassen; 3: Wette erstellen; 4: Verbindung trennen)
            writer.println("2");
            sendPlayer(player);
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
    public void addBet(Bet bet) {
        IPlayer player = null;
        for (IPlayer p : players.keySet()) {
            if (p.getId().equals(bet.getPlayerId())) {
                player = p;
            }
        }
        if (player == null) {
            throw new IllegalArgumentException("Spieler ist nicht am Tisch");
        }
        if (bet.getStake() < 0) {
            throw new IllegalArgumentException("Einsatz darf nicht negativ sein");
        }
        if (bet.getBetType() == BetType.NUMBER && (bet.getNumber() < 0 || bet.getNumber() > 36)) {
            throw new IllegalArgumentException("Zahl muss zwischen 0 und 36 liegen");
        }

        try {
            reader.readLine(); //Protokollzeile lesen
            writer.println("3");
            sendPlayer(player);
            reader.readLine(); //"Wie viel möchtest du setzen?" lesen
            writer.println(bet.getStake() + "");
            reader.readLine(); //"Auf was möchtest du wetten" lesen
            switch (bet.getBetType()) {
                case BetType.BLACK:
                    writer.println("1");
                    break;
                case BetType.RED:
                    writer.println("2");
                    break;
                case BetType.EVEN:
                    writer.println("3");
                    break;
                case BetType.ODD:
                    writer.println("4");
                    break;
                case BetType.NUMBER:
                    writer.println("5");
                    reader.readLine(); //"Auf welche Zahl (0-36)?" lesen
                    writer.println(bet.getNumber() + "");
                    break;
            }
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

    public void sendPlayer(IPlayer player) throws IOException {
        reader.readLine(); //"Enter player id" lesen
        if (players.containsKey(player)) {
            writer.println(players.get(player) + "");
            return;
        }
        Integer id = playerCount;
        playerCount++;
        players.put(player, id);
        writer.println("" + id);
        ServerSocket serverSocket = new ServerSocket(0);
        reader.readLine(); //"Enter ServerSocket Port" lesen
        writer.println(serverSocket.getLocalPort() + "");
        writer.flush();
        Socket socket = serverSocket.accept();
        PlayerServerProxy playerServerProxy = new PlayerServerProxy(socket, (Player) player);
        Thread t = new Thread(playerServerProxy);
        t.start();
    }

    private void handleException(String returnCode) throws IOException {
        String exceptionMessage = reader.readLine(); //Fehlermeldung lesen
        throw new RuntimeException("Exception " + returnCode + " " + exceptionMessage);
    }
}
package main.java.communication;

import main.java.fachlogik.Bet;
import main.java.fachlogik.Player;
import main.java.interfaces.IPlayer;
import main.java.interfaces.IRoulettetable;

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
    public void postBet(IPlayer player, Bet bet) {

    }

    @Override
    public void postResult(IPlayer p, int win) {

    }

    @Override
    public void addBet(Bet bet) {

    }

    @Override
    public void post(String message) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("3");
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
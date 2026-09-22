package communication;

import fachlogik.Player;
import interfaces.IRoulettetable;
import interfaces.IPlayer;

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
    HashMap<IPlayer, Integer> Players;
    int PlayerCount = 0;
    public RoulettetableClientProxy(Socket socket) throws IOException {
        this.socket = socket;
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
        writer = new LogWriter(socket.getOutputStream(), true);
        Players = new HashMap<>();
        reader.readLine(); //Protokollzeile lesen (Welcome)
    }

    @Override
    public void enter(IPlayer Player) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("1");
            sendPlayer(Player);
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
    public void leave(IPlayer Player) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("2");
            sendPlayer(Player);
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
    public void post(IPlayer Player, String bet,  int stake) {
        try {
            reader.readLine(); //Protokollzeile lesen (1: enter; 2: leave; 3: post; 4: disconnect)
            writer.println("3");
            sendPlayer(Player);
            reader.readLine(); //Wettanforderung lesen
            writer.println(bet);
            reader.readLine(); // Einsatzanforderung lesen
            writer.println(stake);
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

    public void sendPlayer(IPlayer Player) throws IOException {
        reader.readLine(); //"Enter Player id" lesen
        if (Players.containsKey(Player)) {
            writer.println(Players.get(Player) + "");
            return;
        }
        Integer id = PlayerCount;
        PlayerCount++;
        Players.put(Player, id);
        writer.println("" + id);
        ServerSocket serverSocket = new ServerSocket(0);
        reader.readLine(); //"Enter ServerSocket Port" lesen
        writer.println(serverSocket.getLocalPort() + "");
        writer.flush();
        Socket socket = serverSocket.accept();
        PlayerServerProxy PlayerServerProxy = new PlayerServerProxy(socket, (Player) Player);
        Thread t = new Thread(PlayerServerProxy);
        t.start();
    }

    private void handleException(String returnCode) throws IOException {
        String exceptionMessage = reader.readLine(); //Fehlermeldung lesen
        throw new RuntimeException("Exception " + returnCode + " " + exceptionMessage);
    }
}
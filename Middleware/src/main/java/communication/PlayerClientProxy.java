package communication;

import interfaces.IPlayer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;

public class PlayerClientProxy implements IPlayer {
    Socket socket;
    LogReader reader;
    LogWriter writer;
    boolean active;
    UUID id;

    public PlayerClientProxy(Socket socket) throws IOException {
        this.socket = socket;
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
        writer = new LogWriter(socket.getOutputStream(), true);
        reader.readLine(); //Welcome Zeile lesen
        this.active = true;
    }

    @Override
    public synchronized void hear(String message) {
        if (!active) {
            return;
        }
        try {
            reader.readLine(); //Protokollzeile lesen
            writer.println("1");
            reader.readLine(); //"Enter Message" lesen
            writer.println(message);
            String returnCode = reader.readLine(); //Returncode lesen
            if (!returnCode.equals("0")) {
                handleException(returnCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized String getName() {
        if (!active) {
            return null;
        }
        try {
            reader.readLine(); //Protokollzeile lesen
            writer.println("2");
            String returnCode = reader.readLine(); //Returncode lesen
            if (!returnCode.equals("0")) {
                handleException(returnCode);
            }
            return reader.readLine(); //Name lesen
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized UUID getId() {
        if (id != null) {
            return id;
        }
        if (!active) {
            return null;
        }
        try {
            reader.readLine(); //Protokollzeile lesen
            writer.println("4");
            String returnCode = reader.readLine(); //Returncode lesen
            if (!returnCode.equals("0")) {
                handleException(returnCode);
            }
            id = UUID.fromString(reader.readLine()); //ID lesen
            return id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleException(String returnCode) throws IOException {
        String exceptionMessage = reader.readLine(); //Fehlermeldung lesen
        throw new RuntimeException("Exception " + returnCode + " " + exceptionMessage);
    }

    public void disconnect() {
        if (!active) return;
        active = false;
        try {
            reader.readLine();    // Menüzeile
            writer.println("3");  // 3: Disconnect (4 wäre Get Id)
            reader.readLine();    // "Goodbye"
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deactivate() {
        this.active = false;
    }
}
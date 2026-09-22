package communication;

import fachlogik.Chatter;
import interfaces.IChatter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PlayerClientProxy implements IPlayer {
    Socket socket;
    LogReader reader;
    LogWriter writer;
    boolean active;

    public PlayerClientProxy(Socket socket) throws IOException {
        this.socket = socket;
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
        writer = new LogWriter(socket.getOutputStream(), true);
        reader.readLine(); //Welcome Zeile lesen
        this.active = true;
    }

    @Override
    public void hear(String message) {
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
    public String getName() {
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

    private void handleException(String returnCode) throws IOException {
        String exceptionMessage = reader.readLine(); //Fehlermeldung lesen
        throw new RuntimeException("Exception " + returnCode + " " + exceptionMessage);
    }

    public void deactivate() {
        this.active = false;
    }
}

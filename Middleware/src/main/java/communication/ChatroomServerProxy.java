package communication;

import fachlogik.Chatter;
import interfaces.IChatroom;
import interfaces.IChatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class ChatroomServerProxy implements Runnable {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    IChatroom chatroom;
    HashMap<Integer, IChatter> chatters;
    public ChatroomServerProxy(Socket socket, IChatroom chatroom) throws IOException {
        this.socket = socket;
        reader = new LogReader(new InputStreamReader(socket.getInputStream()));
        writer = new LogWriter(socket.getOutputStream(), true);
        this.chatroom = chatroom;
        chatters = new HashMap<>();
    }

    @Override
    public void run() {
        writer.println("Welcome to the Chatroom Server");
        boolean running = true;
        do {
            writer.println("1: enter; 2: leave; 3: post; 4: disconnect");
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
                        writer.println("Invalid input");
                        System.err.println("Invalid input " + input + " by " + socket.getRemoteSocketAddress());
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
        IChatter chatter = getChatter();
        try {
            chatroom.enter(chatter);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void leave() throws IOException {
        IChatter chatter = getChatter();
        try {
            chatroom.leave(chatter);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }
    private void post() throws IOException {
        IChatter chatter = getChatter();
        writer.println("Enter message");
        String message = reader.readLine();
        try {
            chatroom.post(chatter, message);
            writer.println("0");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void disconnect() {
        for (IChatter iChatter : chatters.values()) {
            ChatterClientProxy chatter = (ChatterClientProxy) iChatter;
            chatter.deactivate();
        }
        writer.println("Disconnected from the Chatroom");
    }

    private void handleException(Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
        writer.println("1");
        writer.println(e.getMessage());
    }

    private IChatter getChatter() throws IOException {
        writer.println("Enter chatter id");
        Integer id = Integer.valueOf(reader.readLine());
        if (chatters.containsKey(id)) {
            return chatters.get(id);
        }
        writer.println("Enter ServerSocket Port");
        int port = Integer.parseInt(reader.readLine());
        System.out.println(this.socket.getInetAddress());
        Socket socket1 = new Socket(socket.getInetAddress(), port);
        ChatterClientProxy chatterClientProxy = new ChatterClientProxy(socket1);
        chatters.put(id, chatterClientProxy);
        return chatterClientProxy;
    }
}
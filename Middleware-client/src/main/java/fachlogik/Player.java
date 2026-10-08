package main.java.fachlogik;

import main.java.interfaces.IPlayer;

import java.util.UUID;

public class Player implements IPlayer {
    String name;
    UUID id;
    public Player(String name) {
        this.name = name;
    }
    @Override
    public void hear(String message) {
        System.out.println(message + " (" + name + ")");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getID() {
        return id;
    }
}

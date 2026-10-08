package fachlogik;

import interfaces.IPlayer;

import java.util.UUID;

public class Player implements IPlayer {


    String name;
    UUID id;
    public Player(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
    }
    @Override
    public void hear(String message) {
        System.out.println(message);
    }

    @Override
    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }
}

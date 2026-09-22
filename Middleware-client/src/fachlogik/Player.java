package fachlogik;

import interfaces.IPlayer;

public class Player implements IPlayer {
    String name;
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
}

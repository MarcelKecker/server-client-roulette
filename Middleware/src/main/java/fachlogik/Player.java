package fachlogik;

import interfaces.IChatter;

public class Player implements IChatter {
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

package fachlogik;

import interfaces.IChatter;

public class Chatter implements IChatter {
    String name;
    public Chatter(String name) {
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

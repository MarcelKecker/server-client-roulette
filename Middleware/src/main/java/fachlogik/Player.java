package fachlogik;

import interfaces.IPlayer;

import java.util.UUID;

public class Player implements IPlayer {

    public static final int DEFAULT_SALDO = 1000;

    String name;
    int saldo;
    UUID id;
    public Player(String name) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.saldo = DEFAULT_SALDO;
    }
    @Override
    public void hear(String message) {
        System.out.println(message + " (" + name + ")");
    }

    @Override
    public String getName() {
        return name;
    }

    public int getSaldo() {
        return saldo;
    }

    public UUID getId() {
        return id;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }
}

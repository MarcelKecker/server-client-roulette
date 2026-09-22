package fachlogik;

import interfaces.IPlayer;

public class Player implements IPlayer {
    String name;
    int saldo;
    public Player(String name) {
        this.name = name;
    }
    @Override
    public void hearResults(String result, int win) {
        setSaldo(getSaldo() + win);
        System.out.println(result);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSaldo() {
        return 0;
    }

    @Override
    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }
}

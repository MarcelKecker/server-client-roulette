package fachlogik;

import interfaces.IPlayer;

public class Player implements IPlayer {
    String name;
    int saldo;
    public Player(String name) {
        this.name = name;
    }
    @Override
    public void hearResults(String resultMessage, int win) {
        System.out.println(resultMessage + " (Gewinn: " + win + ")");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSaldo() {
        return 0;
    }
}

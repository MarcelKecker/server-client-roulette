package main.java.interfaces;

import main.java.fachlogik.Bet;
import main.java.interfaces.IPlayer;

public interface IRoulettetable {
    void enter(IPlayer player);
    void leave(IPlayer player);
    void postBet(IPlayer player, Bet bet);
    void postResult(IPlayer p, int win);
    void post(String message);
    void addBet(Bet bet);
}


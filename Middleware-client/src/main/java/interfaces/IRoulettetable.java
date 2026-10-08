package interfaces;


import fachlogik.Bet;

public interface IRoulettetable {
    void enter(IPlayer player);
    void leave(IPlayer player);
    void addBet(Bet bet);
}


package interfaces;

import fachlogik.Bet;

public interface IRoulettetable {
    void enter(IPlayer player);
    void leave(IPlayer player);
    void postBet(IPlayer player, Bet bet);
    void addBet(Bet bet);
}

package interfaces;

public interface IRoulettetable {
    void enter(IPlayer Player);
    void leave(IPlayer Player);
    void post(IPlayer Player, String bet, int stake);
}

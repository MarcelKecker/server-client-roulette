package interfaces;

public interface IRoulettetable {
    void enter(IPlayer player);
    void leave(IPlayer player);
    void post(IPlayer player, String message);
}

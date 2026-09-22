package interfaces;

public interface IRoulettetable {
    void enter(IChatter chatter);
    void leave(IChatter chatter);
    void post(IChatter chatter, String message);
}

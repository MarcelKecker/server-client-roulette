package interfaces;

public interface IChatroom {
    void enter(IChatter chatter);
    void leave(IChatter chatter);
    void post(IChatter chatter, String message);
}

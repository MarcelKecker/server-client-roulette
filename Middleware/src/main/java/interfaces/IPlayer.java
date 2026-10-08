package interfaces;

import java.util.UUID;

public interface IPlayer {
    void hear(String message);
    String getName();
    UUID getId();
    void setSaldo(int saldo);
    int getSaldo();
}

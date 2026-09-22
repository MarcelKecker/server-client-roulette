package interfaces;

public interface IPlayer {
    void hearResults(String results, int win);
    String getName();
    int getSaldo();
    void setSaldo(int saldo);
}

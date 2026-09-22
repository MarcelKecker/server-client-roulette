package fachlogik;

public class Bet {
    private int playerId;
    private int stake;
    private int number;
    private BetType betType;

    public Bet(int playerId, int stake, int number, BetType betType) {
        this.playerId = playerId;
        this.stake = stake;
        this.number = number;
        this.betType = betType;
    }

    public int getPlayerId() {
        return playerId;
    }
    public int getStake() {
        return stake;
    }

    public int getNumber() {
        return number;
    }

    public BetType getBetType() {
        return betType;
    }

    public void setBetType(BetType betType) {
        this.betType = betType;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setStake(int stake) {
        this.stake = stake;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return playerId + "\t" + stake + "\t" + number + "\t" + betType;
    }
}

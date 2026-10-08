package fachlogik;

import java.util.UUID;

public class Bet {
    private UUID playerId;
    private int stake;
    private int number;
    private BetType betType;

    public Bet() {}
    public Bet(UUID playerId, int stake, int number, BetType betType) {
        this.playerId = playerId;
        this.stake = stake;
        this.number = number;
        this.betType = betType;
    }

    public UUID getPlayerId() {
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

    public void setPlayerId(UUID playerId) {
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
        String target = switch (betType) {
            case RED -> "Rot";
            case BLACK -> "Schwarz";
            case EVEN -> "Gerade";
            case ODD -> "Ungerade";
            case NUMBER -> "Zahl " + number;
            default -> String.valueOf(betType);
        };
        return stake + "€ auf " + target;
    }
}

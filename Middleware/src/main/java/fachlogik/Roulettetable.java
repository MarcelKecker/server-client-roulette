package fachlogik;

import interfaces.IChatter;

import java.util.ArrayList;
import java.util.List;

public class Roulettetable implements interfaces.IChatroom {
    List<IChatter> players = new ArrayList<>();
    @Override
    public void enter(IPlayer player) {
        if (players.contains(player)) {
            throw new IllegalArgumentException("Player " + player.getName() + " already exists");
        }
        players.add(chatter);
        post(player, "entered");
    }

    @Override
    public void leave(IPlayer player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player " + player.getName() + " does not exist");
        }
        players.remove(player);
        post(player, "left");
    }

    @Override
    public void post(IPlayer author, String message) {
        for (IPlayer player : players) {
            if (player == author) {
                player.hear("I said: " + message);
            } else {
                player.hear(author.getName() + " said: " + message);
            }
        }
    }
}

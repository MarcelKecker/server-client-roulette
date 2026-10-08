package fachlogik;

import interfaces.IPlayer;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.*;

public class Roulettetable implements interfaces.IRoulettetable {
    public static final int DEFAULT_SALDO = 1000;
    List<IPlayer> players = new CopyOnWriteArrayList<>();
    List<Bet> activeBets = new ArrayList<>();
    Boolean isGameActive = false;
    Map<IPlayer, Integer> saldos = new HashMap<>();
    private static final Map<Integer, String> ROULETTE_MAP = Map.ofEntries(
            Map.entry(0, "Grün"),
            Map.entry(1, "Rot"),
            Map.entry(2, "Schwarz"),
            Map.entry(3, "Rot"),
            Map.entry(4, "Schwarz"),
            Map.entry(5, "Rot"),
            Map.entry(6, "Schwarz"),
            Map.entry(7, "Rot"),
            Map.entry(8, "Schwarz"),
            Map.entry(9, "Rot"),
            Map.entry(10, "Schwarz"),
            Map.entry(11, "Schwarz"),
            Map.entry(12, "Rot"),
            Map.entry(13, "Schwarz"),
            Map.entry(14, "Rot"),
            Map.entry(15, "Schwarz"),
            Map.entry(16, "Rot"),
            Map.entry(17, "Schwarz"),
            Map.entry(18, "Rot"),
            Map.entry(19, "Rot"),
            Map.entry(20, "Schwarz"),
            Map.entry(21, "Rot"),
            Map.entry(22, "Schwarz"),
            Map.entry(23, "Rot"),
            Map.entry(24, "Schwarz"),
            Map.entry(25, "Rot"),
            Map.entry(26, "Schwarz"),
            Map.entry(27, "Rot"),
            Map.entry(28, "Schwarz"),
            Map.entry(29, "Schwarz"),
            Map.entry(30, "Rot"),
            Map.entry(31, "Schwarz"),
            Map.entry(32, "Rot"),
            Map.entry(33, "Schwarz"),
            Map.entry(34, "Rot"),
            Map.entry(35, "Schwarz"),
            Map.entry(36, "Rot")
    );

    @Override
    public void enter(IPlayer player) {
        if (players.contains(player)) {
            throw new IllegalArgumentException("Spieler " + player.getName() + " gibt es schon");
        }
        players.add(player);
        saldos.put(player, DEFAULT_SALDO);
        post(player.getName() + " hat sich an den Tisch gesetzt und " + saldos.get(player) + "€ dabei!");
    }

    @Override
    public void leave(IPlayer player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Spieler " + player.getName() + " gibt es nicht");
        }
        players.remove(player);
        post(player.getName() + " hat den Tisch verlassen und seine " + saldos.get(player) + "€ wieder mit nach Hause genommen!");
    }

    public void postBet(IPlayer p, Bet bet) {
        for (IPlayer player : players) {
            if (player == p) {
                player.hear("Du hast folgendes gesetzt: " + bet.toString());
            } else {
                player.hear(p.getName() + " hat folgendes gesetzt: " + bet.toString());
            }
        }
    }

    public void postResult(IPlayer p, int win) {
        String t;
        if (win > 0) {
            t = win + "€ gewonnen";
        } else if (win < 0) {
            t = -win + "€ verloren";
        } else {
            t = "nichts gewonnen und nichts verloren";
        }


        for (IPlayer player : players) {
            if (player == p) {
                t = ("Ich habe " + t);
            } else {
                t = (p.getName() + " hat " + t);
            }
            player.hear(t + " Neuer Saldo: " + saldos.get(player) + "€");
        }
    }

    public void post(String message) {
        for (IPlayer player : players) {
            player.hear(message);
        }
    }

    public void addBet(Bet bet) {
        for(IPlayer player : players) {
            if(player.getId().equals(bet.getPlayerId())) {
                if(saldos.get(player) < bet.getStake()) {
                    player.hear("Du hast nicht genug Geld !!!");
                    return;
                }
                saldos.put(player, saldos.get(player) - bet.getStake());
                postBet(player, bet);
            }
        }

        activeBets.add(bet);

        if(!isGameActive) {
            isGameActive = true;
            post("Runde gestartet, mache jetzt deine Einsätze");
            Thread countdown = new Thread(() -> {
                for (int i = 30; i >= 0; i--) {
                    if(i % 5 == 0) {
                        post("Noch " + i + " Sekunden!");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                post("Keine weiteren Einsätze!");
                isGameActive = false;
                calculateWins();
            });
            countdown.start();
        }
    }

    public void calculateWins() {
        post("Kugel wird in das Spiel geworfen");
        int random = (int) (Math.random()*37);
        String color = ROULETTE_MAP.get(random);
        Map<UUID, Integer> results = new HashMap<>();
        post("Ergebnis: " + random + " "+ color);

        for(Bet bet : activeBets) {
            if(!results.containsKey(bet.getPlayerId())) {
                results.put(bet.getPlayerId(), 0);
            }

            IPlayer player = null;
            for(IPlayer p: players) {
                if(p.getId().equals(bet.getPlayerId())) {
                    player = p;
                }
            }
            if (player == null) continue;

            switch (bet.getBetType()) {
                case BetType.BLACK:
                    if (color.equals("Schwarz")) {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) + bet.getStake());
                        saldos.put(player, saldos.get(player) + bet.getStake() * 2);
                    } else {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) - bet.getStake());
                    }
                    break;
                case BetType.RED:
                    if (color.equals("Rot")) {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) + bet.getStake());
                        saldos.put(player, saldos.get(player) + bet.getStake() * 2);
                    } else {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) - bet.getStake());
                    }
                    break;
                case BetType.EVEN:
                    if (random != 0 && random % 2 == 0) {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) + bet.getStake());
                        saldos.put(player, saldos.get(player) + bet.getStake() * 2);
                    } else {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) - bet.getStake());
                    }
                    break;
                case BetType.ODD:
                    if (random % 2 == 1) {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) + bet.getStake());
                        saldos.put(player, saldos.get(player) + bet.getStake() * 2);
                    } else {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) - bet.getStake());
                    }
                    break;
                case BetType.NUMBER:
                    if (bet.getNumber() == random) {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) + bet.getStake() * 35);
                        saldos.put(player, saldos.get(player) + bet.getStake() * 36);
                    } else {
                        results.put(bet.getPlayerId(), results.get(bet.getPlayerId()) - bet.getStake());
                    }
                    break;
            }
        }

        for(IPlayer player : players) {
            if(results.containsKey(player.getId())) {
                postResult(player, results.get(player.getId()));
            }
        }
        activeBets.clear();
    }
}

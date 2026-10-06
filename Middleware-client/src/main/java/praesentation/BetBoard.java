/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Die Chips, die in der laufenden Runde auf dem Tisch liegen (Feld -> Betrag), mit Rueckgaengig
 * und der Moeglichkeit, die Wetten der letzten Runde zu wiederholen.
 */
public class BetBoard {
    /** Ein einzelner gesetzter Chip. */
    public record Placement(String bet, int amount) {
    }

    private final Map<String, Integer> bets = new LinkedHashMap<>();
    private final Deque<Placement> placements = new ArrayDeque<>();
    private Map<String, Integer> lastRound = Map.of();

    public void place(String bet, int amount) {
        bets.merge(bet, amount, Integer::sum);
        placements.push(new Placement(bet, amount));
    }

    /** Nimmt den zuletzt gesetzten Chip zurueck; null, wenn nichts liegt. */
    public Placement undo() {
        Placement last = placements.poll();
        if (last != null) {
            bets.computeIfPresent(last.bet(), (bet, amount) -> amount == last.amount() ? null : amount - last.amount());
        }
        return last;
    }

    /** Raeumt alle Chips ab und liefert den Gesamtbetrag. */
    public int clear() {
        int total = total();
        bets.clear();
        placements.clear();
        return total;
    }

    /** Beendet die Runde: aktuelle Wetten werden als "letzte Runde" gemerkt und abgeraeumt. */
    public void finishRound() {
        lastRound = new LinkedHashMap<>(bets);
        bets.clear();
        placements.clear();
    }

    public int total() {
        return bets.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int amountOn(String bet) {
        return bets.getOrDefault(bet, 0);
    }

    public boolean isEmpty() {
        return bets.isEmpty();
    }

    public Map<String, Integer> bets() {
        return Collections.unmodifiableMap(bets);
    }

    public Map<String, Integer> lastRound() {
        return lastRound;
    }

    /** Summe aller Auszahlungen (inkl. Einsatz) fuer die Gewinnzahl {@code number}. */
    public int payout(int number) {
        int payout = 0;
        for (Map.Entry<String, Integer> entry : bets.entrySet()) {
            if (RouletteRules.wins(entry.getKey(), number)) {
                payout += RouletteRules.payout(entry.getKey(), entry.getValue());
            }
        }
        return payout;
    }
}

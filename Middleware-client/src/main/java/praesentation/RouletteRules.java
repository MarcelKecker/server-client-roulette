/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import java.util.Set;

/**
 * Lokale Roulette-Regeln fuer das Frontend (europaeisches Roulette, eine Null).
 * <p>
 * Wett-Strings: "0".."36" und "rot", "schwarz", "gerade", "ungerade" wie in der Konsolen-Version,
 * dazu die weiteren Felder eines echten Tisches: "1-18", "19-36", die Dutzende "1-12", "13-24",
 * "25-36" und die Kolonnen "kolonne1" (1, 4, 7, ...), "kolonne2" (2, 5, 8, ...), "kolonne3" (3, 6, 9, ...).
 */
public final class RouletteRules {
    public static final String RED = "rot";
    public static final String BLACK = "schwarz";
    public static final String EVEN = "gerade";
    public static final String ODD = "ungerade";
    public static final String LOW = "1-18";
    public static final String HIGH = "19-36";
    public static final String DOZEN_1 = "1-12";
    public static final String DOZEN_2 = "13-24";
    public static final String DOZEN_3 = "25-36";
    public static final String COLUMN_1 = "kolonne1";
    public static final String COLUMN_2 = "kolonne2";
    public static final String COLUMN_3 = "kolonne3";

    private static final Set<Integer> RED_NUMBERS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);

    private RouletteRules() {
    }

    public static boolean isRed(int number) {
        return RED_NUMBERS.contains(number);
    }

    public static boolean isBlack(int number) {
        return number != 0 && !isRed(number);
    }

    public static boolean wins(String bet, int number) {
        return switch (bet) {
            case RED -> isRed(number);
            case BLACK -> isBlack(number);
            case EVEN -> number != 0 && number % 2 == 0;
            case ODD -> number % 2 == 1;
            case LOW -> number >= 1 && number <= 18;
            case HIGH -> number >= 19;
            case DOZEN_1 -> number >= 1 && number <= 12;
            case DOZEN_2 -> number >= 13 && number <= 24;
            case DOZEN_3 -> number >= 25;
            case COLUMN_1 -> number != 0 && number % 3 == 1;
            case COLUMN_2 -> number != 0 && number % 3 == 2;
            case COLUMN_3 -> number != 0 && number % 3 == 0;
            default -> Integer.parseInt(bet) == number;
        };
    }

    /** Auszahlung inklusive Einsatz: Zahl 35:1, Dutzend/Kolonne 2:1, einfache Chancen 1:1. */
    public static int payout(String bet, int stake) {
        return stake * (odds(bet) + 1);
    }

    /** Gewinnquote (x:1). */
    public static int odds(String bet) {
        if (isNumberBet(bet)) {
            return 35;
        }
        return switch (bet) {
            case DOZEN_1, DOZEN_2, DOZEN_3, COLUMN_1, COLUMN_2, COLUMN_3 -> 2;
            default -> 1;
        };
    }

    /** true fuer Wetten auf eine einzelne Zahl. */
    public static boolean isNumberBet(String bet) {
        return !bet.isEmpty() && bet.chars().allMatch(Character::isDigit);
    }

    public static String colorName(int number) {
        return number == 0 ? "gruen" : isRed(number) ? "rot" : "schwarz";
    }

    /** Anzeigename einer Wette, z.B. "Zahl 17" oder "Rot". */
    public static String describe(String bet) {
        return switch (bet) {
            case RED -> "Rot";
            case BLACK -> "Schwarz";
            case EVEN -> "Gerade (Pair)";
            case ODD -> "Ungerade (Impair)";
            case LOW -> "1–18 (Manque)";
            case HIGH -> "19–36 (Passe)";
            case DOZEN_1 -> "1. Dutzend (1–12)";
            case DOZEN_2 -> "2. Dutzend (13–24)";
            case DOZEN_3 -> "3. Dutzend (25–36)";
            case COLUMN_1 -> "1. Kolonne (1, 4, 7 …)";
            case COLUMN_2 -> "2. Kolonne (2, 5, 8 …)";
            case COLUMN_3 -> "3. Kolonne (3, 6, 9 …)";
            default -> "Zahl " + bet;
        };
    }
}

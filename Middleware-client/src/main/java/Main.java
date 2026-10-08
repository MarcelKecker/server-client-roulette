import communication.RoulettetableClientProxy;
import fachlogik.Bet;
import fachlogik.BetType;
import fachlogik.Player;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public static void main(String[] args) throws IOException {
    Socket socket = new Socket("127.0.0.1", 12345);
    RoulettetableClientProxy roulettetableClientProxy = new RoulettetableClientProxy(socket);
    System.out.println("Bitte Name eingeben");
    Scanner scanner = new Scanner(System.in);
    String name = scanner.nextLine();
    Player player = new Player(name);
    roulettetableClientProxy.enter(player);

    while (true) {
        System.out.println("Einsatz eingeben, -1 um abzubrechen");
        String input = scanner.nextLine().trim();
        if (input.equals("-1")) {
            break;
        }

        Bet bet = new Bet();
        bet.setPlayerId(player.getId());

        try {
            int stake = Integer.parseInt(input);
            if (stake <= 0) {
                System.out.println("Einsatz muss größer als 0 sein");
                continue;
            }
            bet.setStake(stake);
        } catch (NumberFormatException e) {
            System.out.println("Das war keine Zahl");
            continue;
        }

        System.out.println("Auf was möchtest du wetten; 1: Schwarz, 2: Rot, 3: Gerade, 4: Ungerade, 5: Zahl");
        switch (scanner.nextLine().trim()) {
            case "1":
                bet.setBetType(BetType.BLACK);
                break;
            case "2":
                bet.setBetType(BetType.RED);
                break;
            case "3":
                bet.setBetType(BetType.EVEN);
                break;
            case "4":
                bet.setBetType(BetType.ODD);
                break;
            case "5":
                System.out.println("Auf welche Zahl (0-36)?");
                try {
                    int zahl = Integer.parseInt(scanner.nextLine().trim());
                    if (zahl < 0 || zahl > 36) {
                        System.out.println("Zahl muss zwischen 0 und 36 liegen");
                        continue;
                    }
                    bet.setBetType(BetType.NUMBER);
                    bet.setNumber(zahl);
                } catch (NumberFormatException e) {
                    System.out.println("Das war keine Zahl");
                    continue;
                }
                break;
            default:
                System.out.println("Ungültige Eingabe");
                continue;
        }

        roulettetableClientProxy.addBet(bet);
    }

    roulettetableClientProxy.leave(player);
    roulettetableClientProxy.disconnect();
}
import communication.RoulettetableClientProxy;
import fachlogik.Player;

void main() throws IOException {
    Socket socket = new Socket("127.0.0.1", 12345);
    RoulettetableClientProxy RoulettetableClientProxy = new RoulettetableClientProxy(socket);
    System.out.println("Bitte Name eingeben");
    Scanner scanner = new Scanner(System.in);
    String name = scanner.nextLine();
    Player Player = new Player(name);
    RoulettetableClientProxy.enter(Player);
    System.out.println("Gib eine Wette ein: Zahl zwischen 0-36, rot/schwarz oder gerade/ungerade");
    String inputBet = scanner.nextLine();
    System.out.println("Gib deinen Einsatz in Euro ein");
    int inputStake = scanner.nextInt();
    RoulettetableClientProxy.post(Player, inputBet, inputStake);
    do {
        System.out.println("Gib eine Wette ein: Zahl zwischen 0-36, rot/schwarz oder gerade/ungerade, fertig um wetten abzuschicken");
        inputBet = scanner.nextLine();
        if (!inputBet.equals("fertig")) {
            RoulettetableClientProxy.post(Player, inputBet, inputStake);
        }
    } while (!inputBet.equals("fertig"));
    RoulettetableClientProxy.disconnect();
}

import communication.RoulettetableClientProxy;
import communication.LogWriter;
import fachlogik.Player;

void main() throws IOException {
    Socket socket = new Socket("127.0.0.1", 12345);
    RoulettetableClientProxy RoulettetableClientProxy = new RoulettetableClientProxy(socket);
    System.out.println("Bitte Name eingeben");
    Scanner scanner = new Scanner(System.in);
    String name = scanner.nextLine();
    Player Player = new Player(name);
    RoulettetableClientProxy.enter(Player);
    String input;
    do{
        System.out.println("Nachricht eingeben, -1 um abzubrechen");
        input = scanner.nextLine();
        if (!input.equals("-1")) {
            RoulettetableClientProxy.post(Player, input);
        }
    } while (!input.equals("-1"));
    RoulettetableClientProxy.disconnect();
}

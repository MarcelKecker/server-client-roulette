import main.java.communication.RoulettetableClientProxy;
import main.java.fachlogik.Player;

void main() throws IOException {
    Socket socket = new Socket("127.0.0.1", 12345);
    RoulettetableClientProxy roulettetableClientProxy = new RoulettetableClientProxy(socket);
    System.out.println("Bitte Name eingeben");
    Scanner scanner = new Scanner(System.in);
    String name = scanner.nextLine();
    Player player = new Player(name);
    roulettetableClientProxy.enter(player);
    String input;
    do{
        System.out.println("Nachricht eingeben, -1 um abzubrechen");
        input = scanner.nextLine();
        if (!input.equals("-1")) {
            roulettetableClientProxy.post(input);
        }
    } while (!input.equals("-1"));
    roulettetableClientProxy.disconnect();
}

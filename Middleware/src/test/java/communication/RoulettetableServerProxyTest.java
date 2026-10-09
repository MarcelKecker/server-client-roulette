package communication;

import fachlogik.Bet;
import fachlogik.BetType;
import interfaces.IPlayer;
import interfaces.IRoulettetable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class RoulettetableServerProxyTest {
    private static final int TIMEOUT_MS = 5000;
    private static final String PLAYER_MENU = "1: Hear; 2: Get Name; 3: Disconnect; 4: Get Id";

    private FakeTable table;
    private FakePlayerEndpoint player;
    private ServerSocket listener;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeEach
    void setUp() throws IOException {
        table = new FakeTable();
        player = new FakePlayerEndpoint("Leo");

        listener = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        clientSocket = new Socket(InetAddress.getLoopbackAddress(), listener.getLocalPort());
        clientSocket.setSoTimeout(TIMEOUT_MS);
        Socket serverSide = listener.accept();
        Thread proxy = new Thread(new RoulettetableServerProxy(serverSide, table), "server-proxy");
        proxy.setDaemon(true);
        proxy.start();

        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);
        expect("Willkommen am Roulettetisch");
    }

    @AfterEach
    void tearDown() {
        closeQuietly(clientSocket);
        closeQuietly(listener);
        player.close();
    }

    @Test
    void menuWirdNachDemWillkommenAngeboten() throws IOException {
        expect("1: Beitreten; 2: Verlassen; 3: Wette erstellen; 4: Verbindung trennen");
    }

    @Test
    void ungueltigeMenueEingabeWirdAbgelehntUndMenueKommtErneut() throws IOException {
        readMenu();
        out.println("x");
        expect("Ungültige Eingabe");
        readMenu();
    }


    @Test
    void beitretenFragtIdUndPortUndMeldetErfolg() throws IOException {
        assertEquals("0", join(0));

        assertEquals(1, table.entered.size());
        assertEquals("Leo", table.entered.get(0).getName());
    }

    @Test
    void zweitesBeitretenMitDerselbenIdFragtNichtErneutNachDemPort() throws IOException {
        join(0);

        readMenu();
        out.println("1");
        expect("Spieler Id eingeben");
        out.println("0");

        assertEquals("1", in.readLine());
        assertEquals("Spieler existiert schon", in.readLine());
    }

    @Test
    void verlassenMeldetErfolg() throws IOException {
        join(0);

        readMenu();
        out.println("2");
        expect("Spieler Id eingeben");
        out.println("0");
        assertEquals("0", in.readLine());

        assertEquals(1, table.left.size());
        assertSame(table.entered.get(0), table.left.get(0));
    }

    @Test
    void verlassenOhneBeitrittMeldetFehlerMitText() throws IOException {
        readMenu();
        out.println("2");
        expect("Spieler Id eingeben");
        out.println("0");
        expect("ServerSocket Port eingeben");
        out.println(player.port());

        assertEquals("1", in.readLine());
        assertEquals("Spieler nicht am Tisch", in.readLine());
    }



    @Test
    void hearDesTischsErreichtDenClientUeberDenRueckkanal() throws Exception {
        join(0);

        table.entered.get(0).hear("Noch 20 Sekunden!");

        assertEquals("Noch 20 Sekunden!", player.heard.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }



    @ParameterizedTest
    @CsvSource({"1,BLACK", "2,RED", "3,EVEN", "4,ODD"})
    void wetteAufEinfacheChancen(String menuNumber, BetType expected) throws IOException {
        join(0);

        placeBetUpToType(50);
        out.println(menuNumber);
        assertEquals("0", in.readLine());

        assertEquals(1, table.bets.size());
        Bet bet = table.bets.get(0);
        assertEquals(50, bet.getStake());
        assertEquals(expected, bet.getBetType());
        assertEquals(player.id, bet.getPlayerId());
    }

    @Test
    void wetteAufEineZahl() throws IOException {
        join(0);

        placeBetUpToType(25);
        out.println("5");
        expect("Auf welche Zahl (0-36)?");
        out.println("17");
        assertEquals("0", in.readLine());

        Bet bet = table.bets.get(0);
        assertEquals(25, bet.getStake());
        assertEquals(BetType.NUMBER, bet.getBetType());
        assertEquals(17, bet.getNumber());
    }

    @Test
    void zahlAusserhalbDesBereichsWirdAbgelehntUndWetteWirdNeuAbgefragt() throws IOException {
        join(0);

        placeBetUpToType(25);
        out.println("5");
        expect("Auf welche Zahl (0-36)?");
        out.println("99");
        expect("Zahl muss zwischen 0 und 36 liegen");
        assertTrue(table.bets.isEmpty());

        expect("Spieler Id eingeben");
        out.println("0");
        expect("Wie viel möchtest du setzen?");
        out.println("10");
        readBetTypeQuestion();
        out.println("2");
        assertEquals("0", in.readLine());

        assertEquals(1, table.bets.size());
        assertEquals(BetType.RED, table.bets.get(0).getBetType());
        assertEquals(10, table.bets.get(0).getStake());
    }

    @Test
    void ungueltigerEinsatzWirdAbgelehnt() throws IOException {
        join(0);

        readMenu();
        out.println("3");
        expect("Spieler Id eingeben");
        out.println("0");
        expect("Wie viel möchtest du setzen?");
        out.println("viel");
        expect("Ungültige Eingabe");
        expect("Spieler Id eingeben");
        assertTrue(table.bets.isEmpty());
    }

    @Test
    void fehlerDesTischsBeimSetzenKommtAlsReturncodeUndText() throws IOException {
        join(0);
        table.betFailure = new IllegalStateException("Du hast nicht genug Geld");

        placeBetUpToType(5000);
        out.println("1");

        assertEquals("1", in.readLine());
        assertEquals("Du hast nicht genug Geld", in.readLine());
    }


    @Test
    void trennenSchicktDemSpielerDisconnectUndSchliesstDieVerbindung() throws Exception {
        join(0);

        readMenu();
        out.println("4");

        assertTrue(player.disconnected.await(TIMEOUT_MS, TimeUnit.MILLISECONDS),
                "Der Spieler hat kein Disconnect bekommen");
        expect("Verbindung vom Pokertisch trennen");
        assertNull(in.readLine(), "Der Server muss die Verbindung schliessen");
    }

    @Test
    void trennenOhneSpielerSchliesstDieVerbindung() throws IOException {
        readMenu();
        out.println("4");

        expect("Verbindung vom Pokertisch trennen");
        assertNull(in.readLine());
    }



    private void readMenu() throws IOException {
        String line = in.readLine();
        assertNotNull(line, "Verbindung wurde unerwartet geschlossen");
        assertTrue(line.startsWith("1: Beitreten"), "Menüzeile erwartet, war aber: " + line);
    }

    private void expect(String expected) throws IOException {
        assertEquals(expected, in.readLine());
    }


    private String join(int id) throws IOException {
        readMenu();
        out.println("1");
        expect("Spieler Id eingeben");
        out.println(id);
        expect("ServerSocket Port eingeben");
        out.println(player.port());
        return in.readLine();
    }


    private void placeBetUpToType(int stake) throws IOException {
        readMenu();
        out.println("3");
        expect("Spieler Id eingeben");
        out.println("0");
        expect("Wie viel möchtest du setzen?");
        out.println(stake);
        readBetTypeQuestion();
    }

    private void readBetTypeQuestion() throws IOException {
        String line = in.readLine();
        assertNotNull(line);
        assertTrue(line.startsWith("Auf was möchtest du wetten"), "Frage nach der Wettart erwartet: " + line);
    }

    private static void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (Exception ignored) {
        }
    }


    private static class FakePlayerEndpoint implements Runnable {
        final ServerSocket serverSocket;
        final String name;
        final UUID id = UUID.randomUUID();
        final BlockingQueue<String> heard = new LinkedBlockingQueue<>();
        final CountDownLatch disconnected = new CountDownLatch(1);

        FakePlayerEndpoint(String name) throws IOException {
            this.name = name;
            this.serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
            Thread t = new Thread(this, "fake-player");
            t.setDaemon(true);
            t.start();
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void run() {
            try (Socket socket = serverSocket.accept()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                writer.println("Welcome to the Player Server Proxy");
                while (true) {
                    writer.println(PLAYER_MENU);
                    String command = reader.readLine();
                    if (command == null) {
                        return;
                    }
                    switch (command) {
                        case "1":
                            writer.println("Enter message");
                            heard.add(reader.readLine());
                            writer.println("0");
                            break;
                        case "2":
                            writer.println("0");
                            writer.println(name);
                            break;
                        case "3":
                            writer.println("Goodbye");
                            disconnected.countDown();
                            return;
                        case "4":
                            writer.println("0");
                            writer.println(id);
                            break;
                        default:
                            writer.println("Invalid input");
                    }
                }
            } catch (IOException ignored) {
            }
        }

        void close() {
            closeQuietly(serverSocket);
        }
    }

    private static class FakeTable implements IRoulettetable {
        final List<IPlayer> entered = new CopyOnWriteArrayList<>();
        final List<IPlayer> left = new CopyOnWriteArrayList<>();
        final List<Bet> bets = new CopyOnWriteArrayList<>();
        volatile RuntimeException betFailure;

        @Override
        public void enter(IPlayer player) {
            if (entered.contains(player)) {
                throw new IllegalArgumentException("Spieler existiert schon");
            }
            entered.add(player);
        }

        @Override
        public void leave(IPlayer player) {
            if (!entered.contains(player)) {
                throw new IllegalArgumentException("Spieler nicht am Tisch");
            }
            left.add(player);
        }

        @Override
        public void addBet(Bet bet) {
            if (betFailure != null) {
                throw betFailure;
            }
            bets.add(bet);
        }
    }
}

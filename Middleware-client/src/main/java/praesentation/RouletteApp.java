/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.audio.AudioListenerState;
import com.jme3.font.BitmapFont;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.TechniqueDef;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import communication.RoulettetableClientProxy;
import fachlogik.Player;

import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 3D-Voxel-Frontend fuer den Roulette-Tisch aus First-Person-Sicht.
 * <p>
 * Die Middleware wird unveraendert genutzt: {@link RoulettetableClientProxy} fuer die Aufrufe an den
 * Server und {@link Player} als Callback-Objekt. Alle Netzwerkaufrufe laufen in einem eigenen Thread,
 * weil der Proxy blockierend liest und das Rendering sonst einfrieren wuerde.
 */
public class RouletteApp extends SimpleApplication {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 12345;
    public static final int START_SALDO = 1000;
    private static final String MUTE = "App_Mute";
    /** Pixelgroesse des Retro-Filters beim Start (1 = aus), zur Laufzeit mit P umschaltbar. */
    public static final int START_PIXEL_SIZE = 1;
    /** Sichtfeld (Grad), wenn die Kamera beim Drehen auf das Rad zoomt. */
    private static final float WHEEL_FOCUS_FOV = 60f;
    /**
     * Wohin der Spieler beim Drehen geht: aufstehen und einen Schritt nach vorn links an die Tischkante.
     * Von dort sind Rad und Croupier (samt Sprechblase) gemeinsam im Bild und die Mitspieler stehen nicht
     * im Weg (Position per Suche ueber die Bildausschnitte bestimmt).
     */
    private static final Vector3f WHEEL_VIEW_EYE = new Vector3f(-0.60f, 1.85f, 0.80f);
    private static final Vector3f WHEEL_VIEW_TARGET = new Vector3f(0f, 1.25f, -1.3f);
    /** Sekunden, die Ergebnis und Chips nach dem Stillstand des Rads sichtbar bleiben. */
    private static final float ROUND_END_DELAY = 4f;

    private final Player player;
    /**
     * Kontostand, wie ihn das Frontend anzeigt. Der Server wertet (noch) nicht aus und {@link Player}
     * bietet keinen Setter, deshalb fuehrt die App den Saldo selbst.
     */
    private int saldo = START_SALDO;
    private final Random random = new Random();
    private final ExecutorService network = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Roulette-Netzwerk");
        thread.setDaemon(true);
        return thread;
    });
    private volatile RoulettetableClientProxy proxy;

    private TableLayout table;
    private RouletteWheel wheel;
    private Dealer dealer;
    private SeatCameraState seat;
    private BetSelectionState selection;
    private HudState hud;
    private ChipStackView chips;
    private Crowd crowd;
    private Hall hall;
    private WinEffects effects;
    private SoundBank sounds;
    private final BetBoard board = new BetBoard();
    private boolean roundInProgress;
    private float roundEndTimer;

    /**
     * Argumente: optional ein Spielername und/oder {@code --vollbild}.
     * Vollbild geht alternativ ueber die System-Property {@code -Dvollbild=true}.
     */
    public static void main(String[] args) {
        String name = null;
        boolean fullscreen = Boolean.getBoolean("vollbild");
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--vollbild") || arg.equalsIgnoreCase("--fullscreen")) {
                fullscreen = true;
            } else if (!arg.isBlank() && name == null) {
                name = arg.trim();
            }
        }
        if (name == null) {
            name = "Spieler-" + (1000 + new Random().nextInt(9000));
        }

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Voxel-Roulette - " + name);
        settings.setResolution(1280, 720);
        settings.setSamples(4);
        settings.setVSync(true);
        if (fullscreen) {
            useDesktopFullscreen(settings);
        }

        RouletteApp app = new RouletteApp(name);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    /** Vollbild in der aktuellen Aufloesung und Bildfrequenz des Hauptbildschirms. */
    private static void useDesktopFullscreen(AppSettings settings) {
        DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        settings.setFullscreen(true);
        settings.setResolution(mode.getWidth(), mode.getHeight());
        if (mode.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN) {
            settings.setFrequency(mode.getRefreshRate());
        }
        if (mode.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI) {
            settings.setBitsPerPixel(mode.getBitDepth());
        }
    }

    public RouletteApp(String playerName) {
        // Bewusst ohne FlyCamAppState, StatsAppState und DebugKeysAppState: keine freie Kamera
        super(new AudioListenerState(), new ConstantVerifierState());
        // Ergebnisse des Servers (PlayerServerProxy -> hearResults) kommen im Netzwerk-Thread an
        // und werden in den Render-Thread uebergeben.
        player = new Player(playerName) {
            @Override
            public void hearResults(String resultMessage, int win) {
                super.hearResults(resultMessage, win);
                enqueue(() -> onServerResult(resultMessage, win));
            }
        };
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0.04f, 0.04f, 0.07f, 1f));
        // Mehrere Raumlichter in einem Durchgang rendern
        renderManager.setPreferredLightMode(TechniqueDef.LightMode.SinglePass);
        renderManager.setSinglePassLightBatchSize(9);

        // Hochaufloesende Schriften aus Systemfonts statt der kleinen jME-Standardschrift,
        // gerendert in der Groesse, in der das (mitskalierte) HUD sie anzeigt
        float hudScale = Math.max(1f, cam.getHeight() / 720f);
        String[] uiFamilies = {"Segoe UI Semibold", "Segoe UI", "Arial"};
        BitmapFont smallFont = FontFactory.create(assetManager, uiFamilies, Font.PLAIN, Math.round(17 * hudScale));
        BitmapFont uiFont = FontFactory.create(assetManager, uiFamilies, Font.PLAIN, Math.round(40 * hudScale));
        BitmapFont boldFont = FontFactory.create(assetManager, new String[]{"Segoe UI", "Arial"}, Font.BOLD, Math.round(48 * hudScale));
        BitmapFont tableFont = FontFactory.create(assetManager, new String[]{"Georgia", "Times New Roman", "Serif"}, Font.BOLD, 64);

        VoxelFactory voxels = new VoxelFactory(assetManager);
        Room room = new Room(voxels);
        table = new TableLayout(voxels, tableFont);
        wheel = new RouletteWheel(voxels, tableFont);
        dealer = new Dealer(voxels, uiFont, wheel.getCenter(), SeatCameraState.SEAT_EYE);
        sounds = new SoundBank(this, RouletteWheel.SPIN_DURATION);
        crowd = new Crowd(voxels, table, wheel.getCenter());
        hall = new Hall(voxels, tableFont, sounds);
        effects = new WinEffects(voxels, assetManager);
        chips = new ChipStackView(table, voxels);
        rootNode.attachChild(room.getNode());
        rootNode.attachChild(table.getNode());
        rootNode.attachChild(wheel.getNode());
        rootNode.attachChild(dealer.getNode());
        rootNode.attachChild(chips.getNode());
        rootNode.attachChild(crowd.getNode());
        rootNode.attachChild(hall.getNode());
        rootNode.attachChild(effects.getNode());
        setupLights(room);

        GuiGlobals.initialize(this);
        hud = new HudState(assetManager, player, new HudState.Actions() {
            @Override
            public void spin() {
                RouletteApp.this.spin();
            }

            @Override
            public void undo() {
                RouletteApp.this.undo();
            }

            @Override
            public void clear() {
                RouletteApp.this.clearBets();
            }

            @Override
            public void repeat() {
                RouletteApp.this.repeat();
            }
        }, board::amountOn, smallFont, uiFont, boldFont);
        hud.setBalance(saldo, 0);
        stateManager.attach(hud);

        seat = new SeatCameraState();
        // etwas unterhalb der Mitte zielen, damit das Rad beim Zoomen ueber der Chipleiste liegt
        seat.setFocus(WHEEL_VIEW_TARGET, WHEEL_VIEW_EYE, WHEEL_FOCUS_FOV);
        seat.setDragListener(dragging -> hud.setDragging(dragging));
        // Gehen: Kollision gegen die echte Szene (der Kellner laeuft durch einen hindurch)
        seat.setCollision(new WalkCollision()
                .add(room.getNode()).add(table.getNode()).add(wheel.getNode()).add(dealer.getNode())
                .add(crowd.getNode()).add(hall.getNode()));
        seat.setStepListener(() -> sounds.playStep());
        seat.setWalkListener(walking -> {
            hud.setWalking(walking);
            if (walking) {
                hud.toast("Du bist aufgestanden – F bringt dich zurück an den Platz", HudState.TEXT, 3f);
            }
        });
        stateManager.attach(seat);

        selection = new BetSelectionState(table, seat);
        selection.setClickListener(this::placeChip);
        selection.setHoverListener(hud::setHover);
        selection.setBlockedByHud(hud::isOverHud);
        stateManager.attach(selection);

        PixelationState pixelation = new PixelationState(START_PIXEL_SIZE);
        pixelation.setListener(hud::setPixelSize);
        stateManager.attach(pixelation);

        sounds.startAmbience();
        crowd.setChipSound((position, volume) -> sounds.playChip(position, volume));
        inputManager.addMapping(MUTE, new KeyTrigger(KeyInput.KEY_M));
        inputManager.addListener((ActionListener) (name, pressed, tpf) -> {
            if (pressed) {
                hud.toast(sounds.toggleMute() ? "Ton aus" : "Ton an", HudState.TEXT, 1.2f);
            }
        }, MUTE);

        dealer.say("Willkommen, " + player.getName() + "! Faites vos jeux.", 6f);
        hud.toast("Chip wählen, Felder anklicken, dann DREHEN", HudState.TEXT, 5f);

        connectToServer();
    }

    @Override
    public void simpleUpdate(float tpf) {
        wheel.update(tpf);
        dealer.update(tpf);
        crowd.update(tpf);
        hall.update(tpf);
        effects.update(tpf);
        if (roundEndTimer > 0f) {
            roundEndTimer -= tpf;
            if (roundEndTimer <= 0f) {
                endRound();
            }
        }
    }

    private void setupLights(Room room) {
        rootNode.addLight(new AmbientLight(new ColorRGBA(0.32f, 0.30f, 0.30f, 1f)));
        // schwaches Richtungslicht, damit die Voxel-Seiten unterschiedlich hell sind
        rootNode.addLight(new DirectionalLight(new Vector3f(-0.3f, -1f, -0.45f).normalizeLocal(),
                new ColorRGBA(0.35f, 0.33f, 0.30f, 1f)));
        room.addLights(rootNode);
    }

    private void connectToServer() {
        network.submit(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(HOST, PORT), 2000);
                RoulettetableClientProxy connected = new RoulettetableClientProxy(socket);
                connected.enter(player);
                proxy = connected;
                enqueue(() -> hud.setConnectionStatus("Online: " + HOST + ":" + PORT, true));
            } catch (IOException | RuntimeException e) {
                System.err.println("Server " + HOST + ":" + PORT + " nicht erreichbar: " + e.getMessage());
                enqueue(() -> hud.setConnectionStatus("Offline - Wetten nur lokal", false));
            }
        });
    }

    // ---------------------------------------------------------------- Spielablauf

    private boolean bettingLocked() {
        if (roundInProgress) {
            hud.toast("Nichts erlaubt – warte auf die nächste Runde", HudState.BAD, 2f);
        }
        return roundInProgress;
    }

    /** Linksklick auf ein Feld: einen Chip des gewaehlten Werts setzen. */
    private void placeChip(String bet) {
        if (bettingLocked()) {
            return;
        }
        int chip = hud.getSelectedChip();
        if (chip > saldo) {
            hud.toast("Nicht genug Guthaben für einen " + HudState.euro(chip) + "-Chip", HudState.BAD, 2.5f);
            return;
        }
        saldo -= chip;
        board.place(bet, chip);
        sounds.playChip(table.getField(bet).topCenter(), 1f);
        // neue Runde: der Croupier nimmt den Gewinnmarker vom Tisch
        effects.hideDolly();
        refreshBets();
    }

    private void undo() {
        if (bettingLocked()) {
            return;
        }
        BetBoard.Placement placement = board.undo();
        if (placement != null) {
            saldo += placement.amount();
            refreshBets();
        }
    }

    private void clearBets() {
        if (bettingLocked()) {
            return;
        }
        saldo += board.clear();
        refreshBets();
    }

    /** Setzt die Wetten der letzten Runde erneut, sofern das Guthaben reicht. */
    private void repeat() {
        if (bettingLocked() || board.lastRound().isEmpty()) {
            return;
        }
        int total = board.lastRound().values().stream().mapToInt(Integer::intValue).sum();
        if (total > saldo) {
            hud.toast("Nicht genug Guthaben zum Wiederholen (" + HudState.euro(total) + ")", HudState.BAD, 2.5f);
            return;
        }
        board.lastRound().forEach((bet, amount) -> {
            saldo -= amount;
            board.place(bet, amount);
        });
        sounds.playChip(table.getField(board.bets().keySet().iterator().next()).topCenter(), 1f);
        effects.hideDolly();
        refreshBets();
    }

    private void spin() {
        if (bettingLocked()) {
            return;
        }
        if (board.isEmpty()) {
            hud.toast("Setze zuerst Chips auf den Tisch", HudState.BAD, 2f);
            return;
        }
        if (seat.isWalking()) {
            // wer unterwegs DREHEN drueckt, wird an den Tisch zurueckgebracht
            seat.sitDown();
            hud.fade();
        }
        roundInProgress = true;
        seat.setWalkAllowed(false);
        hud.setSpinning(true);
        hud.toast("Viel Erfolg!", HudState.GOLD, 2f);
        dealer.throwBall();
        crowd.onSpin();
        sounds.playSpin(wheel.getCenter(), 1f);
        // aufstehen und zum Rad gehen
        seat.focus();
        board.bets().forEach(this::sendToServer);

        // Der Server wertet (noch) nicht aus -> Ergebnis wird lokal gezogen
        int result = random.nextInt(37);
        wheel.spinTo(result, this::resolve);
    }

    /**
     * Ergebnis vom Server: Mit Serververbindung ist der Server fuer die Auszahlung zustaendig,
     * der Gewinn wird hier gutgeschrieben.
     */
    private void onServerResult(String resultMessage, int win) {
        if (hud == null) {
            return;
        }
        saldo += win;
        hud.setBalance(saldo, board.total());
        hud.showTableMessage(resultMessage + (win > 0 ? "  ·  Gewinn " + HudState.euro(win) : ""));
        if (win > 0) {
            hud.toast("Auszahlung vom Server: " + HudState.euro(win), HudState.GOOD, 3f);
            sounds.playWin();
        }
    }

    private void resolve(int number) {
        int stake = board.total();
        int payout = board.payout(number);
        int net = payout - stake;
        // Online zahlt der Server ueber hearResults aus; nur offline wertet die App selbst aus
        boolean serverPays = proxy != null;
        if (!serverPays) {
            saldo += payout;
        }

        String result = number + " " + RouletteRules.colorName(number).toUpperCase(Locale.ROOT);
        hud.addResult(number);
        hud.addRound(result + "  ·  Einsatz " + HudState.euro(stake) + "  ·  "
                + (net >= 0 ? "+" : "") + HudState.euro(net), net);
        hud.toast(payout > 0 ? result + "   Gewinn " + HudState.euro(payout) : result + "   Leider verloren",
                payout > 0 ? HudState.GOOD : HudState.TEXT, ROUND_END_DELAY + 1f);
        dealer.announce(number + " " + RouletteRules.colorName(number) + "!" + (payout > 0 ? " Glückwunsch!" : ""));
        crowd.onResult(number);
        effects.showDolly(table.getField(String.valueOf(number)).topCenter());
        if (payout > 0) {
            sounds.playWin();
            crowd.cheerForPlayer();
            // Funkenregen ueber dem ersten eigenen Gewinnfeld
            board.bets().keySet().stream()
                    .filter(bet -> RouletteRules.wins(bet, number))
                    .findFirst()
                    .ifPresent(bet -> effects.burst(table.getField(bet).topCenter()));
        }
        hud.setBalance(saldo, 0);
        // Chips bleiben kurz liegen, damit man das Ergebnis sieht
        roundEndTimer = ROUND_END_DELAY;
    }

    private void endRound() {
        board.finishRound();
        roundInProgress = false;
        seat.unfocus();
        seat.setWalkAllowed(true);
        crowd.onRoundEnd();
        hud.setSpinning(false);
        refreshBets();
    }

    private void refreshBets() {
        chips.show(board.bets());
        hud.setBalance(saldo, board.total());
        hud.setCanRepeat(!board.lastRound().isEmpty());
    }

    /** Schickt eine Wette ueber die Middleware: {@code proxy.post(player, bet, stake)}. */
    private void sendToServer(String bet, int stake) {
        RoulettetableClientProxy connected = proxy;
        if (connected == null) {
            return;
        }
        network.submit(() -> {
            try {
                connected.post(player, bet, stake);
            } catch (RuntimeException e) {
                enqueue(() -> hud.showTableMessage("Serverfehler: " + e.getMessage()));
            }
        });
    }

    @Override
    public void destroy() {
        RoulettetableClientProxy connected = proxy;
        if (connected != null) {
            network.submit(connected::disconnect);
        }
        network.shutdown();
        try {
            network.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        super.destroy();
        // Der PlayerServerProxy-Thread der Middleware ist kein Daemon und blockiert im readLine(),
        // solange der Server die Callback-Verbindung offen haelt -> JVM explizit beenden.
        System.exit(0);
    }
}

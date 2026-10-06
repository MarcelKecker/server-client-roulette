/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import jme3tools.optimize.GeometryBatchFactory;
import praesentation.VoxelFactory.Pattern;
import praesentation.VoxelPerson.Hair;
import praesentation.VoxelPerson.Look;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Die Spielhalle hinter dem eigenen Tisch: ein zweiter Roulettetisch, zwei Blackjack-Tische, ein
 * Pokertisch, Reihen von Spielautomaten an den Waenden, eine Kasse, Kronleuchter und Tischlampen.
 * Croupiers geben Karten und drehen das Rad, Gaeste spielen, jubeln und ziehen an Automaten.
 */
public class Hall {
    private static final float TOP = TableLayout.TABLE_TOP;
    private static final ColorRGBA LEATHER = new ColorRGBA(0.30f, 0.14f, 0.07f, 1f);
    private static final ColorRGBA BRASS = new ColorRGBA(0.85f, 0.66f, 0.28f, 1f);
    private static final ColorRGBA BLUE_FELT = new ColorRGBA(0.06f, 0.22f, 0.45f, 1f);
    private static final ColorRGBA CARD = new ColorRGBA(0.96f, 0.96f, 0.93f, 1f);
    private static final ColorRGBA CARD_BACK = new ColorRGBA(0.62f, 0.08f, 0.12f, 1f);
    private static final ColorRGBA LIGHT_WARM = new ColorRGBA(1f, 0.88f, 0.55f, 1f);
    private static final ColorRGBA SHOES = new ColorRGBA(0.06f, 0.05f, 0.05f, 1f);
    private static final ColorRGBA WHITE_SHIRT = new ColorRGBA(0.94f, 0.94f, 0.92f, 1f);
    private static final ColorRGBA[] SKINS = {
            new ColorRGBA(0.95f, 0.78f, 0.64f, 1f), new ColorRGBA(0.82f, 0.62f, 0.46f, 1f),
            new ColorRGBA(0.60f, 0.42f, 0.30f, 1f), new ColorRGBA(0.42f, 0.28f, 0.19f, 1f)};
    private static final ColorRGBA[] HAIRS = {
            new ColorRGBA(0.85f, 0.70f, 0.38f, 1f), new ColorRGBA(0.30f, 0.18f, 0.09f, 1f),
            new ColorRGBA(0.08f, 0.07f, 0.07f, 1f), new ColorRGBA(0.70f, 0.70f, 0.70f, 1f),
            new ColorRGBA(0.62f, 0.22f, 0.08f, 1f)};
    private static final ColorRGBA[] CLOTHES = {
            new ColorRGBA(0.55f, 0.10f, 0.35f, 1f), new ColorRGBA(0.12f, 0.13f, 0.18f, 1f),
            new ColorRGBA(0.85f, 0.55f, 0.15f, 1f), new ColorRGBA(0.10f, 0.35f, 0.55f, 1f),
            new ColorRGBA(0.20f, 0.45f, 0.25f, 1f), new ColorRGBA(0.60f, 0.12f, 0.12f, 1f),
            new ColorRGBA(0.85f, 0.80f, 0.70f, 1f), new ColorRGBA(0.35f, 0.20f, 0.45f, 1f)};
    private static final ColorRGBA[] SYMBOLS = {
            new ColorRGBA(1f, 0.2f, 0.2f, 1f), new ColorRGBA(1f, 0.85f, 0.2f, 1f), new ColorRGBA(0.3f, 1f, 0.4f, 1f),
            new ColorRGBA(0.3f, 0.6f, 1f, 1f), new ColorRGBA(0.85f, 0.35f, 1f, 1f)};

    private final VoxelFactory voxels;
    private final SoundBank sounds;
    private final Node node = new Node("Halle");
    private final Node statics = new Node("Halle statisch");
    private final Random random = new Random(11);
    private final List<Runnable> updaters = new ArrayList<>();
    private float tpf;
    private float time;
    private int lookIndex;

    public Hall(VoxelFactory voxels, BitmapFont font, SoundBank sounds) {
        this.voxels = voxels;
        this.sounds = sounds;
        node.attachChild(statics);
        buildRouletteTable(font, -2.3f, -9.0f);
        buildBlackjackTable(2.35f, -8.6f, 4);
        buildBlackjackTable(2.35f, -12.2f, 3);
        buildPokerTable(-2.3f, -12.3f);
        float[] slotRows = {-7.15f, -7.75f, -8.35f, -9.65f, -10.25f, -10.85f};
        for (int i = 0; i < slotRows.length; i++) {
            buildSlotMachine(-1, slotRows[i], i == 1 || i == 5);
            buildSlotMachine(1, slotRows[i], i == 3);
        }
        buildCashier();
        // so gehaengt, dass sie vom Platz aus nicht vor dem CASINO-Schild haengen
        chandelier(0f, -6.6f);
        chandelier(-1.5f, -10.8f);
        chandelier(1.5f, -10.8f);
        GeometryBatchFactory.optimize(statics);
    }

    public Node getNode() {
        return node;
    }

    public void update(float tpf) {
        this.tpf = tpf;
        time += tpf;
        for (Runnable updater : updaters) {
            updater.run();
        }
    }

    // ---------------------------------------------------------------- Roulette

    private void buildRouletteTable(BitmapFont font, float x, float z) {
        float width = 1.4f;
        float length = 2.3f;
        tableBody(x, z, width, length, VoxelFactory.FELT);
        lamp(x, z - 0.3f, 1.1f);
        // kleines Zahlenfeld
        box(x, TOP + 0.002f, z + 0.45f, 0.34f, 0.004f, 1.02f, VoxelFactory.LAYOUT_LINES, Pattern.NOISE);
        for (int row = 0; row < 12; row++) {
            for (int column = 0; column < 3; column++) {
                int number = 3 * row + column + 1;
                box(x - 0.1f + column * 0.1f, TOP + 0.008f, z + 0.92f - row * 0.08f, 0.092f, 0.01f, 0.072f,
                        RouletteRules.isRed(number) ? VoxelFactory.RED : VoxelFactory.BLACK, Pattern.NOISE);
            }
        }
        box(x, TOP + 0.008f, z - 0.04f, 0.292f, 0.01f, 0.072f, VoxelFactory.FELT_LIGHT, Pattern.NOISE);
        box(x, TOP + 0.13f, z - 0.62f, 0.4f, 0.26f, 0.4f, VoxelFactory.WOOD_DARK, Pattern.WOOD);

        RouletteWheel wheel = new RouletteWheel(voxels, font);
        wheel.getNode().setLocalTranslation(x, TOP + 0.36f, z - 0.62f);
        node.attachChild(wheel.getNode());
        Vector3f wheelCenter = wheel.getNode().getLocalTranslation().clone();

        VoxelPerson dealer = standing(dealerLook(), x, z - 1.55f, 0f);
        stool(x - 1.05f, z - 0.1f, 0.66f);
        stool(x - 1.05f, z + 0.6f, 0.66f);
        stool(x + 1.05f, z + 0.25f, 0.66f);
        List<Guest> guests = List.of(
                seatedGuest(x - 1.05f, z - 0.1f, FastMath.HALF_PI, 0.68f),
                seatedGuest(x - 1.05f, z + 0.6f, FastMath.HALF_PI, 0.68f),
                seatedGuest(x + 1.05f, z + 0.25f, -FastMath.HALF_PI, 0.68f));
        float[] state = {8f + random.nextFloat() * 6f, -1f};
        updaters.add(() -> {
            // Zyklus: warten -> Kugel werfen (Rad dreht 6 s) -> Ergebnis, Gaeste reagieren
            state[0] -= tpf;
            if (state[0] <= 0f && !wheel.isSpinning()) {
                state[1] = 0f;
                sounds.playSpin(wheelCenter, 0.7f);
                wheel.spinTo(random.nextInt(37), number -> {
                    for (Guest guest : guests) {
                        guest.react(random.nextFloat() < 0.3f ? 1 : -1);
                    }
                    state[0] = 12f + random.nextFloat() * 10f;
                });
            }
            wheel.update(tpf);
            VoxelPerson.Pose pose = dealer.pose;
            dealer.resetPose();
            pose.leftArm = -0.55f;
            pose.rightArm = -0.55f;
            pose.headPitch = 0.3f;
            pose.headYaw = 0.4f * FastMath.sin(time * 0.4f);
            if (state[1] >= 0f) {
                state[1] += tpf;
                pose.rightArm = state[1] < 0.5f ? -1.8f : -0.9f;
                pose.headYaw = 0f;
                pose.headPitch = 0.45f;
                if (state[1] > 1f) {
                    state[1] = -1f;
                }
            }
            dealer.update(tpf);
            for (Guest guest : guests) {
                guest.update(wheel.isSpinning() ? wheelCenter : null);
            }
        });
    }

    // ---------------------------------------------------------------- Blackjack

    private void buildBlackjackTable(float x, float z, int players) {
        float radius = 0.95f;
        float centerZ = z - 0.35f;
        // Halbmond aus Stufen, darunter Holz, aussen Lederpolster
        for (float dz = 0f; dz < radius; dz += 0.05f) {
            float half = (float) Math.sqrt(radius * radius - dz * dz);
            box(x, TOP - 0.025f, centerZ + dz + 0.025f, 2 * half, 0.05f, 0.05f, BLUE_FELT, Pattern.FELT);
            box(x, TOP - 0.1f, centerZ + dz + 0.025f, 2 * half + 0.1f, 0.12f, 0.05f, VoxelFactory.WOOD_DARK, Pattern.WOOD);
            for (float side : new float[]{-1f, 1f}) {
                box(x + side * (half + 0.05f), TOP + 0.04f, centerZ + dz + 0.025f, 0.11f, 0.07f, 0.055f, LEATHER, Pattern.FELT);
            }
            // goldene Versicherungslinie
            float inner = 0.55f;
            if (dz < inner) {
                float lineHalf = (float) Math.sqrt(inner * inner - dz * dz);
                for (float side : new float[]{-1f, 1f}) {
                    box(x + side * lineHalf, TOP + 0.002f, centerZ + dz + 0.025f, 0.018f, 0.004f, 0.05f, BRASS, Pattern.METAL);
                }
            }
        }
        box(x, TOP + 0.04f, centerZ + radius + 0.03f, 0.2f, 0.07f, 0.08f, LEATHER, Pattern.FELT);
        box(x, TOP + 0.01f, centerZ - 0.04f, 2 * radius + 0.12f, 0.06f, 0.08f, VoxelFactory.WOOD, Pattern.WOOD);
        box(x, (TOP - 0.16f) / 2f, centerZ + 0.3f, 0.9f, TOP - 0.16f, 0.5f, VoxelFactory.WOOD_DARK, Pattern.PLANKS);
        // Chip-Tray, Kartenschuh, Ablage
        box(x, TOP + 0.02f, centerZ + 0.1f, 0.6f, 0.04f, 0.14f, VoxelFactory.WOOD_DARK, Pattern.WOOD);
        for (int i = 0; i < 6; i++) {
            box(x - 0.25f + i * 0.1f, TOP + 0.05f, centerZ + 0.1f, 0.07f, 0.03f, 0.11f,
                    ChipStackView.color(ChipStackView.DENOMINATIONS[i]), Pattern.PLANKS);
        }
        box(x + 0.5f, TOP + 0.05f, centerZ + 0.15f, 0.12f, 0.1f, 0.2f, CARD_BACK, Pattern.NOISE);
        box(x - 0.5f, TOP + 0.03f, centerZ + 0.15f, 0.1f, 0.06f, 0.14f, new ColorRGBA(0.8f, 0.85f, 0.9f, 1f), Pattern.METAL);
        lamp(x, centerZ + 0.35f, 0.9f);

        float[] angles = {-55f, -18f, 18f, 55f};
        List<Guest> guests = new ArrayList<>();
        List<Node> cards = new ArrayList<>();
        List<Vector3f> seats = new ArrayList<>();
        for (int i = 0; i < players; i++) {
            float angle = angles[i] * FastMath.DEG_TO_RAD;
            float sx = x + 1.2f * FastMath.sin(angle);
            float sz = centerZ + 1.2f * FastMath.cos(angle);
            stool(sx, sz, 0.66f);
            guests.add(seatedGuest(sx, sz, angle + FastMath.PI, 0.68f));
            seats.add(new Vector3f(sx, TOP, sz));
            // Wettfeld und zwei Karten vor dem Spieler
            float bx = x + 0.68f * FastMath.sin(angle);
            float bz = centerZ + 0.68f * FastMath.cos(angle);
            box(bx, TOP + 0.002f, bz, 0.1f, 0.004f, 0.1f, new ColorRGBA(0.85f, 0.85f, 0.9f, 1f), Pattern.NOISE);
            box(bx, TOP + 0.006f, bz, 0.07f, 0.006f, 0.07f, BLUE_FELT, Pattern.FELT);
            box(bx, TOP + 0.02f, bz, 0.045f, 0.03f, 0.045f, ChipStackView.color(new int[]{5, 10, 25, 100}[i % 4]), Pattern.PLANKS);
            float cx = x + 0.5f * FastMath.sin(angle);
            float cz = centerZ + 0.5f * FastMath.cos(angle);
            cards.add(card(cx - 0.02f, cz, true));
            cards.add(card(cx + 0.02f, cz + 0.015f, true));
        }
        cards.add(card(x - 0.03f, centerZ + 0.3f, true));
        cards.add(card(x + 0.03f, centerZ + 0.3f, false));

        VoxelPerson dealer = standing(dealerLook(), x, centerZ - 0.3f, 0f);
        float[] cycle = {random.nextFloat() * 6f};
        int[] lastDealt = {0};
        updaters.add(() -> {
            // 0-4 s: Karten austeilen, 4-10 s: Spieler ueberlegen, 10-11 s: abraeumen
            cycle[0] = (cycle[0] + tpf) % 11f;
            float t = cycle[0];
            int dealt = (int) Math.min(cards.size(), t / 4f * cards.size());
            if (dealt > lastDealt[0] && t < 10f) {
                sounds.playCard(cards.get(dealt - 1).getLocalTranslation());
            }
            lastDealt[0] = t < 10f ? dealt : 0;
            for (int i = 0; i < cards.size(); i++) {
                cards.get(i).setCullHint(i < dealt && t < 10f ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
            }
            VoxelPerson.Pose pose = dealer.pose;
            dealer.resetPose();
            pose.leftArm = -0.7f;
            pose.rightArm = -0.6f;
            pose.headPitch = 0.3f;
            if (t < 4f && dealt < cards.size()) {
                // zum naechsten Spieler austeilen
                int target = Math.min(dealt / 2, seats.size() - 1);
                Vector3f seat = seats.get(target);
                float yaw = FastMath.atan2(seat.x - x, seat.z - (centerZ - 0.3f));
                pose.headYaw = FastMath.clamp(yaw, -1f, 1f);
                pose.rightArm = -1.3f + 0.2f * FastMath.sin(time * 9f);
                pose.rightArmOut = -yaw * 0.6f;
            } else if (t > 10f) {
                pose.leftArm = -1.2f;
                pose.rightArm = -1.2f;
            } else {
                pose.headYaw = 0.6f * FastMath.sin(time * 0.5f);
            }
            dealer.update(tpf);
            for (int i = 0; i < guests.size(); i++) {
                Guest guest = guests.get(i);
                if (t > 4.5f && t < 10f && ((int) (t * 0.7f) + i) % 3 == 0) {
                    guest.tap();
                }
                guest.update(null);
            }
        });
    }

    // ---------------------------------------------------------------- Poker

    private void buildPokerTable(float x, float z) {
        float a = 1.0f;
        float b = 0.6f;
        for (float dz = -b; dz < b - 0.001f; dz += 0.05f) {
            float mid = dz + 0.025f;
            float half = a * (float) Math.sqrt(Math.max(0f, 1f - (mid / b) * (mid / b)));
            box(x, TOP - 0.025f, z + mid, 2 * half, 0.05f, 0.05f, VoxelFactory.FELT, Pattern.FELT);
            box(x, TOP - 0.1f, z + mid, 2 * half + 0.1f, 0.12f, 0.05f, VoxelFactory.WOOD_DARK, Pattern.WOOD);
            for (float side : new float[]{-1f, 1f}) {
                box(x + side * (half + 0.05f), TOP + 0.04f, z + mid, 0.11f, 0.07f, 0.055f, LEATHER, Pattern.FELT);
            }
        }
        box(x, TOP + 0.04f, z - b - 0.03f, 0.35f, 0.07f, 0.07f, LEATHER, Pattern.FELT);
        box(x, TOP + 0.04f, z + b + 0.03f, 0.35f, 0.07f, 0.07f, LEATHER, Pattern.FELT);
        box(x, (TOP - 0.16f) / 2f, z, 0.9f, TOP - 0.16f, 0.5f, VoxelFactory.WOOD_DARK, Pattern.PLANKS);
        lamp(x, z, 1.1f);

        // Pot in der Mitte
        for (int i = 0; i < 5; i++) {
            float height = 0.03f + (i % 3) * 0.02f;
            box(x - 0.1f + i * 0.05f, TOP + height / 2f, z + 0.12f, 0.04f, height, 0.04f,
                    ChipStackView.color(ChipStackView.DENOMINATIONS[i + 1]), Pattern.PLANKS);
        }
        List<Node> community = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            community.add(card(x - 0.2f + i * 0.1f, z - 0.08f, true));
        }

        float[] angles = {-115f, -60f, 0f, 60f, 115f};
        List<Guest> guests = new ArrayList<>();
        for (int i = 0; i < angles.length; i++) {
            float angle = angles[i] * FastMath.DEG_TO_RAD;
            float sx = x + (a + 0.4f) * FastMath.sin(angle);
            float sz = z + (b + 0.4f) * FastMath.cos(angle);
            chair(sx, sz, FastMath.atan2(x - sx, z - sz));
            guests.add(seatedGuest(sx, sz, FastMath.atan2(x - sx, z - sz), 0.50f));
            // verdeckte Karten und Chips vor dem Spieler
            float cx = x + (a - 0.3f) * FastMath.sin(angle);
            float cz = z + (b - 0.2f) * FastMath.cos(angle);
            card(cx - 0.02f, cz, false);
            card(cx + 0.02f, cz + 0.01f, false);
            float px = x + (a - 0.12f) * FastMath.sin(angle);
            float pz = z + (b - 0.08f) * FastMath.cos(angle);
            for (int c = 0; c < 3; c++) {
                float height = 0.02f + ((i + c) % 3) * 0.015f;
                box(px + (c - 1) * 0.045f, TOP + height / 2f, pz, 0.04f, height, 0.04f,
                        ChipStackView.color(ChipStackView.DENOMINATIONS[(i + c) % 5 + 1]), Pattern.PLANKS);
            }
        }
        VoxelPerson dealer = standing(dealerLook(), x, z - b - 0.45f, 0f);
        float[] cycle = {random.nextFloat() * 20f};
        int[] lastShown = {0};
        updaters.add(() -> {
            // Flop, Turn, River nacheinander aufdecken, dann neue Hand
            cycle[0] = (cycle[0] + tpf) % 20f;
            float t = cycle[0];
            int shown = t < 3f ? 0 : t < 8f ? 3 : t < 12f ? 4 : 5;
            if (shown != lastShown[0]) {
                if (shown > lastShown[0]) {
                    sounds.playCard(community.get(shown - 1).getLocalTranslation());
                }
                lastShown[0] = shown;
            }
            for (int i = 0; i < community.size(); i++) {
                community.get(i).setCullHint(i < shown && t < 18f ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
            }
            VoxelPerson.Pose pose = dealer.pose;
            dealer.resetPose();
            pose.leftArm = -0.8f;
            pose.rightArm = (t > 2.5f && t < 3.5f) || (t > 7.5f && t < 8.5f) || (t > 11.5f && t < 12.5f) ? -1.35f : -0.8f;
            pose.headPitch = 0.35f;
            pose.headYaw = 0.7f * FastMath.sin(time * 0.35f);
            dealer.update(tpf);
            for (int i = 0; i < guests.size(); i++) {
                Guest guest = guests.get(i);
                if (t > 18f && t < 18.2f && i == (int) (time / 20f) % guests.size()) {
                    guest.react(1);
                }
                if (((int) (t * 0.5f) + i) % 4 == 0) {
                    guest.tap();
                }
                guest.update(null);
            }
        });
    }

    // ---------------------------------------------------------------- Spielautomaten

    /**
     * Automat an der Wand; {@code side} -1 = linke Wand, 1 = rechte Wand. Walzen und Toplicht leuchten
     * und laufen; mit {@code occupied} sitzt ein Gast davor und spielt.
     */
    private void buildSlotMachine(int side, float z, boolean occupied) {
        float x = side * 4.61f;
        float front = -side;
        boolean redCabinet = random.nextBoolean();
        ColorRGBA cabinet = redCabinet ? new ColorRGBA(0.45f, 0.05f, 0.08f, 1f) : new ColorRGBA(0.10f, 0.10f, 0.14f, 1f);
        box(x, 0.45f, z, 0.5f, 0.9f, 0.5f, cabinet, Pattern.NOISE);
        box(x - front * 0.04f, 1.28f, z, 0.42f, 0.76f, 0.48f, cabinet, Pattern.NOISE);
        box(x + front * 0.26f, 0.9f, z, 0.04f, 0.04f, 0.52f, BRASS, Pattern.METAL);
        box(x + front * 0.18f, 1.3f, z, 0.012f, 0.4f, 0.44f, BRASS, Pattern.METAL);
        box(x + front * 0.186f, 1.3f, z, 0.012f, 0.34f, 0.4f, VoxelFactory.BLACK, Pattern.NOISE);
        box(x + front * 0.2f, 0.88f, z - 0.3f + 0.05f, 0.04f, 0.4f, 0.04f, new ColorRGBA(0.7f, 0.7f, 0.75f, 1f), Pattern.METAL);
        box(x + front * 0.2f, 1.1f, z - 0.25f, 0.07f, 0.07f, 0.07f, VoxelFactory.RED, Pattern.METAL);
        // Tasten
        for (int i = 0; i < 3; i++) {
            node.attachChild(voxels.glowBox("Taste", x + front * 0.25f, 0.93f, z - 0.12f + i * 0.12f, 0.03f, 0.015f, 0.07f,
                    i == 1 ? new ColorRGBA(1f, 0.3f, 0.2f, 1f) : new ColorRGBA(1f, 0.85f, 0.3f, 1f)));
        }
        Material[] reels = new Material[3];
        for (int i = 0; i < 3; i++) {
            reels[i] = glowMaterial(SYMBOLS[random.nextInt(SYMBOLS.length)]);
            Geometry reel = new Geometry("Walze", new Box(0.006f, 0.1f, 0.055f));
            reel.setMaterial(reels[i]);
            reel.setLocalTranslation(x + front * 0.195f, 1.3f, z + (i - 1) * 0.13f);
            node.attachChild(reel);
        }
        Material sign = glowMaterial(redCabinet ? new ColorRGBA(1f, 0.8f, 0.2f, 1f) : new ColorRGBA(0.3f, 0.8f, 1f, 1f));
        Geometry header = new Geometry("Automatenschild", new Box(0.01f, 0.07f, 0.22f));
        header.setMaterial(sign);
        header.setLocalTranslation(x + front * 0.175f, 1.72f, z);
        node.attachChild(header);
        Material topLight = glowMaterial(ColorRGBA.White);
        Geometry top = new Geometry("Toplicht", new Box(0.07f, 0.05f, 0.07f));
        top.setMaterial(topLight);
        top.setLocalTranslation(x - front * 0.04f, 1.72f + 0.12f, z);
        node.attachChild(top);
        stool(x + front * 0.78f, z, 0.62f);

        // der Gast schaut zum Automaten (also entgegen der Front-Richtung)
        Guest player = occupied ? seatedGuest(x + front * 0.78f, z, front > 0 ? -FastMath.HALF_PI : FastMath.HALF_PI, 0.64f) : null;
        float[] state = {random.nextFloat() * 6f, -1f, 0f, 0f};
        Vector3f position = new Vector3f(x, 1.3f, z);
        ColorRGBA topA = new ColorRGBA(1f, 0.25f, 0.2f, 1f);
        ColorRGBA topB = new ColorRGBA(1f, 0.85f, 0.3f, 1f);
        updaters.add(() -> {
            state[0] -= tpf;
            if (state[0] <= 0f && state[1] < 0f) {
                state[1] = 0f;
                sounds.playSlotSpin(position);
                state[0] = occupied ? 3.5f + random.nextFloat() * 3f : 8f + random.nextFloat() * 14f;
            }
            if (state[1] >= 0f) {
                state[1] += tpf;
                if (state[1] < 1.3f) {
                    for (Material reel : reels) {
                        reel.setColor("Color", SYMBOLS[random.nextInt(SYMBOLS.length)]);
                    }
                } else {
                    // Stopp: gelegentlich drei gleiche -> Gewinnlicht
                    boolean jackpot = random.nextFloat() < 0.12f;
                    ColorRGBA first = SYMBOLS[random.nextInt(SYMBOLS.length)];
                    for (Material reel : reels) {
                        reel.setColor("Color", jackpot ? first : SYMBOLS[random.nextInt(SYMBOLS.length)]);
                    }
                    state[2] = jackpot ? 2.5f : 0f;
                    if (jackpot) {
                        sounds.playSlotWin(position);
                        if (player != null) {
                            player.react(1);
                        }
                    }
                    state[1] = -1f;
                }
            }
            state[2] = Math.max(0f, state[2] - tpf);
            boolean fast = state[2] > 0f;
            boolean phase = ((int) (time * (fast ? 8f : 1.5f) + z * 3f)) % 2 == 0;
            topLight.setColor("Color", phase ? topA : topB);
            if (player != null) {
                // Hebel ziehen am Anfang einer Runde
                player.pullLever(state[1] >= 0f && state[1] < 0.4f);
                player.update(null);
            }
        });
    }

    // ---------------------------------------------------------------- Kasse, Leuchter, Moebel

    private void buildCashier() {
        float z = Room.Z_BACK + 0.55f;
        box(0f, 0.53f, z, 1.9f, 1.06f, 0.45f, VoxelFactory.WOOD_DARK, Pattern.PLANKS);
        box(0f, 1.08f, z, 2.0f, 0.05f, 0.52f, VoxelFactory.WOOD_LIGHT, Pattern.WOOD);
        for (float x = -0.9f; x <= 0.91f; x += 0.15f) {
            box(x, 1.55f, z + 0.18f, 0.02f, 0.9f, 0.02f, BRASS, Pattern.METAL);
        }
        box(0f, 2.05f, z + 0.1f, 2.0f, 0.22f, 0.3f, VoxelFactory.WOOD, Pattern.WOOD);
        box(0f, 2.18f, z + 0.1f, 2.05f, 0.04f, 0.34f, BRASS, Pattern.METAL);
        box(0f, 1.8f, Room.Z_BACK + 0.12f, 1.9f, 1.4f, 0.04f, new ColorRGBA(0.15f, 0.12f, 0.1f, 1f), Pattern.PLANKS);
        pixelText("KASSE", 0f, 2.12f, z + 0.26f, 0.035f);
        node.attachChild(voxels.glowBox("Kassenlicht", 0f, 1.95f, z, 1.6f, 0.02f, 0.2f, LIGHT_WARM));

        VoxelPerson cashier = standing(new Look(SKINS[0], HAIRS[1], Hair.BUN, WHITE_SHIRT, new ColorRGBA(0.1f, 0.1f, 0.12f, 1f),
                true, new ColorRGBA(0.1f, 0.1f, 0.12f, 1f), SHOES, VoxelFactory.RED, true, false), 0.3f, z - 0.27f, 0f);
        VoxelPerson customer = standing(look(), -0.25f, z + 0.65f, FastMath.PI);
        updaters.add(() -> {
            VoxelPerson.Pose pose = cashier.pose;
            cashier.resetPose();
            float t = time % 7f;
            pose.leftArm = -0.9f;
            pose.rightArm = t < 1.5f ? -1.4f : -0.9f;
            pose.headPitch = t < 3f ? 0.35f : 0f;
            pose.headYaw = t < 3f ? 0f : -0.4f;
            cashier.update(tpf);
            VoxelPerson.Pose other = customer.pose;
            customer.resetPose();
            other.rightArm = t > 1.5f && t < 2.5f ? -1.3f : 0f;
            other.headPitch = 0.1f;
            other.headYaw = 0.2f * FastMath.sin(time * 0.6f);
            customer.update(tpf);
        });
    }

    /** Kronleuchter: Messingring mit leuchtenden Kristallen und Mittelstange. */
    private void chandelier(float x, float z) {
        float y = Room.HEIGHT - 0.4f;
        box(x, (Room.HEIGHT + y) / 2f, z, 0.04f, Room.HEIGHT - y, 0.04f, BRASS, Pattern.METAL);
        for (int i = 0; i < 12; i++) {
            float angle = i * FastMath.TWO_PI / 12f;
            float cx = x + 0.42f * FastMath.sin(angle);
            float cz = z + 0.42f * FastMath.cos(angle);
            box(cx, y, cz, 0.12f, 0.03f, 0.12f, BRASS, Pattern.METAL);
            node.attachChild(voxels.glowBox("Kristall", cx, y - 0.08f - (i % 2) * 0.05f, cz, 0.04f, 0.1f, 0.04f,
                    new ColorRGBA(1f, 0.95f, 0.8f, 1f)));
            node.attachChild(voxels.glowBox("Kerze", cx, y + 0.06f, cz, 0.03f, 0.07f, 0.03f, LIGHT_WARM));
        }
        for (int i = 0; i < 6; i++) {
            float angle = i * FastMath.TWO_PI / 6f;
            box(x + 0.21f * FastMath.sin(angle), y - 0.15f, z + 0.21f * FastMath.cos(angle), 0.03f, 0.25f, 0.03f, BRASS, Pattern.METAL);
        }
        node.attachChild(voxels.glowBox("Kristallkugel", x, y - 0.3f, z, 0.12f, 0.12f, 0.12f, new ColorRGBA(1f, 0.97f, 0.88f, 1f)));
    }

    /** Tischlampe ueber einem Tisch (wie ueber dem eigenen, etwas kleiner). */
    private void lamp(float x, float z, float width) {
        float y = 2.45f;
        box(x, (Room.HEIGHT + y) / 2f, z, 0.03f, Room.HEIGHT - y, 0.03f, new ColorRGBA(0.55f, 0.55f, 0.58f, 1f), Pattern.METAL);
        box(x, y, z, width, 0.1f, 0.34f, new ColorRGBA(0.06f, 0.32f, 0.16f, 1f), Pattern.FELT);
        box(x, y - 0.06f, z, width + 0.04f, 0.025f, 0.38f, BRASS, Pattern.METAL);
        node.attachChild(voxels.glowBox("Lampenlicht", x, y - 0.08f, z, width - 0.2f, 0.02f, 0.2f, LIGHT_WARM));
    }

    private void tableBody(float x, float z, float width, float length, ColorRGBA felt) {
        box(x, TOP - 0.025f, z, width, 0.05f, length, felt, Pattern.FELT);
        box(x, TOP - 0.1f, z, width + 0.3f, 0.12f, length + 0.3f, VoxelFactory.WOOD_DARK, Pattern.WOOD);
        for (float side : new float[]{-1f, 1f}) {
            box(x + side * (width / 2f + 0.075f), TOP + 0.01f, z, 0.15f, 0.04f, length, VoxelFactory.WOOD, Pattern.WOOD);
            box(x + side * (width / 2f + 0.075f), TOP + 0.055f, z, 0.13f, 0.06f, length - 0.02f, LEATHER, Pattern.FELT);
            box(x, TOP + 0.01f, z + side * (length / 2f + 0.075f), width + 0.3f, 0.04f, 0.15f, VoxelFactory.WOOD, Pattern.WOOD);
            box(x, TOP + 0.055f, z + side * (length / 2f + 0.075f), width + 0.28f, 0.06f, 0.13f, LEATHER, Pattern.FELT);
        }
        box(x, (TOP - 0.16f) / 2f, z, width * 0.6f, TOP - 0.16f, length * 0.7f, VoxelFactory.WOOD_DARK, Pattern.PLANKS);
        box(x, 0.03f, z, width * 0.7f, 0.06f, length * 0.75f, BRASS, Pattern.METAL);
    }

    private void stool(float x, float z, float height) {
        ColorRGBA seat = new ColorRGBA(0.55f, 0.08f, 0.10f, 1f);
        ColorRGBA metal = new ColorRGBA(0.55f, 0.55f, 0.58f, 1f);
        box(x, height - 0.04f, z, 0.36f, 0.08f, 0.36f, seat, Pattern.FELT);
        box(x, height - 0.09f, z, 0.38f, 0.03f, 0.38f, VoxelFactory.GOLD, Pattern.METAL);
        box(x, (height - 0.1f) / 2f, z, 0.07f, height - 0.1f, 0.07f, metal, Pattern.METAL);
        box(x, 0.02f, z, 0.32f, 0.04f, 0.32f, metal, Pattern.METAL);
    }

    private void chair(float x, float z, float yaw) {
        ColorRGBA seat = new ColorRGBA(0.55f, 0.08f, 0.10f, 1f);
        box(x, 0.46f, z, 0.45f, 0.08f, 0.45f, seat, Pattern.FELT);
        box(x, 0.21f, z, 0.36f, 0.42f, 0.36f, VoxelFactory.WOOD_DARK, Pattern.WOOD);
        box(x - 0.22f * FastMath.sin(yaw), 0.8f, z - 0.22f * FastMath.cos(yaw), 0.3f, 0.6f, 0.3f, seat, Pattern.FELT);
    }

    /** Spielkarte (offen: weiss mit Farbzeichen, verdeckt: rote Rueckseite); anfangs sichtbar. */
    private Node card(float x, float z, boolean faceUp) {
        Node card = new Node("Karte");
        card.setLocalTranslation(x, TOP + 0.004f, z);
        card.attachChild(voxels.box("Karte", 0f, 0f, 0f, 0.055f, 0.004f, 0.08f, faceUp ? CARD : CARD_BACK, Pattern.NOISE));
        if (faceUp) {
            ColorRGBA pip = random.nextBoolean() ? VoxelFactory.RED : VoxelFactory.BLACK;
            card.attachChild(voxels.box("Kartenzeichen", 0f, 0.003f, 0f, 0.018f, 0.002f, 0.024f, pip, Pattern.NOISE));
            card.attachChild(voxels.box("Kartenecke", -0.018f, 0.003f, -0.028f, 0.008f, 0.002f, 0.012f, pip, Pattern.NOISE));
        } else {
            card.attachChild(voxels.box("Kartenrand", 0f, 0.003f, 0f, 0.045f, 0.002f, 0.07f, new ColorRGBA(0.85f, 0.75f, 0.4f, 1f), Pattern.NOISE));
            card.attachChild(voxels.box("Kartenmuster", 0f, 0.004f, 0f, 0.04f, 0.002f, 0.065f, CARD_BACK, Pattern.NOISE));
        }
        node.attachChild(card);
        return card;
    }

    /** Leuchtende Pixel-Buchstaben (3x5), zentriert, in Blickrichtung +Z. */
    private void pixelText(String text, float centerX, float top, float z, float pixel) {
        String[][] font = {
                {"K", "101", "110", "100", "110", "101"},
                {"A", "010", "101", "111", "101", "101"},
                {"S", "111", "100", "111", "001", "111"},
                {"E", "111", "100", "110", "100", "111"}};
        int width = text.length() * 4 - 1;
        float left = centerX - width * pixel / 2f;
        for (int i = 0; i < text.length(); i++) {
            for (String[] glyph : font) {
                if (glyph[0].charAt(0) != text.charAt(i)) {
                    continue;
                }
                for (int row = 0; row < 5; row++) {
                    for (int column = 0; column < 3; column++) {
                        if (glyph[row + 1].charAt(column) == '1') {
                            node.attachChild(voxels.glowBox("Kassenschrift", left + (i * 4 + column + 0.5f) * pixel,
                                    top - (row + 0.5f) * pixel, z, pixel * 0.9f, pixel * 0.9f, 0.02f, VoxelFactory.GOLD));
                        }
                    }
                }
            }
        }
    }

    private Material glowMaterial(ColorRGBA color) {
        Material material = voxels.glow(ColorRGBA.White).clone();
        material.setColor("Color", color.clone());
        return material;
    }

    private void box(float x, float y, float z, float sx, float sy, float sz, ColorRGBA color, Pattern pattern) {
        statics.attachChild(voxels.box("Halle", x, y, z, sx, sy, sz, color, pattern));
    }

    // ---------------------------------------------------------------- Personen

    private Look dealerLook() {
        return new Look(SKINS[random.nextInt(SKINS.length)], HAIRS[1 + random.nextInt(3)], Hair.SHORT, WHITE_SHIRT,
                new ColorRGBA(0.45f, 0.06f, 0.10f, 1f), true, new ColorRGBA(0.1f, 0.1f, 0.12f, 1f), SHOES,
                VoxelFactory.BLACK, false, random.nextBoolean());
    }

    /** Zufaelliger Gast (abwechselnd Frau/Mann, verschiedene Farben). */
    private Look look() {
        boolean dress = lookIndex++ % 2 == 0;
        ColorRGBA clothes = CLOTHES[random.nextInt(CLOTHES.length)];
        ColorRGBA skin = SKINS[random.nextInt(SKINS.length)];
        ColorRGBA hair = HAIRS[random.nextInt(HAIRS.length)];
        if (dress) {
            return new Look(skin, hair, random.nextBoolean() ? Hair.LONG : Hair.BUN, clothes, null, false,
                    clothes, SHOES, null, true, false);
        }
        boolean suit = random.nextBoolean();
        return new Look(skin, hair, random.nextFloat() < 0.2f ? Hair.BALD : Hair.SHORT, suit ? WHITE_SHIRT : clothes,
                suit ? clothes : null, false, suit ? clothes : new ColorRGBA(0.18f, 0.2f, 0.28f, 1f), SHOES,
                suit && random.nextBoolean() ? VoxelFactory.RED : null, false, random.nextFloat() < 0.25f);
    }

    private VoxelPerson standing(Look look, float x, float z, float yaw) {
        VoxelPerson person = new VoxelPerson(voxels, look);
        person.setPosition(x, z, yaw);
        person.resetPose();
        person.snap();
        node.attachChild(person.getNode());
        return person;
    }

    private Guest seatedGuest(float x, float z, float yaw, float seatHeight) {
        VoxelPerson person = new VoxelPerson(voxels, look());
        person.setPosition(x, z, yaw);
        person.sit(seatHeight);
        person.resetPose();
        person.snap();
        node.attachChild(person.getNode());
        return new Guest(person, x, z, yaw);
    }

    /** Sitzender Gast mit kleinen Bewegungen und Reaktionen. */
    private final class Guest {
        final VoxelPerson person;
        final float x;
        final float z;
        final float yaw;
        final float phase = random.nextFloat() * 10f;
        float reactTime = -1f;
        int reaction;
        float tapTime = -1f;
        boolean lever;

        Guest(VoxelPerson person, float x, float z, float yaw) {
            this.person = person;
            this.x = x;
            this.z = z;
            this.yaw = yaw;
        }

        void react(int result) {
            reaction = result;
            reactTime = 0f;
        }

        void tap() {
            if (tapTime < 0f) {
                tapTime = 0f;
            }
        }

        void pullLever(boolean pulling) {
            lever = pulling;
        }

        void update(Vector3f watch) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            pose.leftArm = -0.9f;
            pose.rightArm = -0.9f;
            pose.headPitch = 0.3f;
            pose.headYaw = 0.35f * FastMath.sin(time * 0.33f + phase);
            if (watch != null) {
                float world = FastMath.atan2(watch.x - x, watch.z - z);
                float relative = world - yaw;
                while (relative > FastMath.PI) {
                    relative -= FastMath.TWO_PI;
                }
                while (relative < -FastMath.PI) {
                    relative += FastMath.TWO_PI;
                }
                pose.headYaw = FastMath.clamp(relative, -1.1f, 1.1f);
                pose.lean = 0.12f;
            }
            if (tapTime >= 0f) {
                tapTime += tpf;
                pose.rightArm = -1.3f + 0.12f * FastMath.sin(tapTime * 14f);
                if (tapTime > 0.8f) {
                    tapTime = -1f;
                }
            }
            if (lever) {
                pose.rightArm = -2.1f;
                pose.rightArmOut = 0.3f;
            }
            if (reactTime >= 0f) {
                reactTime += tpf;
                if (reaction > 0) {
                    pose.leftArm = -0.3f;
                    pose.rightArm = -0.3f;
                    pose.leftArmOut = 2.6f + 0.2f * FastMath.sin(reactTime * 12f);
                    pose.rightArmOut = 2.6f + 0.2f * FastMath.sin(reactTime * 12f + 1f);
                    pose.bob = 0.04f * FastMath.abs(FastMath.sin(reactTime * 8f));
                } else {
                    pose.headPitch = 0.6f;
                    pose.lean = 0.18f;
                }
                if (reactTime > 2.5f) {
                    reactTime = -1f;
                }
            }
            person.update(tpf);
        }
    }
}

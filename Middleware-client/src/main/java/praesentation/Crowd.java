/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import praesentation.VoxelFactory.Pattern;
import praesentation.VoxelPerson.Hair;
import praesentation.VoxelPerson.Look;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Leben im Casino: Mitspieler am Roulettetisch (setzen eigene Farbchips, schauen dem Rad zu, jubeln oder
 * sind enttaeuscht), ein Zuschauer, Barkeeper und Gaeste an der Bar, Kartenspieler, ein Paar im Gespraech
 * und ein Kellner, der mit Tablett durch den Raum laeuft.
 */
public class Crowd {
    private static final ColorRGBA[] SKINS = {
            new ColorRGBA(0.95f, 0.78f, 0.64f, 1f), new ColorRGBA(0.82f, 0.62f, 0.46f, 1f),
            new ColorRGBA(0.60f, 0.42f, 0.30f, 1f), new ColorRGBA(0.42f, 0.28f, 0.19f, 1f)};
    private static final ColorRGBA BLONDE = new ColorRGBA(0.85f, 0.70f, 0.38f, 1f);
    private static final ColorRGBA BROWN = new ColorRGBA(0.30f, 0.18f, 0.09f, 1f);
    private static final ColorRGBA BLACK_HAIR = new ColorRGBA(0.08f, 0.07f, 0.07f, 1f);
    private static final ColorRGBA GREY_HAIR = new ColorRGBA(0.70f, 0.70f, 0.70f, 1f);
    private static final ColorRGBA RED_HAIR = new ColorRGBA(0.62f, 0.22f, 0.08f, 1f);
    private static final ColorRGBA SUIT = new ColorRGBA(0.12f, 0.13f, 0.18f, 1f);
    private static final ColorRGBA SHOES = new ColorRGBA(0.06f, 0.05f, 0.05f, 1f);
    private static final ColorRGBA WHITE_SHIRT = new ColorRGBA(0.94f, 0.94f, 0.92f, 1f);

    private final VoxelFactory voxels;
    private final TableLayout table;
    private final Vector3f wheelCenter;
    private final Random random = new Random(7);
    private final Node node = new Node("Gaeste");
    private final List<TableGuest> tableGuests = new ArrayList<>();
    private final List<Behavior> behaviors = new ArrayList<>();
    private BiConsumer<Vector3f, Float> chipSound = (position, volume) -> {
    };
    private float time;
    private boolean bettingOpen = true;
    private boolean watching;

    /** Etwas, das jedes Frame weiterlaeuft. */
    private interface Behavior {
        void update(float tpf);
    }

    public Crowd(VoxelFactory voxels, TableLayout table, Vector3f wheelCenter) {
        this.voxels = voxels;
        this.table = table;
        this.wheelCenter = wheelCenter.clone();

        // Mitspieler am Roulettetisch (je eigene Chipfarbe, wie im echten Casino)
        tableGuests.add(new TableGuest(look(SKINS[1], BROWN, Hair.LONG, new ColorRGBA(0.55f, 0.10f, 0.35f, 1f), null,
                new ColorRGBA(0.55f, 0.10f, 0.35f, 1f), true), -1.45f, -1.2f, FastMath.HALF_PI,
                new ColorRGBA(0.95f, 0.45f, 0.75f, 1f), new Vector2f(-0.028f, 0.022f)));
        tableGuests.add(new TableGuest(look(SKINS[0], GREY_HAIR, Hair.SHORT, WHITE_SHIRT, SUIT, SUIT, false),
                -1.45f, -2.0f, FastMath.HALF_PI, new ColorRGBA(0.25f, 0.80f, 0.85f, 1f), new Vector2f(0.028f, 0.022f)));
        tableGuests.add(new TableGuest(look(SKINS[3], BLACK_HAIR, Hair.SHORT, new ColorRGBA(0.85f, 0.55f, 0.15f, 1f), null,
                new ColorRGBA(0.20f, 0.22f, 0.30f, 1f), false), 1.45f, -0.8f, -FastMath.HALF_PI,
                new ColorRGBA(1f, 0.6f, 0.15f, 1f), new Vector2f(0f, -0.03f)));
        behaviors.addAll(tableGuests);

        behaviors.add(new Spectator(look(SKINS[2], BLONDE, Hair.BUN, new ColorRGBA(0.10f, 0.35f, 0.55f, 1f), null,
                new ColorRGBA(0.10f, 0.35f, 0.55f, 1f), true), -1.95f, -1.6f));
        behaviors.add(new Bartender());
        behaviors.add(new BarGuest(look(SKINS[0], RED_HAIR, Hair.LONG, new ColorRGBA(0.1f, 0.5f, 0.3f, 1f), null,
                new ColorRGBA(0.1f, 0.5f, 0.3f, 1f), true), -3.35f, -3.85f, 0f, new ColorRGBA(0.9f, 0.3f, 0.4f, 1f)));
        behaviors.add(new BarGuest(look(SKINS[1], BROWN, Hair.BALD, new ColorRGBA(0.45f, 0.45f, 0.5f, 1f), SUIT,
                SUIT, false), -3.35f, -2.75f, 2.1f, new ColorRGBA(0.85f, 0.6f, 0.2f, 1f)));
        behaviors.add(new CardPlayer(look(SKINS[2], BLACK_HAIR, Hair.SHORT, new ColorRGBA(0.6f, 0.15f, 0.15f, 1f), null,
                new ColorRGBA(0.2f, 0.2f, 0.25f, 1f), false), 2.2f, -4.4f, FastMath.HALF_PI, 0f));
        behaviors.add(new CardPlayer(look(SKINS[0], BLONDE, Hair.SHORT, WHITE_SHIRT, new ColorRGBA(0.3f, 0.2f, 0.12f, 1f),
                new ColorRGBA(0.3f, 0.2f, 0.12f, 1f), false), 4.2f, -4.4f, -FastMath.HALF_PI, 1.7f));
        Talker first = new Talker(look(SKINS[3], BLACK_HAIR, Hair.LONG, new ColorRGBA(0.9f, 0.8f, 0.2f, 1f), null,
                new ColorRGBA(0.9f, 0.8f, 0.2f, 1f), true), 3.75f, -1.05f, 4.2f, -0.65f, 0f);
        Talker second = new Talker(look(SKINS[1], BROWN, Hair.SHORT, new ColorRGBA(0.2f, 0.25f, 0.45f, 1f), SUIT,
                SUIT, false), 4.2f, -0.65f, 3.75f, -1.05f, 3.1f);
        behaviors.add(first);
        behaviors.add(second);
        behaviors.add(new Waiter());

        // Getraenke der Mitspieler auf der Armauflage
        node.attachChild(glass(new ColorRGBA(0.95f, 0.45f, 0.2f, 1f), -TableLayout.HALF_WIDTH - 0.075f, TableLayout.RAIL_TOP, -0.8f, true));
        node.attachChild(glass(new ColorRGBA(0.75f, 0.55f, 0.15f, 1f), -TableLayout.HALF_WIDTH - 0.075f, TableLayout.RAIL_TOP, -1.62f, false));
        node.attachChild(glass(new ColorRGBA(0.4f, 0.8f, 0.9f, 1f), TableLayout.HALF_WIDTH + 0.075f, TableLayout.RAIL_TOP, -1.12f, true));
    }

    public Node getNode() {
        return node;
    }

    /** Wird aufgerufen, wenn ein Gast Chips setzt (Parameter: Position, Lautstaerke 0..1). */
    public void setChipSound(BiConsumer<Vector3f, Float> chipSound) {
        this.chipSound = chipSound;
    }

    public void update(float tpf) {
        time += tpf;
        for (Behavior behavior : behaviors) {
            behavior.update(tpf);
        }
    }

    // ---------------------------------------------------------------- Rundenablauf

    /** Viel Erfolg: Gaeste schauen gebannt aufs Rad. */
    public void onSpin() {
        bettingOpen = false;
        watching = true;
    }

    /** Ergebnis: Gewinner jubeln, Verlierer lassen den Kopf haengen. */
    public void onResult(int number) {
        watching = false;
        boolean anyWin = false;
        for (TableGuest guest : tableGuests) {
            boolean won = guest.bet != null && RouletteRules.wins(guest.bet, number);
            anyWin |= won;
            guest.react(guest.bet == null ? 0 : won ? 1 : -1);
        }
        for (Behavior behavior : behaviors) {
            if (behavior instanceof Spectator spectator) {
                spectator.react(anyWin);
            }
        }
    }

    /** Gewinnt der Spieler selbst, applaudiert der Zuschauer auch. */
    public void cheerForPlayer() {
        for (Behavior behavior : behaviors) {
            if (behavior instanceof Spectator spectator) {
                spectator.react(true);
            }
        }
    }

    /** Chips abraeumen, neue Wetten sind moeglich. */
    public void onRoundEnd() {
        bettingOpen = true;
        for (TableGuest guest : tableGuests) {
            guest.clearBet();
        }
    }

    // ---------------------------------------------------------------- Hilfen

    private static Look look(ColorRGBA skin, ColorRGBA hair, Hair style, ColorRGBA shirt, ColorRGBA jacket,
                             ColorRGBA legs, boolean dress) {
        return new Look(skin, hair, style, shirt, jacket, false, legs, SHOES, null, dress, false);
    }

    private VoxelPerson person(Look look, float x, float z, float yaw) {
        VoxelPerson person = new VoxelPerson(voxels, look);
        person.setPosition(x, z, yaw);
        node.attachChild(person.getNode());
        return person;
    }

    /** Kopfdrehung relativ zum Koerper, um von (x, z) mit Blickrichtung {@code yaw} auf {@code target} zu schauen. */
    private static float headYaw(float x, float z, float yaw, Vector3f target) {
        float world = FastMath.atan2(target.x - x, target.z - z);
        float relative = world - yaw;
        while (relative > FastMath.PI) {
            relative -= FastMath.TWO_PI;
        }
        while (relative < -FastMath.PI) {
            relative += FastMath.TWO_PI;
        }
        return FastMath.clamp(relative, -1.2f, 1.2f);
    }

    private static float headPitch(Vector3f eye, Vector3f target) {
        Vector3f direction = target.subtract(eye).normalizeLocal();
        return FastMath.clamp(-FastMath.asin(direction.y), -0.5f, 0.7f);
    }

    /** Glas mit Getraenk (und optional Strohhalm); steht mit dem Boden auf {@code y}. */
    private Node glass(ColorRGBA drink, float x, float y, float z, boolean straw) {
        Node glass = new Node("Glas");
        glass.setLocalTranslation(x, y, z);
        glass.attachChild(voxels.box("Glas", 0f, 0.055f, 0f, 0.06f, 0.11f, 0.06f, new ColorRGBA(0.85f, 0.92f, 0.95f, 1f), Pattern.METAL));
        glass.attachChild(voxels.box("Getraenk", 0f, 0.05f, 0f, 0.062f, 0.07f, 0.062f, drink, Pattern.FELT));
        glass.attachChild(voxels.box("Glasrand", 0f, 0.112f, 0f, 0.066f, 0.006f, 0.066f, ColorRGBA.White, Pattern.METAL));
        if (straw) {
            glass.attachChild(voxels.box("Strohhalm", 0.012f, 0.13f, 0f, 0.008f, 0.1f, 0.008f, VoxelFactory.RED, Pattern.NOISE));
        }
        return glass;
    }

    // ---------------------------------------------------------------- Verhalten

    /** Mitspieler auf einem Hocker am Roulettetisch. */
    private final class TableGuest implements Behavior {
        final VoxelPerson person;
        final float x;
        final float z;
        final float yaw;
        final ChipStackView chips;
        final Vector3f eye;
        String bet;
        float betTimer;
        float reachTime = -1f;
        int reaction;
        float reactionTime;
        float phase = random.nextFloat() * 10f;

        TableGuest(Look look, float x, float z, float yaw, ColorRGBA chipColor, Vector2f chipOffset) {
            this.x = x;
            this.z = z;
            this.yaw = yaw;
            person = person(look, x, z, yaw);
            person.sit(0.70f);
            person.resetPose();
            person.snap();
            chips = new ChipStackView(table, voxels, chipColor, chipOffset);
            node.attachChild(chips.getNode());
            eye = new Vector3f(x, 1.35f, z);
            betTimer = 1.5f + random.nextFloat() * 5f;
        }

        void react(int result) {
            reaction = result;
            reactionTime = 0f;
        }

        void clearBet() {
            bet = null;
            chips.show(Map.of());
            betTimer = 1.5f + random.nextFloat() * 6f;
        }

        @Override
        public void update(float tpf) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            pose.leftArm = -0.9f;
            pose.rightArm = -0.9f;
            float lookAround = 0.3f * FastMath.sin(time * 0.37f + phase);
            pose.headYaw = headYaw(x, z, yaw, new Vector3f(0f, 0.8f, -0.2f)) + lookAround;
            pose.headPitch = 0.3f;

            if (bettingOpen && bet == null) {
                betTimer -= tpf;
                if (betTimer <= 0f && reachTime < 0f) {
                    reachTime = 0f;
                }
            }
            if (reachTime >= 0f) {
                // nach vorn beugen und Chips aufs Feld schieben
                reachTime += tpf;
                pose.rightArm = -1.35f;
                pose.lean = 0.25f;
                pose.headPitch = 0.55f;
                if (reachTime > 0.7f && bet == null) {
                    placeBet();
                }
                if (reachTime > 1.1f) {
                    reachTime = -1f;
                }
            }
            if (watching) {
                pose.headYaw = headYaw(x, z, yaw, wheelCenter);
                pose.headPitch = headPitch(eye, wheelCenter);
                pose.lean = 0.12f + 0.03f * FastMath.sin(time * 2f + phase);
            }
            if (reaction != 0) {
                reactionTime += tpf;
                if (reaction > 0) {
                    // Jubel: Arme hoch, auf dem Hocker wippen
                    pose.leftArm = -0.3f;
                    pose.rightArm = -0.3f;
                    pose.leftArmOut = 2.6f + 0.2f * FastMath.sin(reactionTime * 12f);
                    pose.rightArmOut = 2.6f + 0.2f * FastMath.sin(reactionTime * 12f + 1f);
                    pose.bob = 0.04f * FastMath.abs(FastMath.sin(reactionTime * 8f));
                    pose.headPitch = -0.3f;
                } else {
                    pose.headPitch = 0.65f;
                    pose.lean = 0.2f;
                    pose.leftArm = -1.1f;
                    pose.rightArm = -1.1f;
                }
                if (reactionTime > 2.8f) {
                    reaction = 0;
                }
            }
            person.update(tpf);
        }

        private void placeBet() {
            List<TableLayout.Field> fields = new ArrayList<>(table.getFields());
            float choice = random.nextFloat();
            List<TableLayout.Field> candidates = new ArrayList<>();
            for (TableLayout.Field field : fields) {
                int odds = RouletteRules.odds(field.bet());
                if ((choice < 0.5f && odds == 1) || (choice >= 0.5f && choice < 0.75f && odds == 2)
                        || (choice >= 0.75f && odds == 35)) {
                    candidates.add(field);
                }
            }
            bet = candidates.get(random.nextInt(candidates.size())).bet();
            int[] amounts = {5, 10, 10, 25, 50};
            chips.show(Map.of(bet, amounts[random.nextInt(amounts.length)]));
            chipSound.accept(table.getField(bet).topCenter(), 0.6f);
        }
    }

    /** Steht neben dem Tisch, schaut zu und applaudiert bei Gewinnen. */
    private final class Spectator implements Behavior {
        final VoxelPerson person;
        final float x;
        final float z;
        final float yaw = FastMath.HALF_PI;
        final Vector3f eye;
        float clapTime = -1f;

        Spectator(Look look, float x, float z) {
            this.x = x;
            this.z = z;
            person = person(look, x, z, yaw);
            eye = new Vector3f(x, 1.68f, z);
        }

        void react(boolean applause) {
            if (applause) {
                clapTime = 0f;
            }
        }

        @Override
        public void update(float tpf) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            // Arme vor dem Bauch verschraenkt
            pose.leftArm = -0.7f;
            pose.rightArm = -0.7f;
            pose.leftArmOut = -0.45f;
            pose.rightArmOut = -0.45f;
            Vector3f target = watching ? wheelCenter : new Vector3f(0f, 0.85f, -0.1f);
            pose.headYaw = headYaw(x, z, yaw, target) + (watching ? 0f : 0.25f * FastMath.sin(time * 0.3f));
            pose.headPitch = headPitch(eye, target);
            pose.bob = 0.005f * FastMath.sin(time * 1.3f);
            if (clapTime >= 0f) {
                clapTime += tpf;
                pose.leftArm = -1.25f;
                pose.rightArm = -1.25f;
                float clap = FastMath.sin(clapTime * 18f);
                pose.leftArmOut = -0.25f + 0.2f * clap;
                pose.rightArmOut = -0.25f + 0.2f * clap;
                if (clapTime > 2.4f) {
                    clapTime = -1f;
                }
            }
            person.update(tpf);
        }
    }

    /** Barkeeper hinter der Theke, poliert Glaeser. */
    private final class Bartender implements Behavior {
        final VoxelPerson person;

        Bartender() {
            Look look = new Look(SKINS[1], BLACK_HAIR, Hair.SHORT, WHITE_SHIRT, new ColorRGBA(0.08f, 0.08f, 0.1f, 1f), true,
                    new ColorRGBA(0.08f, 0.08f, 0.1f, 1f), SHOES, new ColorRGBA(0.1f, 0.1f, 0.1f, 1f), false, true);
            person = person(look, -4.55f, -2.75f, FastMath.HALF_PI);
            Node glass = glass(new ColorRGBA(0.9f, 0.9f, 0.95f, 1f), 0f, -0.06f, 0.04f, false);
            person.getLeftHand().attachChild(glass);
        }

        @Override
        public void update(float tpf) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            pose.leftArm = -1.1f;
            pose.leftArmOut = -0.35f;
            pose.rightArm = -1.15f + 0.18f * FastMath.sin(time * 6f);
            pose.rightArmOut = -0.3f + 0.15f * FastMath.cos(time * 6f);
            pose.headPitch = 0.3f;
            pose.headYaw = 0.5f * FastMath.sin(time * 0.25f);
            person.update(tpf);
        }
    }

    /** Gast auf einem Barhocker, trinkt ab und zu. */
    private final class BarGuest implements Behavior {
        final VoxelPerson person;
        final float offset;

        BarGuest(Look look, float x, float z, float offset, ColorRGBA drink) {
            this.offset = offset;
            person = person(look, x, z, -FastMath.HALF_PI);
            person.sit(0.86f);
            Node glass = glass(drink, 0f, -0.06f, 0.04f, false);
            person.getRightHand().attachChild(glass);
            person.setSpeed(4f);
        }

        @Override
        public void update(float tpf) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            pose.leftArm = -1.0f;
            pose.rightArm = -1.0f;
            float cycle = (time + offset) % 9f;
            if (cycle > 6.5f && cycle < 8f) {
                // trinken: Glas zum Mund
                pose.rightArm = -2.3f;
                pose.rightArmOut = -0.35f;
                pose.headPitch = -0.35f;
            } else {
                // Unterhaltung mit dem Nachbarn
                pose.headYaw = 0.9f * FastMath.sin((time + offset) * 0.35f);
                pose.headPitch = 0.1f;
                if (cycle < 2f) {
                    pose.leftArm = -1.3f + 0.2f * FastMath.sin(time * 5f);
                }
            }
            person.update(tpf);
        }
    }

    /** Spieler am Kartentisch. */
    private final class CardPlayer implements Behavior {
        final VoxelPerson person;
        final float offset;

        CardPlayer(Look look, float x, float z, float yaw, float offset) {
            this.offset = offset;
            person = person(look, x, z, yaw);
            person.sit(0.50f);
        }

        @Override
        public void update(float tpf) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            float t = time + offset;
            pose.leftArm = -1.25f + 0.04f * FastMath.sin(t * 2f);
            pose.rightArm = -1.25f;
            pose.headPitch = 0.45f;
            pose.headYaw = 0.15f * FastMath.sin(t * 0.5f);
            if (t % 5f < 0.5f) {
                // Karte ausspielen
                pose.rightArm = -1.6f;
                pose.lean = 0.15f;
            }
            person.update(tpf);
        }
    }

    /** Einer von zwei Gaesten, die sich unterhalten. */
    private final class Talker implements Behavior {
        final VoxelPerson person;
        final float offset;

        Talker(Look look, float x, float z, float otherX, float otherZ, float offset) {
            this.offset = offset;
            person = person(look, x, z, FastMath.atan2(otherX - x, otherZ - z));
            person.setSpeed(5f);
        }

        @Override
        public void update(float tpf) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            float t = time + offset;
            float cycle = t % 6.2f;
            pose.headPitch = 0.05f * FastMath.sin(t * 2.3f);
            pose.headYaw = 0.12f * FastMath.sin(t * 0.7f);
            if (cycle < 3.1f) {
                // redet und gestikuliert
                pose.rightArm = -1.0f + 0.3f * FastMath.sin(t * 4f);
                pose.rightArmOut = 0.2f;
                pose.headPitch = -0.05f + 0.08f * FastMath.sin(t * 6f);
            } else {
                // hoert zu, nickt, haelt das Glas
                pose.leftArm = -0.9f;
                pose.headPitch = 0.1f + 0.08f * FastMath.abs(FastMath.sin(t * 3f));
            }
            if (cycle > 5.5f) {
                // lacht
                pose.bob = 0.015f * FastMath.abs(FastMath.sin(t * 14f));
            }
            person.update(tpf);
        }
    }

    /** Kellner mit Tablett, laeuft einen Rundweg durch den Raum und bleibt an Bar und Tisch kurz stehen. */
    private final class Waiter implements Behavior {
        /** Wegpunkte (x, z) und Wartezeit dort in Sekunden; Blickrichtung beim Warten. */
        private final float[][] path = {
                {-2.6f, 1.8f, 0f, 0f}, {-2.6f, -2.75f, 4f, -FastMath.HALF_PI}, {-2.6f, -5.45f, 0f, 0f},
                {1.2f, -5.45f, 0f, 0f}, {1.2f, -3.2f, 0f, 0f}, {1.95f, -3.2f, 0f, 0f},
                {1.95f, -0.8f, 3f, -FastMath.HALF_PI}, {1.95f, 1.8f, 0f, 0f}};
        private static final float SPEED = 0.85f;
        final VoxelPerson person;
        int target = 1;
        float waitTime;
        float yaw;
        float walkPhase;
        final Vector3f position = new Vector3f();

        Waiter() {
            Look look = new Look(SKINS[0], BROWN, Hair.SHORT, WHITE_SHIRT, new ColorRGBA(0.1f, 0.1f, 0.12f, 1f), true,
                    new ColorRGBA(0.1f, 0.1f, 0.12f, 1f), SHOES, new ColorRGBA(0.1f, 0.1f, 0.1f, 1f), false, false);
            person = person(look, path[0][0], path[0][1], 0f);
            // der Spieler kann durch den Kellner hindurchgehen (er bewegt sich ja selbst)
            person.getNode().setUserData(WalkCollision.PASS_THROUGH, true);
            person.setSpeed(10f);
            position.set(path[0][0], 0f, path[0][1]);
            // Tablett mit Glaesern, gegen die Armneigung ausgeglichen
            Node tray = new Node("Tablett");
            tray.setLocalRotation(new Quaternion().fromAngles(1.45f, 0f, 0f));
            tray.attachChild(voxels.box("Tablett", 0f, 0f, 0.1f, 0.34f, 0.015f, 0.26f, new ColorRGBA(0.75f, 0.75f, 0.78f, 1f), Pattern.METAL));
            tray.attachChild(glass(new ColorRGBA(0.95f, 0.85f, 0.4f, 1f), -0.08f, 0.008f, 0.08f, false));
            tray.attachChild(glass(new ColorRGBA(0.8f, 0.2f, 0.3f, 1f), 0.07f, 0.008f, 0.14f, true));
            person.getRightHand().attachChild(tray);
        }

        @Override
        public void update(float tpf) {
            VoxelPerson.Pose pose = person.pose;
            person.resetPose();
            pose.rightArm = -1.45f;
            float[] goal = path[target];
            Vector3f toGoal = new Vector3f(goal[0] - position.x, 0f, goal[1] - position.z);
            float distance = toGoal.length();
            boolean walking = false;
            float desiredYaw = yaw;
            if (distance > 0.02f) {
                walking = true;
                float step = Math.min(distance, SPEED * tpf);
                position.addLocal(toGoal.normalizeLocal().multLocal(step));
                desiredYaw = FastMath.atan2(toGoal.x, toGoal.z);
            } else if (waitTime < goal[2]) {
                waitTime += tpf;
                if (goal[2] > 0f) {
                    desiredYaw = goal[3];
                    pose.headPitch = 0.2f;
                }
            } else {
                waitTime = 0f;
                target = (target + 1) % path.length;
            }
            yaw = turnTowards(yaw, desiredYaw, 4f * tpf);
            if (walking) {
                walkPhase += tpf * SPEED * 6f;
                float swing = FastMath.sin(walkPhase);
                pose.leftHip = 0.45f * swing;
                pose.rightHip = -0.45f * swing;
                pose.leftKnee = 0.5f * Math.max(0f, -swing);
                pose.rightKnee = 0.5f * Math.max(0f, swing);
                pose.leftArm = -0.35f * swing;
                pose.bob = 0.02f * FastMath.abs(FastMath.cos(walkPhase));
            }
            person.setPosition(position.x, position.z, yaw);
            person.update(tpf);
        }

        private float turnTowards(float current, float desired, float maxStep) {
            float delta = desired - current;
            while (delta > FastMath.PI) {
                delta -= FastMath.TWO_PI;
            }
            while (delta < -FastMath.PI) {
                delta += FastMath.TWO_PI;
            }
            return current + FastMath.clamp(delta, -maxStep, maxStep);
        }
    }
}

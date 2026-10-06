/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import praesentation.VoxelFactory.Pattern;

import java.util.function.IntConsumer;

/**
 * Voxel-Roulette-Rad: 37 Segment-Boxen im Kreis (europaeische Reihenfolge) auf einem drehbaren Rotor,
 * dazu Kessel, Mittelkreuz, Zeiger und eine Voxel-Kugel.
 * <p>
 * Das Rad ist deutlich zum Spieler gekippt, damit man es vom Sitzplatz aus gut sieht. Der Zeiger steht
 * auf der dem Spieler abgewandten Seite; nach {@link #spinTo} liegt die Kugel im Gewinnfach unter dem Zeiger.
 */
public class RouletteWheel {
    public static final int[] ORDER = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};

    private static final float SEGMENT_ANGLE = FastMath.TWO_PI / ORDER.length;
    /** Neigung zum Spieler: steiler = man sieht mehr von der Scheibe. */
    private static final float TILT = 32f * FastMath.DEG_TO_RAD;
    /** Hoehe des Radmittelpunkts ueber der Tischplatte. */
    private static final float HEIGHT_ABOVE_TABLE = 0.36f;
    private static final float POCKET_RADIUS = 0.40f;
    private static final float TRACK_RADIUS = 0.51f;
    private static final float POINTER_ANGLE = FastMath.PI;
    private static final float BALL_TRACK_Y = 0.035f;
    private static final float BALL_POCKET_Y = 0.03f;
    /** Dauer einer Drehung in Sekunden (die Geraeusche werden passend dazu erzeugt). */
    public static final float SPIN_DURATION = 6f;
    private static final float IDLE_SPEED = 0.25f;
    private static final Quaternion FLAT = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);

    private final VoxelFactory voxels;
    private final Node node = new Node("Rad");
    private final Node rotor = new Node("Rotor");
    private final Geometry ball;
    private final Quaternion rotorRotation = new Quaternion();

    private float angle;
    /** Fach, in dem die Kugel liegt (-1 = Kugel liegt noch auf der Laufbahn). */
    private int ballPocket = -1;

    private boolean spinning;
    private float elapsed;
    private float angleFrom;
    private float angleTo;
    private float ballFrom;
    private float ballTo;
    private int resultPocket;
    private IntConsumer onFinished;

    public RouletteWheel(VoxelFactory voxels, BitmapFont font) {
        this.voxels = voxels;
        node.setLocalTranslation(TableLayout.WHEEL_POSITION.add(0f, HEIGHT_ABOVE_TABLE, 0f));
        node.setLocalRotation(new Quaternion().fromAngleAxis(TILT, Vector3f.UNIT_X));

        // Kessel (steht still)
        node.attachChild(voxels.box("Kesselboden", 0f, -0.03f, 0f, 0.80f, 0.02f, 0.80f, VoxelFactory.WOOD_DARK));
        ring(node, "Kesselrand", 40, 0.58f, 0.02f, 0.095f, 0.08f, 0.08f, VoxelFactory.WOOD, 0f);
        ring(node, "Laufbahn", 40, TRACK_RADIUS, -0.005f, 0.085f, 0.03f, 0.07f, VoxelFactory.WOOD_LIGHT, 0f);
        node.attachChild(voxels.box("Zeiger", 0f, 0.07f, -0.47f, 0.03f, 0.12f, 0.03f, VoxelFactory.GOLD));
        // Rauten ("Deflektoren") auf der Laufbahn, an denen die Kugel abprallt
        for (int i = 0; i < 8; i++) {
            float a = (i + 0.5f) * FastMath.QUARTER_PI;
            Geometry deflector = voxels.box("Deflektor", polar(TRACK_RADIUS, a, 0.014f),
                    new Vector3f(0.022f, 0.012f, 0.022f), VoxelFactory.GOLD);
            deflector.setLocalRotation(yaw(a + FastMath.QUARTER_PI));
            deflector.setLocalScale(1f, 1f, 1.8f);
            node.attachChild(deflector);
        }
        ring(node, "Messingring", 40, 0.625f, 0.065f, 0.1f, 0.012f, 0.012f, VoxelFactory.GOLD, 0f);

        // Rotor mit den 37 Segmenten
        node.attachChild(rotor);
        for (int i = 0; i < ORDER.length; i++) {
            int number = ORDER[i];
            ColorRGBA color = number == 0 ? VoxelFactory.FELT_LIGHT
                    : RouletteRules.isRed(number) ? VoxelFactory.RED : VoxelFactory.BLACK;
            Geometry segment = voxels.box("Segment " + number, polar(POCKET_RADIUS, pocketAngle(i), 0f),
                    new Vector3f(0.064f, 0.025f, 0.13f), color);
            segment.setLocalRotation(yaw(pocketAngle(i)));
            rotor.attachChild(segment);

            Geometry fret = voxels.box("Steg", polar(POCKET_RADIUS, pocketAngle(i) + SEGMENT_ANGLE / 2f, 0.008f),
                    new Vector3f(0.006f, 0.04f, 0.13f), VoxelFactory.GOLD);
            fret.setLocalRotation(yaw(pocketAngle(i) + SEGMENT_ANGLE / 2f));
            rotor.attachChild(fret);

            rotor.attachChild(createNumberLabel(font, number, pocketAngle(i)));
        }
        ring(rotor, "Innenring", 30, 0.28f, 0.02f, 0.062f, 0.05f, 0.10f, VoxelFactory.WOOD, 0f);
        ring(rotor, "Kegel", 20, 0.18f, 0.04f, 0.060f, 0.07f, 0.09f, VoxelFactory.WOOD_LIGHT, 0f);
        rotor.attachChild(voxels.box("Nabe", 0f, 0.06f, 0f, 0.12f, 0.08f, 0.12f, VoxelFactory.GOLD));
        rotor.attachChild(voxels.box("Spindel", 0f, 0.14f, 0f, 0.04f, 0.14f, 0.04f, VoxelFactory.GOLD));
        rotor.attachChild(voxels.box("Kreuz X", 0f, 0.20f, 0f, 0.28f, 0.025f, 0.025f, VoxelFactory.GOLD));
        rotor.attachChild(voxels.box("Kreuz Z", 0f, 0.20f, 0f, 0.025f, 0.025f, 0.28f, VoxelFactory.GOLD));
        for (int i = 0; i < 4; i++) {
            rotor.attachChild(voxels.box("Kreuzende", polar(0.14f, i * FastMath.HALF_PI, 0.20f),
                    new Vector3f(0.04f, 0.04f, 0.04f), VoxelFactory.GOLD));
        }

        ball = new Geometry("Kugel", new Sphere(16, 20, 0.017f));
        ball.setMaterial(voxels.material(ColorRGBA.White, Pattern.METAL));
        ball.setLocalTranslation(polar(TRACK_RADIUS, POINTER_ANGLE, BALL_TRACK_Y));
        node.attachChild(ball);
        applyRotor();
    }

    public Node getNode() {
        return node;
    }

    /** Mittelpunkt des Rads in Weltkoordinaten (Ziel der Kamera beim Drehen). */
    public Vector3f getCenter() {
        return node.getLocalTranslation().clone();
    }

    public boolean isSpinning() {
        return spinning;
    }

    /** Dreht das Rad so, dass es nach der Animation auf {@code number} stehen bleibt. */
    public void spinTo(int number, IntConsumer onFinished) {
        float ballAngle = ballPocket >= 0 ? pocketAngle(ballPocket) + angle : POINTER_ANGLE;
        if (ballPocket >= 0) {
            ball.removeFromParent();
            node.attachChild(ball);
            ballPocket = -1;
        }
        angle = positiveMod(angle);
        resultPocket = indexOf(number);
        float target = POINTER_ANGLE - pocketAngle(resultPocket);

        angleFrom = angle;
        angleTo = angle + positiveMod(target - angle) + 3 * FastMath.TWO_PI;
        ballFrom = ballAngle;
        ballTo = ballAngle - positiveMod(ballAngle - POINTER_ANGLE) - 6 * FastMath.TWO_PI;
        elapsed = 0f;
        spinning = true;
        this.onFinished = onFinished;
    }

    public void update(float tpf) {
        if (!spinning) {
            angle += IDLE_SPEED * tpf;
            applyRotor();
            return;
        }
        elapsed += tpf;
        float u = Math.min(1f, elapsed / SPIN_DURATION);
        angle = angleFrom + (angleTo - angleFrom) * easeOut(u, 3);

        float ballAngle = ballFrom + (ballTo - ballFrom) * easeOut(u, 2);
        float drop = smoothstep((u - 0.7f) / 0.3f);
        float radius = FastMath.interpolateLinear(drop, TRACK_RADIUS, POCKET_RADIUS);
        float y = FastMath.interpolateLinear(drop, BALL_TRACK_Y, BALL_POCKET_Y);
        ball.setLocalTranslation(polar(radius, ballAngle, y));
        applyRotor();

        if (u >= 1f) {
            spinning = false;
            ballPocket = resultPocket;
            ball.removeFromParent();
            ball.setLocalTranslation(polar(POCKET_RADIUS, pocketAngle(resultPocket), BALL_POCKET_Y));
            rotor.attachChild(ball);
            onFinished.accept(ORDER[resultPocket]);
        }
    }

    private void applyRotor() {
        rotor.setLocalRotation(rotorRotation.fromAngleAxis(angle, Vector3f.UNIT_Y));
    }

    private BitmapText createNumberLabel(BitmapFont font, int number, float pocketAngle) {
        BitmapText label = new BitmapText(font);
        label.setSize(0.034f);
        label.setText(String.valueOf(number));
        label.setColor(ColorRGBA.White);
        label.setBox(new Rectangle(-0.032f, 0.03f, 0.064f, 0.06f));
        label.setAlignment(BitmapFont.Align.Center);
        label.setVerticalAlignment(BitmapFont.VAlign.Center);
        label.setQueueBucket(RenderQueue.Bucket.Transparent);
        label.setLocalRotation(yaw(pocketAngle).mult(FLAT));
        label.setLocalTranslation(polar(0.43f, pocketAngle, 0.0135f));
        return label;
    }

    private void ring(Node parent, String name, int count, float radius, float y,
                      float tangential, float height, float radial, ColorRGBA color, float offset) {
        for (int i = 0; i < count; i++) {
            float a = offset + i * FastMath.TWO_PI / count;
            Geometry box = voxels.box(name, polar(radius, a, y), new Vector3f(tangential, height, radial), color);
            box.setLocalRotation(yaw(a));
            parent.attachChild(box);
        }
    }

    private static int indexOf(int number) {
        for (int i = 0; i < ORDER.length; i++) {
            if (ORDER[i] == number) {
                return i;
            }
        }
        throw new IllegalArgumentException("Keine Roulette-Zahl: " + number);
    }

    private static float pocketAngle(int index) {
        return index * SEGMENT_ANGLE;
    }

    /** Winkel 0 zeigt Richtung +Z (zum Spieler), eine Drehung um +Y um a erhoeht den Winkel um a. */
    private static Vector3f polar(float radius, float angle, float y) {
        return new Vector3f(radius * FastMath.sin(angle), y, radius * FastMath.cos(angle));
    }

    private static Quaternion yaw(float angle) {
        return new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y);
    }

    private static float positiveMod(float a) {
        float r = a % FastMath.TWO_PI;
        return r < 0 ? r + FastMath.TWO_PI : r;
    }

    private static float easeOut(float u, int power) {
        return 1f - (float) Math.pow(1f - u, power);
    }

    private static float smoothstep(float x) {
        float t = FastMath.clamp(x, 0f, 1f);
        return t * t * (3f - 2f * t);
    }
}

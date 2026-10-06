/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;

/**
 * Voxel-Croupier rechts neben dem Rad, schraeg zum Spieler gedreht, damit er gut im Blick ist.
 * Er bewegt sich im Leerlauf, wirft beim Drehen die Kugel, schaut dem Rad zu, sagt das Ergebnis in
 * einer Sprechblase an und zeigt auf die Gewinnzahl.
 */
public class Dealer {
    /** Standplatz rechts neben dem Rad (ausserhalb der Tischkante). */
    public static final Vector3f POSITION = new Vector3f(1.30f, 0f, -1.62f);
    /** Der Koerper zeigt zwischen Spieler und Rad. */
    private static final float BODY_YAW = -0.62f;

    private static final VoxelPerson.Look LOOK = new VoxelPerson.Look(
            new ColorRGBA(0.93f, 0.75f, 0.60f, 1f), new ColorRGBA(0.22f, 0.13f, 0.07f, 1f), VoxelPerson.Hair.SHORT,
            new ColorRGBA(0.95f, 0.95f, 0.92f, 1f), new ColorRGBA(0.45f, 0.06f, 0.10f, 1f), true,
            new ColorRGBA(0.10f, 0.10f, 0.12f, 1f), new ColorRGBA(0.04f, 0.04f, 0.05f, 1f),
            VoxelFactory.RED, false, true);

    private static final float ARM_REST = -0.55f;

    private enum Gesture {IDLE, THROW, WATCH, ANNOUNCE}

    private final VoxelPerson person;
    private final Node bubble = new Node("Sprechblase");
    private final BitmapText bubbleText;
    private final Geometry bubbleBackground;
    private final Geometry bubbleBorder;
    /** Kopfdrehung (relativ zum Koerper) zum Rad bzw. zum Spieler. */
    private final float wheelYaw;
    private final float playerYaw;

    private Gesture gesture = Gesture.IDLE;
    private float gestureTime;
    private float time;
    private float bubbleTime;

    public Dealer(VoxelFactory voxels, BitmapFont font, Vector3f wheelCenter, Vector3f playerEye) {
        person = new VoxelPerson(voxels, LOOK);
        person.setPosition(POSITION.x, POSITION.z, BODY_YAW);
        person.setSpeed(7f);
        wheelYaw = yawTo(wheelCenter);
        playerYaw = yawTo(playerEye);

        // Namensschild
        person.getBody().attachChild(voxels.box("Namensschild", 0.14f, 1.30f, 0.132f, 0.08f, 0.03f, 0.01f, VoxelFactory.GOLD));

        // Sprechblase ueber dem Kopf, dreht sich immer zur Kamera
        bubble.setLocalTranslation(0f, 2.12f, 0f);
        bubble.addControl(new BillboardControl());
        bubbleBorder = new Geometry("Blasenrand", new Quad(1f, 1f));
        bubbleBorder.setMaterial(voxels.glow(VoxelFactory.GOLD));
        bubbleBackground = new Geometry("Blase", new Quad(1f, 1f));
        bubbleBackground.setMaterial(voxels.glow(new ColorRGBA(0.05f, 0.05f, 0.08f, 1f)));
        bubbleText = new BitmapText(font);
        bubbleText.setSize(0.075f);
        bubbleText.setColor(ColorRGBA.White);
        bubbleText.setQueueBucket(RenderQueue.Bucket.Transparent);
        bubble.attachChild(bubbleBorder);
        bubble.attachChild(bubbleBackground);
        bubble.attachChild(bubbleText);
        bubble.setCullHint(Spatial.CullHint.Always);
        person.getNode().attachChild(bubble);

        person.resetPose();
        person.pose.leftArm = ARM_REST;
        person.pose.rightArm = ARM_REST;
        person.snap();
    }

    public Node getNode() {
        return person.getNode();
    }

    /** Zeigt {@code text} fuer {@code seconds} Sekunden in der Sprechblase. */
    public void say(String text, float seconds) {
        bubbleText.setText(text);
        float width = bubbleText.getLineWidth();
        float height = bubbleText.getHeight();
        float padX = 0.05f;
        float padY = 0.03f;
        float border = 0.012f;
        bubbleText.setLocalTranslation(-width / 2f, height / 2f, 0.004f);
        bubbleBackground.setLocalScale(width + 2 * padX, height + 2 * padY, 1f);
        bubbleBackground.setLocalTranslation(-width / 2f - padX, -height / 2f - padY, 0.002f);
        bubbleBorder.setLocalScale(width + 2 * (padX + border), height + 2 * (padY + border), 1f);
        bubbleBorder.setLocalTranslation(-width / 2f - padX - border, -height / 2f - padY - border, 0f);
        bubble.setCullHint(Spatial.CullHint.Never);
        bubbleTime = seconds;
    }

    /** Kugel einwerfen und danach dem Rad zuschauen. */
    public void throwBall() {
        startGesture(Gesture.THROW);
        say("Rien ne va plus!", 2.5f);
    }

    /** Ergebnis ansagen und zum Tisch zeigen. */
    public void announce(String text) {
        startGesture(Gesture.ANNOUNCE);
        say(text, 4f);
    }

    public void update(float tpf) {
        time += tpf;
        gestureTime += tpf;
        VoxelPerson.Pose pose = person.pose;
        person.resetPose();
        pose.leftArm = ARM_REST;
        pose.rightArm = ARM_REST;
        // im Leerlauf zwischen Spieler und Tisch hin und her schauen
        pose.headYaw = playerYaw + 0.35f * FastMath.sin(time * 0.45f);
        pose.headPitch = 0.12f;
        pose.bob = 0.006f * FastMath.sin(time * 1.6f);
        person.setSpeed(7f);

        switch (gesture) {
            case THROW -> {
                pose.headYaw = wheelYaw;
                pose.headPitch = 0.42f;
                pose.lean = 0.12f;
                person.setSpeed(12f);
                if (gestureTime < 0.45f) {
                    pose.rightArm = -1.75f;
                    pose.rightArmOut = 0.35f;
                } else if (gestureTime < 0.9f) {
                    pose.rightArm = -0.95f;
                    pose.rightArmOut = 0.55f;
                } else {
                    startGesture(Gesture.WATCH);
                }
            }
            case WATCH -> {
                pose.headYaw = wheelYaw + 0.06f * FastMath.sin(time * 3f);
                pose.headPitch = 0.42f;
                pose.lean = 0.08f;
            }
            case ANNOUNCE -> {
                pose.leftArm = -1.45f + 0.08f * FastMath.sin(time * 7f);
                pose.leftArmOut = -0.25f;
                pose.headYaw = playerYaw;
                pose.headPitch = gestureTime < 0.5f ? 0.35f : 0.05f;
                if (gestureTime > 2.5f) {
                    startGesture(Gesture.IDLE);
                }
            }
            default -> {
            }
        }
        person.update(tpf);

        if (bubbleTime > 0f) {
            bubbleTime -= tpf;
            if (bubbleTime <= 0f) {
                bubble.setCullHint(Spatial.CullHint.Always);
            }
        }
    }

    private void startGesture(Gesture next) {
        gesture = next;
        gestureTime = 0f;
    }

    /** Kopfdrehung relativ zur Koerperausrichtung, um auf {@code target} zu schauen. */
    private static float yawTo(Vector3f target) {
        float world = FastMath.atan2(target.x - POSITION.x, target.z - POSITION.z);
        return FastMath.clamp(world - BODY_YAW, -1.1f, 1.1f);
    }
}

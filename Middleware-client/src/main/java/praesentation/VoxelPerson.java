/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import praesentation.VoxelFactory.Pattern;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Voxel-Figur mit Gelenken (Kopf, Schultern, Hueften, Knie). Verhalten setzen jedes Frame
 * Zielwinkel ({@link #pose}), die Figur gleitet weich dorthin und blinzelt von selbst.
 * <p>
 * Alle Boxen eines Koerperteils werden zu einem Mesh mit Vertexfarben zusammengefasst: eine Figur braucht
 * so nur etwa zehn Draw-Calls statt ueber fuenfzig, auch viele Gaeste bleiben fluessig.
 * <p>
 * Masse in Metern, Fuesse bei y=0, Blickrichtung +Z. Winkel an Armen/Beinen: negativ = nach vorn.
 */
public class VoxelPerson {
    public enum Hair {SHORT, LONG, BUN, BALD}

    /**
     * Aussehen: Farben und Frisur. {@code jacket} und {@code tie} duerfen null sein; mit {@code vest}
     * ist die Jacke eine Weste (Aermel in Hemdfarbe).
     */
    public record Look(ColorRGBA skin, ColorRGBA hair, Hair hairStyle, ColorRGBA shirt, ColorRGBA jacket, boolean vest,
                       ColorRGBA legs, ColorRGBA shoes, ColorRGBA tie, boolean dress, boolean mustache) {
    }

    /** Zielhaltung, von Verhalten jedes Frame gesetzt. */
    public static final class Pose {
        public float headYaw;
        public float headPitch;
        public float leftArm;
        public float rightArm;
        /** seitliches Anheben (Jubeln), positiv = nach aussen */
        public float leftArmOut;
        public float rightArmOut;
        public float leftHip;
        public float rightHip;
        public float leftKnee;
        public float rightKnee;
        public float bob;
        public float lean;

        void copyFrom(Pose other) {
            headYaw = other.headYaw;
            headPitch = other.headPitch;
            leftArm = other.leftArm;
            rightArm = other.rightArm;
            leftArmOut = other.leftArmOut;
            rightArmOut = other.rightArmOut;
            leftHip = other.leftHip;
            rightHip = other.rightHip;
            leftKnee = other.leftKnee;
            rightKnee = other.rightKnee;
            bob = other.bob;
            lean = other.lean;
        }
    }

    public static final float HIP_HEIGHT = 0.87f;
    private static final float THIGH = 0.42f;
    private static final float SHIN = 0.45f;

    private final VoxelFactory voxels;
    private final Node root = new Node("Person");
    private final Node body = new Node("Koerper");
    private final Node head = new Node("Kopf");
    private final Node leftShoulder;
    private final Node rightShoulder;
    private final Node leftHip;
    private final Node rightHip;
    private final Node leftKnee;
    private final Node rightKnee;
    private final Node rightHand = new Node("rechte Hand");
    private final Node leftHand = new Node("linke Hand");
    private final Node eyes = new Node("Augen");
    private final Map<Node, VoxelFactory.MeshBuilder> builders = new LinkedHashMap<>();
    private final Random random = new Random();
    private float blinkTimer = 1f + (float) Math.random() * 4f;

    /** Zielhaltung (von aussen gesetzt) und aktuelle, geglaettete Haltung. */
    public final Pose pose = new Pose();
    private final Pose current = new Pose();
    private final Quaternion rotation = new Quaternion();
    private float seatHeight = -1f;
    private float speed = 8f;

    public VoxelPerson(VoxelFactory voxels, Look look) {
        this.voxels = voxels;
        root.attachChild(body);

        leftHip = leg(0.09f, look);
        rightHip = leg(-0.09f, look);
        leftKnee = (Node) leftHip.getChild("Knie");
        rightKnee = (Node) rightHip.getChild("Knie");

        // Rumpf
        part(body, 0f, 1.17f, 0f, 0.46f, 0.62f, 0.24f, look.shirt());
        if (look.dress()) {
            part(body, 0f, 0.74f, 0f, 0.50f, 0.34f, 0.30f, look.shirt());
        } else {
            part(body, 0f, 0.88f, 0f, 0.47f, 0.05f, 0.25f, darker(look.legs()));
        }
        if (look.jacket() != null) {
            part(body, -0.14f, 1.12f, 0.122f, 0.17f, 0.52f, 0.012f, look.jacket());
            part(body, 0.14f, 1.12f, 0.122f, 0.17f, 0.52f, 0.012f, look.jacket());
            part(body, 0f, 1.12f, -0.122f, 0.46f, 0.52f, 0.012f, look.jacket());
            part(body, -0.232f, 1.12f, 0f, 0.012f, 0.52f, 0.24f, look.jacket());
            part(body, 0.232f, 1.12f, 0f, 0.012f, 0.52f, 0.24f, look.jacket());
            // Knoepfe
            part(body, -0.07f, 1.02f, 0.13f, 0.024f, 0.024f, 0.01f, look.vest() ? VoxelFactory.GOLD : darker(look.jacket()));
            part(body, -0.07f, 1.14f, 0.13f, 0.024f, 0.024f, 0.01f, look.vest() ? VoxelFactory.GOLD : darker(look.jacket()));
            if (!look.vest()) {
                // Revers und Einstecktuch
                part(body, -0.085f, 1.36f, 0.131f, 0.05f, 0.17f, 0.01f, darker(look.jacket()));
                part(body, 0.085f, 1.36f, 0.131f, 0.05f, 0.17f, 0.01f, darker(look.jacket()));
                part(body, 0.15f, 1.33f, 0.131f, 0.05f, 0.022f, 0.01f, ColorRGBA.White);
            }
        } else {
            // Hemdknoepfe
            for (float y : new float[]{1.05f, 1.18f, 1.31f}) {
                part(body, 0f, y, 0.123f, 0.018f, 0.018f, 0.008f, darker(look.shirt()));
            }
        }
        // Kragen
        part(body, -0.055f, 1.475f, 0.115f, 0.08f, 0.035f, 0.03f, look.shirt());
        part(body, 0.055f, 1.475f, 0.115f, 0.08f, 0.035f, 0.03f, look.shirt());
        if (!look.dress()) {
            // Guertelschnalle
            part(body, 0f, 0.88f, 0.128f, 0.05f, 0.035f, 0.01f, VoxelFactory.GOLD);
        }
        if (look.tie() != null) {
            part(body, -0.04f, 1.44f, 0.126f, 0.06f, 0.05f, 0.02f, look.tie());
            part(body, 0.04f, 1.44f, 0.126f, 0.06f, 0.05f, 0.02f, look.tie());
            part(body, 0f, 1.44f, 0.132f, 0.03f, 0.03f, 0.02f, look.tie());
        }
        part(body, 0f, 1.51f, 0f, 0.12f, 0.06f, 0.12f, look.skin());

        // Kopf (Drehpunkt am Hals)
        head.setLocalTranslation(0f, 1.53f, 0f);
        body.attachChild(head);
        buildHead(look);

        // Arme (Drehpunkt an der Schulter)
        ColorRGBA sleeve = look.jacket() != null && !look.vest() ? look.jacket() : look.shirt();
        leftShoulder = arm(0.30f, sleeve, look.skin(), leftHand);
        rightShoulder = arm(-0.30f, sleeve, look.skin(), rightHand);

        for (Map.Entry<Node, VoxelFactory.MeshBuilder> entry : builders.entrySet()) {
            entry.getKey().attachChild(voxels.geometry("Person", entry.getValue(), Pattern.FELT));
        }
        builders.clear();
    }

    public Node getNode() {
        return root;
    }

    public Node getBody() {
        return body;
    }

    /** Befestigungspunkt fuer Gegenstaende (Glas, Tablett) in der rechten Hand. */
    public Node getRightHand() {
        return rightHand;
    }

    public Node getLeftHand() {
        return leftHand;
    }

    public void setPosition(float x, float z, float yaw) {
        root.setLocalTranslation(x, 0f, z);
        root.setLocalRotation(new Quaternion().fromAngleAxis(yaw, Vector3f.UNIT_Y));
    }

    public Vector3f getPosition() {
        return root.getLocalTranslation();
    }

    /** Sitzen auf einer Sitzflaeche der Hoehe {@code height} (Oberschenkel waagerecht, Unterschenkel haengen). */
    public void sit(float height) {
        seatHeight = height;
    }

    public void stand() {
        seatHeight = -1f;
    }

    /** Wie schnell die Figur ihrer Zielhaltung folgt (1/s). */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /** Setzt Arme, Kopf und Beine auf die Grundhaltung (stehend bzw. sitzend). */
    public void resetPose() {
        pose.headYaw = 0f;
        pose.headPitch = 0.05f;
        pose.leftArm = seatHeight > 0 ? -0.35f : 0f;
        pose.rightArm = seatHeight > 0 ? -0.35f : 0f;
        pose.leftArmOut = 0f;
        pose.rightArmOut = 0f;
        pose.leftHip = seatHeight > 0 ? -FastMath.HALF_PI : 0f;
        pose.rightHip = pose.leftHip;
        pose.leftKnee = seatHeight > 0 ? FastMath.HALF_PI : 0f;
        pose.rightKnee = pose.leftKnee;
        pose.bob = 0f;
        pose.lean = 0f;
    }

    public void snap() {
        current.copyFrom(pose);
        apply();
    }

    public void update(float tpf) {
        // Blinzeln: Augen kurz zusammenkneifen
        blinkTimer -= tpf;
        if (blinkTimer < 0f) {
            eyes.setLocalScale(1f, 0.12f, 1f);
            if (blinkTimer < -0.12f) {
                eyes.setLocalScale(1f);
                blinkTimer = 2f + random.nextFloat() * 4.5f;
            }
        }
        float k = 1f - FastMath.exp(-speed * tpf);
        current.headYaw += (pose.headYaw - current.headYaw) * k;
        current.headPitch += (pose.headPitch - current.headPitch) * k;
        current.leftArm += (pose.leftArm - current.leftArm) * k;
        current.rightArm += (pose.rightArm - current.rightArm) * k;
        current.leftArmOut += (pose.leftArmOut - current.leftArmOut) * k;
        current.rightArmOut += (pose.rightArmOut - current.rightArmOut) * k;
        current.leftHip += (pose.leftHip - current.leftHip) * k;
        current.rightHip += (pose.rightHip - current.rightHip) * k;
        current.leftKnee += (pose.leftKnee - current.leftKnee) * k;
        current.rightKnee += (pose.rightKnee - current.rightKnee) * k;
        current.bob += (pose.bob - current.bob) * k;
        current.lean += (pose.lean - current.lean) * k;
        apply();
    }

    private void apply() {
        float baseY = seatHeight > 0 ? seatHeight - HIP_HEIGHT + 0.02f : 0f;
        body.setLocalTranslation(0f, baseY + current.bob, 0f);
        body.setLocalRotation(rotation.fromAngles(current.lean, 0f, 0f));
        head.setLocalRotation(rotation.fromAngles(current.headPitch, current.headYaw, 0f));
        leftShoulder.setLocalRotation(rotation.fromAngles(current.leftArm, 0f, current.leftArmOut));
        rightShoulder.setLocalRotation(rotation.fromAngles(current.rightArm, 0f, -current.rightArmOut));
        leftHip.setLocalRotation(rotation.fromAngles(current.leftHip, 0f, 0f));
        rightHip.setLocalRotation(rotation.fromAngles(current.rightHip, 0f, 0f));
        leftKnee.setLocalRotation(rotation.fromAngles(current.leftKnee, 0f, 0f));
        rightKnee.setLocalRotation(rotation.fromAngles(current.rightKnee, 0f, 0f));
    }

    private Node leg(float x, Look look) {
        Node hip = new Node("Huefte");
        hip.setLocalTranslation(x, HIP_HEIGHT, 0f);
        body.attachChild(hip);
        part(hip, 0f, -THIGH / 2f, 0f, 0.16f, THIGH, 0.18f, look.dress() ? look.skin() : look.legs());
        Node knee = new Node("Knie");
        knee.setLocalTranslation(0f, -THIGH, 0f);
        hip.attachChild(knee);
        part(knee, 0f, -SHIN / 2f + 0.04f, 0f, 0.15f, SHIN - 0.08f, 0.17f, look.dress() ? look.skin() : look.legs());
        part(knee, 0f, -SHIN + 0.045f, 0.03f, 0.17f, 0.07f, 0.25f, look.shoes());
        part(knee, 0f, -SHIN + 0.005f, 0.03f, 0.18f, 0.012f, 0.26f, darker(darker(look.shoes())));
        if (look.dress()) {
            // Absatz
            part(knee, 0f, -SHIN + 0.035f, -0.08f, 0.05f, 0.07f, 0.04f, look.shoes());
        }
        return hip;
    }

    private Node arm(float x, ColorRGBA sleeve, ColorRGBA skin, Node hand) {
        Node shoulder = new Node("Schulter");
        shoulder.setLocalTranslation(x, 1.43f, 0f);
        body.attachChild(shoulder);
        part(shoulder, 0f, -0.27f, 0f, 0.13f, 0.55f, 0.14f, sleeve);
        part(shoulder, 0f, -0.535f, 0f, 0.135f, 0.03f, 0.145f, darker(sleeve));
        part(shoulder, 0f, -0.59f, 0f, 0.11f, 0.10f, 0.12f, skin);
        // Daumen zeigt zum Koerper
        part(shoulder, x > 0 ? -0.05f : 0.05f, -0.575f, 0.05f, 0.035f, 0.05f, 0.04f, skin);
        hand.setLocalTranslation(0f, -0.62f, 0.02f);
        shoulder.attachChild(hand);
        return shoulder;
    }

    private void buildHead(Look look) {
        ColorRGBA skin = look.skin();
        part(head, 0f, 0.16f, 0f, 0.30f, 0.30f, 0.30f, skin);
        // Ohren
        part(head, -0.157f, 0.17f, 0f, 0.025f, 0.07f, 0.06f, darker(skin));
        part(head, 0.157f, 0.17f, 0f, 0.025f, 0.07f, 0.06f, darker(skin));
        if (look.dress()) {
            part(head, -0.168f, 0.115f, 0.005f, 0.02f, 0.03f, 0.02f, VoxelFactory.GOLD);
            part(head, 0.168f, 0.115f, 0.005f, 0.02f, 0.03f, 0.02f, VoxelFactory.GOLD);
        }
        ColorRGBA hair = look.hair();
        switch (look.hairStyle()) {
            case SHORT -> {
                part(head, 0f, 0.33f, -0.01f, 0.32f, 0.07f, 0.32f, hair);
                part(head, 0.04f, 0.295f, 0.153f, 0.24f, 0.035f, 0.012f, hair);
                part(head, 0f, 0.20f, -0.155f, 0.32f, 0.24f, 0.02f, hair);
                part(head, -0.155f, 0.24f, -0.03f, 0.02f, 0.14f, 0.26f, hair);
                part(head, 0.155f, 0.24f, -0.03f, 0.02f, 0.14f, 0.26f, hair);
            }
            case LONG -> {
                part(head, 0f, 0.33f, -0.01f, 0.32f, 0.07f, 0.32f, hair);
                part(head, 0f, 0.10f, -0.16f, 0.34f, 0.46f, 0.04f, hair);
                part(head, -0.16f, 0.14f, -0.03f, 0.03f, 0.36f, 0.26f, hair);
                part(head, 0.16f, 0.14f, -0.03f, 0.03f, 0.36f, 0.26f, hair);
                part(head, 0f, 0.29f, 0.152f, 0.30f, 0.05f, 0.01f, hair);
            }
            case BUN -> {
                part(head, 0f, 0.33f, -0.01f, 0.32f, 0.07f, 0.32f, hair);
                part(head, 0f, 0.20f, -0.155f, 0.32f, 0.26f, 0.02f, hair);
                part(head, 0f, 0.36f, -0.16f, 0.14f, 0.12f, 0.10f, hair);
            }
            case BALD -> part(head, 0f, 0.20f, -0.155f, 0.32f, 0.10f, 0.02f, hair);
        }
        // Augen in eigenem Knoten, damit sie blinzeln koennen
        eyes.setLocalTranslation(0f, 0.19f, 0.151f);
        head.attachChild(eyes);
        for (float x : new float[]{-0.07f, 0.07f}) {
            part(eyes, x, 0f, 0f, 0.06f, 0.045f, 0.01f, ColorRGBA.White);
            part(eyes, x + (x < 0 ? 0.012f : -0.012f), 0f, 0.006f, 0.03f, 0.04f, 0.01f,
                    new ColorRGBA(0.10f, 0.12f, 0.22f, 1f));
            part(eyes, x + (x < 0 ? 0.008f : -0.016f), 0.008f, 0.012f, 0.008f, 0.008f, 0.004f, ColorRGBA.White);
            part(head, x, 0.235f, 0.152f, 0.07f, 0.02f, 0.01f, darker(hair));
        }
        part(head, 0f, 0.14f, 0.165f, 0.04f, 0.06f, 0.03f, darker(skin));
        if (look.mustache()) {
            part(head, 0f, 0.095f, 0.152f, 0.14f, 0.025f, 0.01f, hair);
        }
        ColorRGBA lips = look.dress() ? new ColorRGBA(0.75f, 0.12f, 0.2f, 1f) : new ColorRGBA(0.6f, 0.22f, 0.22f, 1f);
        part(head, 0f, 0.065f, 0.151f, 0.08f, 0.015f, 0.01f, lips);
    }

    /** Sammelt die Box fuer das Mesh des Koerperteils {@code parent}. */
    private void part(Node parent, float x, float y, float z, float sx, float sy, float sz, ColorRGBA color) {
        builders.computeIfAbsent(parent, node -> voxels.meshBuilder()).box(x, y, z, sx, sy, sz, color);
    }

    private static ColorRGBA darker(ColorRGBA color) {
        ColorRGBA darker = color.clone().multLocal(0.78f);
        darker.a = 1f;
        return darker;
    }
}

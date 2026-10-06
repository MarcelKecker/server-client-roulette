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
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import praesentation.VoxelFactory.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Zeigt gesetzte Betraege als Chip-Stapel auf den Wettfeldern (Stueckelung wie im Casino).
 * Mitspieler haben wie im echten Casino eigene Farbchips und einen Versatz auf dem Feld.
 */
public class ChipStackView {
    /** Chipwerte, groesster zuerst. */
    public static final int[] DENOMINATIONS = {500, 100, 25, 10, 5, 1};

    private static final int MAX_CHIPS_PER_STACK = 10;
    private static final float CHIP_RADIUS = 0.024f;
    private static final float CHIP_HEIGHT = 0.0065f;
    private static final Quaternion UPRIGHT = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);

    private final TableLayout table;
    private final VoxelFactory voxels;
    /** Einheitliche Chipfarbe eines Mitspielers; null = Farben nach Chipwert. */
    private final ColorRGBA ownerColor;
    private final Vector2f offset;
    private final Node node = new Node("Chip-Stapel");
    private final Mesh chipMesh = new Cylinder(2, 28, CHIP_RADIUS, CHIP_HEIGHT, true);
    private final Mesh inlayMesh = new Cylinder(2, 28, CHIP_RADIUS * 0.62f, CHIP_HEIGHT * 1.08f, true);

    public ChipStackView(TableLayout table, VoxelFactory voxels) {
        this(table, voxels, null, Vector2f.ZERO);
    }

    public ChipStackView(TableLayout table, VoxelFactory voxels, ColorRGBA ownerColor, Vector2f offset) {
        this.table = table;
        this.voxels = voxels;
        this.ownerColor = ownerColor;
        this.offset = offset.clone();
    }

    public Node getNode() {
        return node;
    }

    public static ColorRGBA color(int denomination) {
        return switch (denomination) {
            case 1 -> new ColorRGBA(0.92f, 0.92f, 0.90f, 1f);
            case 5 -> new ColorRGBA(0.80f, 0.10f, 0.12f, 1f);
            case 10 -> new ColorRGBA(0.15f, 0.35f, 0.85f, 1f);
            case 25 -> new ColorRGBA(0.10f, 0.58f, 0.25f, 1f);
            case 100 -> new ColorRGBA(0.12f, 0.12f, 0.13f, 1f);
            default -> new ColorRGBA(0.50f, 0.20f, 0.70f, 1f);
        };
    }

    public static ColorRGBA textColor(int denomination) {
        return denomination == 1 ? new ColorRGBA(0.1f, 0.1f, 0.1f, 1f) : ColorRGBA.White;
    }

    /** Zerlegt einen Betrag in Chips, groesste Werte zuerst. */
    public static List<Integer> decompose(int amount) {
        List<Integer> chips = new ArrayList<>();
        for (int denomination : DENOMINATIONS) {
            while (amount >= denomination) {
                chips.add(denomination);
                amount -= denomination;
            }
        }
        return chips;
    }

    public void show(Map<String, Integer> bets) {
        node.detachAllChildren();
        for (Map.Entry<String, Integer> entry : bets.entrySet()) {
            TableLayout.Field field = table.getField(entry.getKey());
            List<Integer> chips = decompose(entry.getValue());
            // Oben liegen die kleinen Werte, deshalb von unten mit den groessten beginnen
            int count = Math.min(chips.size(), MAX_CHIPS_PER_STACK);
            for (int i = 0; i < count; i++) {
                int denomination = chips.get(i);
                float jitterX = 0.0012f * FastMath.sin(i * 2.3f + field.topCenter().x * 40f);
                float jitterZ = 0.0012f * FastMath.cos(i * 1.7f + field.topCenter().z * 40f);
                Vector3f position = field.topCenter().add(offset.x + jitterX, CHIP_HEIGHT * (i + 0.5f), offset.y + jitterZ);
                ColorRGBA color = ownerColor != null ? ownerColor.clone() : color(denomination);
                node.attachChild(chip(chipMesh, color, position));
                node.attachChild(chip(inlayMesh, color.clone().interpolateLocal(ColorRGBA.White, 0.55f), position));
            }
        }
    }

    private Geometry chip(Mesh mesh, ColorRGBA color, Vector3f position) {
        Geometry geometry = new Geometry("Chip", mesh);
        geometry.setMaterial(voxels.material(color, Pattern.FELT));
        geometry.setLocalRotation(UPRIGHT);
        geometry.setLocalTranslation(position);
        return geometry;
    }
}

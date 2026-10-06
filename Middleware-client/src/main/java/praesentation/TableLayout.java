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
import com.jme3.scene.shape.Cylinder;
import jme3tools.optimize.GeometryBatchFactory;
import praesentation.VoxelFactory.Pattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Roulettetisch nach dem Vorbild eines europaeischen Casinotisches: gepolsterte Leder-Armauflage mit
 * Messingleiste, Filz, Wett-Layout, Chip-Koffer des Croupiers und Limit-Schild.
 * <p>
 * Layout aus Sicht des Spielers (Blick Richtung -Z): links die 0, rechts davon 12 Spalten mit je 3 Zahlen
 * (hinten 3, 6, ..., 36 / Mitte 2, 5, ..., 35 / vorne 1, 4, ..., 34) und ganz rechts die drei Kolonnen
 * ("2:1"). Davor die Dutzende, ganz vorne 1–18, Gerade, Rot (Raute), Schwarz (Raute), Ungerade, 19–36.
 */
public class TableLayout {
    /** Name des UserData-Eintrags, der an jedem anklickbaren Feld den Wett-String traegt. */
    public static final String BET_KEY = "bet";

    public static final float TABLE_TOP = 0.80f;
    /** Position des Roulette-Rads (Mittelpunkt des Sockels auf der Tischplatte). */
    public static final Vector3f WHEEL_POSITION = new Vector3f(0f, TABLE_TOP, -1.15f);
    /** Tischkante (Innenseite der Armauflage) in X-Richtung und vorne/hinten. */
    public static final float HALF_WIDTH = 0.95f;
    public static final float FRONT_Z = 0.55f;
    public static final float BACK_Z = -2.15f;
    /** Oberkante der Leder-Armauflage (dort stehen z.B. Glaeser). */
    public static final float RAIL_TOP = TABLE_TOP + 0.085f;

    private static final float CELL_W = 0.105f;
    private static final float CELL_D = 0.125f;
    private static final float GAP = 0.008f;
    private static final float BASE_H = 0.004f;
    private static final float TILE_H = 0.012f;
    private static final float TILE_TOP = TABLE_TOP + BASE_H + TILE_H;
    private static final int COLUMNS = 14;
    private static final int ROWS = 5;
    private static final float LAYOUT_LEFT = -COLUMNS * CELL_W / 2f;
    private static final float LAYOUT_NEAR_Z = 0.25f;

    private static final ColorRGBA LEATHER = new ColorRGBA(0.30f, 0.14f, 0.07f, 1f);
    private static final ColorRGBA BRASS = new ColorRGBA(0.85f, 0.66f, 0.28f, 1f);

    /** Liegend, Schrift-Oberkante zeigt vom Spieler weg (-Z). */
    private static final Quaternion FLAT = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);

    /** Ein anklickbares Wettfeld: Wett-String, Mittelpunkt der Oberseite und Groesse. */
    public record Field(String bet, Vector3f topCenter, Vector3f size) {
    }

    private final VoxelFactory voxels;
    private final BitmapFont font;
    private final Node node = new Node("Tisch");
    private final Node betFields = new Node("Wettfelder");
    private final Map<String, Field> fields = new HashMap<>();

    public TableLayout(VoxelFactory voxels, BitmapFont font) {
        this.voxels = voxels;
        this.font = font;
        node.attachChild(buildTable());
        node.attachChild(betFields);
        buildLayout();
        buildChipRack();
        buildLimitSign();
    }

    public Node getNode() {
        return node;
    }

    /** Nur die anklickbaren Felder, Ziel fuer das Raycasting. */
    public Node getBetFields() {
        return betFields;
    }

    public Field getField(String bet) {
        return fields.get(bet);
    }

    public java.util.Collection<Field> getFields() {
        return fields.values();
    }

    private Node buildTable() {
        Node table = new Node("Tischkoerper");
        float centerZ = (FRONT_Z + BACK_Z) / 2f;
        float length = FRONT_Z - BACK_Z;
        float width = 2 * HALF_WIDTH;
        table.attachChild(voxels.box("Filz", 0f, TABLE_TOP - 0.025f, centerZ, width, 0.05f, length, VoxelFactory.FELT));
        table.attachChild(voxels.box("Zarge", 0f, TABLE_TOP - 0.10f, centerZ, width + 0.30f, 0.12f, length + 0.30f,
                VoxelFactory.WOOD_DARK, Pattern.WOOD));

        // Gepolsterte Armauflage: Holzleiste, Lederpolster, Messingkante zum Filz
        rail(table, 0f, centerZ + length / 2f + 0.075f, width + 0.30f, 0.15f);
        rail(table, 0f, centerZ - length / 2f - 0.075f, width + 0.30f, 0.15f);
        rail(table, -HALF_WIDTH - 0.075f, centerZ, 0.15f, length);
        rail(table, HALF_WIDTH + 0.075f, centerZ, 0.15f, length);
        table.attachChild(voxels.box("Messing", 0f, TABLE_TOP + 0.004f, FRONT_Z - 0.006f, width, 0.008f, 0.012f, BRASS, Pattern.METAL));
        table.attachChild(voxels.box("Messing", 0f, TABLE_TOP + 0.004f, BACK_Z + 0.006f, width, 0.008f, 0.012f, BRASS, Pattern.METAL));
        table.attachChild(voxels.box("Messing", -HALF_WIDTH + 0.006f, TABLE_TOP + 0.004f, centerZ, 0.012f, 0.008f, length, BRASS, Pattern.METAL));
        table.attachChild(voxels.box("Messing", HALF_WIDTH - 0.006f, TABLE_TOP + 0.004f, centerZ, 0.012f, 0.008f, length, BRASS, Pattern.METAL));

        // Sockel statt vier Beinen: zwei breite Holzfuesse mit Messingfuss
        for (float z : new float[]{0.05f, -1.65f}) {
            table.attachChild(voxels.box("Tischfuss", 0f, (TABLE_TOP - 0.16f) / 2f, z, 1.3f, TABLE_TOP - 0.16f, 0.26f,
                    VoxelFactory.WOOD_DARK, Pattern.PLANKS));
            table.attachChild(voxels.box("Fussleiste", 0f, 0.03f, z, 1.4f, 0.06f, 0.34f, BRASS, Pattern.METAL));
        }

        // Sockel fuer das Rad mit Messingring
        table.attachChild(voxels.box("Radsockel", WHEEL_POSITION.x, TABLE_TOP + 0.13f, WHEEL_POSITION.z,
                0.40f, 0.26f, 0.40f, VoxelFactory.WOOD_DARK));
        table.attachChild(voxels.box("Radsockelring", WHEEL_POSITION.x, TABLE_TOP + 0.01f, WHEEL_POSITION.z,
                0.48f, 0.02f, 0.48f, BRASS, Pattern.METAL));

        // Heller Untergrund unter dem Layout: die Fugen zwischen den Feldern wirken wie weisse Linien
        float layoutDepth = ROWS * CELL_D;
        float layoutWidth = COLUMNS * CELL_W;
        float layoutCenterZ = LAYOUT_NEAR_Z - layoutDepth / 2f;
        table.attachChild(voxels.box("Layout-Linien", 0f, TABLE_TOP + BASE_H / 2f, layoutCenterZ,
                layoutWidth + 0.02f, BASE_H, layoutDepth + 0.02f, VoxelFactory.LAYOUT_LINES));
        // Goldene Zierlinie um das Layout
        float frame = 0.012f;
        float outerW = layoutWidth + 0.06f;
        float outerD = layoutDepth + 0.06f;
        table.attachChild(voxels.box("Zierlinie", 0f, TABLE_TOP + 0.002f, layoutCenterZ + outerD / 2f, outerW, 0.004f, frame, BRASS, Pattern.METAL));
        table.attachChild(voxels.box("Zierlinie", 0f, TABLE_TOP + 0.002f, layoutCenterZ - outerD / 2f, outerW, 0.004f, frame, BRASS, Pattern.METAL));
        table.attachChild(voxels.box("Zierlinie", -outerW / 2f, TABLE_TOP + 0.002f, layoutCenterZ, frame, 0.004f, outerD, BRASS, Pattern.METAL));
        table.attachChild(voxels.box("Zierlinie", outerW / 2f, TABLE_TOP + 0.002f, layoutCenterZ, frame, 0.004f, outerD, BRASS, Pattern.METAL));

        // Leere Ecken des Layouts (unter der 0 und unter den Kolonnen)
        for (int column : new int[]{0, COLUMNS - 1}) {
            table.attachChild(voxels.box("Leerfeld", cellX(column, 1), TABLE_TOP + BASE_H + TILE_H / 2f, cellZ(3, 2),
                    CELL_W - GAP, TILE_H, 2 * CELL_D - GAP, VoxelFactory.FELT));
        }

        // Statische Geometrie nach Material zusammenfassen -> deutlich weniger Draw-Calls
        GeometryBatchFactory.optimize(table);
        return table;
    }

    private void rail(Node table, float x, float z, float sizeX, float sizeZ) {
        table.attachChild(voxels.box("Armauflage Holz", x, TABLE_TOP + 0.01f, z, sizeX, 0.04f, sizeZ, VoxelFactory.WOOD, Pattern.WOOD));
        float inset = 0.02f;
        table.attachChild(voxels.box("Armauflage Leder", x, TABLE_TOP + 0.055f, z,
                Math.max(sizeX - inset, 0.01f), 0.06f, Math.max(sizeZ - inset, 0.01f), LEATHER, Pattern.FELT));
    }

    private void buildLayout() {
        addField("0", 0, 0, 1, 3, VoxelFactory.FELT_LIGHT, "0", 0.06f);
        for (int column = 1; column <= 12; column++) {
            for (int row = 0; row < 3; row++) {
                int number = 3 * column - row;
                ColorRGBA color = RouletteRules.isRed(number) ? VoxelFactory.RED : VoxelFactory.BLACK;
                addField(String.valueOf(number), column, row, 1, 1, color, String.valueOf(number), 0.048f);
            }
        }
        // Kolonnen: hinten 3, 6, ..., 36 -> Kolonne 3
        addField(RouletteRules.COLUMN_3, 13, 0, 1, 1, VoxelFactory.FELT, "2:1", 0.036f);
        addField(RouletteRules.COLUMN_2, 13, 1, 1, 1, VoxelFactory.FELT, "2:1", 0.036f);
        addField(RouletteRules.COLUMN_1, 13, 2, 1, 1, VoxelFactory.FELT, "2:1", 0.036f);

        addField(RouletteRules.DOZEN_1, 1, 3, 4, 1, VoxelFactory.FELT, "1. DUTZEND", 0.036f);
        addField(RouletteRules.DOZEN_2, 5, 3, 4, 1, VoxelFactory.FELT, "2. DUTZEND", 0.036f);
        addField(RouletteRules.DOZEN_3, 9, 3, 4, 1, VoxelFactory.FELT, "3. DUTZEND", 0.036f);

        addField(RouletteRules.LOW, 1, 4, 2, 1, VoxelFactory.FELT, "1–18", 0.036f);
        addField(RouletteRules.EVEN, 3, 4, 2, 1, VoxelFactory.FELT, "GERADE", 0.032f);
        addField(RouletteRules.RED, 5, 4, 2, 1, VoxelFactory.FELT, null, 0f);
        addField(RouletteRules.BLACK, 7, 4, 2, 1, VoxelFactory.FELT, null, 0f);
        addField(RouletteRules.ODD, 9, 4, 2, 1, VoxelFactory.FELT, "UNGERADE", 0.030f);
        addField(RouletteRules.HIGH, 11, 4, 2, 1, VoxelFactory.FELT, "19–36", 0.036f);

        // Rot und Schwarz wie auf echten Tischen als Rauten
        diamond(getField(RouletteRules.RED).topCenter(), VoxelFactory.RED);
        diamond(getField(RouletteRules.BLACK).topCenter(), VoxelFactory.BLACK);
    }

    private void addField(String bet, int column, int row, int columnSpan, int rowSpan, ColorRGBA color,
                          String text, float textSize) {
        float x = cellX(column, columnSpan);
        float z = cellZ(row, rowSpan);
        Vector3f size = new Vector3f(columnSpan * CELL_W - GAP, TILE_H, rowSpan * CELL_D - GAP);

        Geometry tile = voxels.box("Feld " + bet, x, TABLE_TOP + BASE_H + TILE_H / 2f, z, size.x, size.y, size.z, color);
        tile.setUserData(BET_KEY, bet);
        betFields.attachChild(tile);
        fields.put(bet, new Field(bet, new Vector3f(x, TILE_TOP, z), size));

        if (text != null) {
            node.attachChild(createLabel(text, textSize, size.x, size.z, new Vector3f(x, TILE_TOP + 0.001f, z)));
        }
    }

    /** Raute aus einer um 45 Grad gedrehten, in die Breite gezogenen Box (nicht anklickbar). */
    private void diamond(Vector3f center, ColorRGBA color) {
        Node stretch = new Node("Raute");
        stretch.setLocalTranslation(center.add(0f, 0.002f, 0f));
        stretch.setLocalScale(1.7f, 1f, 1f);
        Geometry square = voxels.box("Raute", Vector3f.ZERO, new Vector3f(0.07f, 0.004f, 0.07f), color);
        square.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Y));
        stretch.attachChild(square);
        Geometry rim = voxels.box("Rautenrand", new Vector3f(0f, -0.001f, 0f), new Vector3f(0.078f, 0.004f, 0.078f),
                VoxelFactory.GOLD);
        rim.setLocalRotation(square.getLocalRotation());
        stretch.attachChild(rim);
        node.attachChild(stretch);
    }

    /** Chip-Koffer ("Float") des Croupiers mit gestapelten Chips in allen Werten. */
    private void buildChipRack() {
        Node rack = new Node("Chipkoffer");
        rack.setLocalTranslation(0.66f, TABLE_TOP, -1.92f);
        rack.setLocalRotation(new Quaternion().fromAngleAxis(-0.35f, Vector3f.UNIT_Y));
        rack.attachChild(voxels.box("Koffer", 0f, 0.02f, 0f, 0.46f, 0.04f, 0.2f, VoxelFactory.WOOD_DARK, Pattern.WOOD));
        rack.attachChild(voxels.box("Kofferrand", 0f, 0.045f, 0.1f, 0.46f, 0.01f, 0.01f, BRASS, Pattern.METAL));
        int[] values = ChipStackView.DENOMINATIONS;
        for (int i = 0; i < values.length; i++) {
            for (int row = 0; row < 2; row++) {
                float height = 0.035f + ((i + row) % 3) * 0.012f;
                Geometry stack = new Geometry("Chipstapel", new Cylinder(2, 20, 0.022f, height, true));
                stack.setMaterial(voxels.material(ChipStackView.color(values[i]), Pattern.PLANKS));
                stack.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
                stack.setLocalTranslation(-0.19f + i * 0.076f, 0.04f + height / 2f, -0.045f + row * 0.09f);
                rack.attachChild(stack);
            }
        }
        node.attachChild(rack);
    }

    /** Kleines Limit-Schild links neben dem Rad, zum Spieler gedreht. */
    private void buildLimitSign() {
        Node sign = new Node("Limit-Schild");
        sign.setLocalTranslation(-0.74f, TABLE_TOP, -0.85f);
        sign.setLocalRotation(new Quaternion().fromAngleAxis(0.3f, Vector3f.UNIT_Y));
        sign.attachChild(voxels.box("Schildfuss", 0f, 0.006f, 0f, 0.2f, 0.012f, 0.06f, BRASS, Pattern.METAL));
        Node plate = new Node("Schildplatte");
        plate.setLocalTranslation(0f, 0.012f, 0f);
        plate.setLocalRotation(new Quaternion().fromAngleAxis(-0.35f, Vector3f.UNIT_X));
        plate.attachChild(voxels.box("Schild", 0f, 0.045f, 0f, 0.19f, 0.09f, 0.008f, VoxelFactory.BLACK));
        plate.attachChild(voxels.box("Schildrand", 0f, 0.045f, -0.001f, 0.2f, 0.1f, 0.008f, BRASS, Pattern.METAL));
        BitmapText text = new BitmapText(font);
        text.setSize(0.022f);
        text.setText("MIN 1 €\nMAX 500 €");
        text.setColor(VoxelFactory.GOLD);
        text.setBox(new Rectangle(-0.09f, 0.085f, 0.18f, 0.08f));
        text.setAlignment(BitmapFont.Align.Center);
        text.setVerticalAlignment(BitmapFont.VAlign.Center);
        text.setQueueBucket(RenderQueue.Bucket.Transparent);
        text.setLocalTranslation(0f, 0f, 0.0055f);
        plate.attachChild(text);
        sign.attachChild(plate);
        node.attachChild(sign);
    }

    private BitmapText createLabel(String text, float textSize, float width, float depth, Vector3f position) {
        BitmapText label = new BitmapText(font);
        label.setSize(textSize);
        label.setText(text);
        label.setColor(ColorRGBA.White);
        label.setBox(new Rectangle(-width / 2f, depth / 2f, width, depth));
        label.setAlignment(BitmapFont.Align.Center);
        label.setVerticalAlignment(BitmapFont.VAlign.Center);
        label.setQueueBucket(RenderQueue.Bucket.Transparent);
        label.setLocalRotation(FLAT);
        label.setLocalTranslation(position);
        return label;
    }

    private static float cellX(int column, int columnSpan) {
        return LAYOUT_LEFT + (column + columnSpan / 2f) * CELL_W;
    }

    private static float cellZ(int row, int rowSpan) {
        return LAYOUT_NEAR_Z - (ROWS - row - rowSpan / 2f) * CELL_D;
    }
}

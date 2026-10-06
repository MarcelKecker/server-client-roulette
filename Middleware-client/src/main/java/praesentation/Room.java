/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import jme3tools.optimize.GeometryBatchFactory;
import praesentation.VoxelFactory.Pattern;

import java.util.Map;

/**
 * Der Casino-Raum um den Roulette-Tisch: Boden, Waende mit Paneelen, Decke, Saeulen, Lampen,
 * Pixel-Bilder, "CASINO"-Schriftzug, Bar, Tuer, Kartentisch, Hocker und Pflanzen.
 * <p>
 * Alles ist statisch und wird nach Material zu wenigen Geometrien zusammengefasst.
 */
public class Room {
    public static final float X_HALF = 5f;
    /** Hinter dem Tisch oeffnet sich der Raum zu einer Spielhalle mit weiteren Tischen (siehe {@link Hall}). */
    public static final float Z_BACK = -14f;
    public static final float Z_FRONT = 3f;
    public static final float HEIGHT = 3.2f;

    private static final ColorRGBA WALLPAPER = new ColorRGBA(0.10f, 0.26f, 0.19f, 1f);
    private static final ColorRGBA CEILING = new ColorRGBA(0.20f, 0.12f, 0.08f, 1f);
    private static final ColorRGBA LEATHER = new ColorRGBA(0.55f, 0.08f, 0.10f, 1f);
    private static final ColorRGBA METAL = new ColorRGBA(0.55f, 0.55f, 0.58f, 1f);
    private static final ColorRGBA LAMP_SHADE = new ColorRGBA(0.06f, 0.32f, 0.16f, 1f);
    private static final ColorRGBA LIGHT_WARM = new ColorRGBA(1f, 0.88f, 0.55f, 1f);
    private static final ColorRGBA PLANT = new ColorRGBA(0.12f, 0.50f, 0.16f, 1f);
    private static final ColorRGBA PLANT_DARK = new ColorRGBA(0.06f, 0.34f, 0.10f, 1f);
    private static final ColorRGBA POT = new ColorRGBA(0.62f, 0.30f, 0.16f, 1f);

    /** Positionen der Raumlichter (Hauptlampe ueber dem Tisch zuerst, dann Bar, rechts, Halle). */
    private static final Vector3f[] LIGHTS = {
            new Vector3f(0f, 2.25f, -0.6f),
            new Vector3f(-3.8f, 2.3f, -2.75f),
            new Vector3f(3.8f, 2.3f, -2.4f),
            new Vector3f(0f, 2.7f, -6.2f),
            new Vector3f(-2.3f, 2.3f, -9.0f),
            new Vector3f(2.35f, 2.3f, -8.7f),
            new Vector3f(-2.3f, 2.3f, -12.3f),
            new Vector3f(2.35f, 2.3f, -12.3f),
    };

    private final VoxelFactory voxels;
    private final Node room = new Node("Raum");

    public Room(VoxelFactory voxels) {
        this.voxels = voxels;
        buildFloorAndCeiling();
        wall(true, Z_BACK, 1f);
        wall(true, Z_FRONT, -1f);
        wall(false, -X_HALF, 1f);
        wall(false, X_HALF, -1f);
        buildPillars();
        buildTableLamp();
        buildSign();
        buildPaintings();
        buildBar();
        buildDoor();
        buildCardTable();
        // Hocker der Mitspieler (rechts hinten steht der Croupier)
        stool(-1.45f, -1.2f);
        stool(1.45f, -0.8f);
        stool(-1.45f, -2.0f);
        plant(-4.5f, 2.4f);
        plant(4.5f, 2.4f);
        plant(-4.4f, -5.4f);
        plant(4.4f, -5.4f);
        buildDownlights();

        GeometryBatchFactory.optimize(room);
    }

    public Node getNode() {
        return room;
    }

    /** Punktlichter muessen am rootNode haengen, damit sie auch Tisch, Rad und Croupier beleuchten. */
    public void addLights(Node rootNode) {
        for (int i = 0; i < LIGHTS.length; i++) {
            boolean main = i == 0;
            boolean hall = i >= 3;
            rootNode.addLight(new PointLight(LIGHTS[i],
                    main ? new ColorRGBA(1.2f, 1.0f, 0.75f, 1f) : new ColorRGBA(0.9f, 0.7f, 0.45f, 1f),
                    main ? 6f : hall ? 4f : 4.5f));
        }
    }

    private void buildFloorAndCeiling() {
        // Casino-Teppich mit Ornament, die Farbe steckt in der Textur
        for (int x = (int) -X_HALF; x < X_HALF; x++) {
            for (int z = (int) Z_BACK; z < Z_FRONT; z++) {
                add(voxels.box("Boden", x + 0.5f, -0.05f, z + 0.5f, 1f, 0.1f, 1f, ColorRGBA.White, Pattern.ORNATE));
            }
        }
        // dunkle Teppichborte entlang der Waende
        float borderDepth = Z_FRONT - Z_BACK;
        float borderMid = (Z_FRONT + Z_BACK) / 2f;
        add(voxels.box("Borte", -X_HALF + 0.35f, 0.002f, borderMid, 0.3f, 0.004f, borderDepth, VoxelFactory.CARPET_DARK, Pattern.CARPET));
        add(voxels.box("Borte", X_HALF - 0.35f, 0.002f, borderMid, 0.3f, 0.004f, borderDepth, VoxelFactory.CARPET_DARK, Pattern.CARPET));
        float depth = Z_FRONT - Z_BACK;
        float midZ = (Z_FRONT + Z_BACK) / 2f;
        add(voxels.box("Decke", 0f, HEIGHT + 0.1f, midZ, 2 * X_HALF + 0.4f, 0.2f, depth + 0.4f, CEILING, Pattern.PLANKS));
        for (float z : new float[]{1.5f, -1.5f, -4.5f, -7.5f, -10.5f, -13.3f}) {
            add(voxels.box("Balken", 0f, HEIGHT - 0.1f, z, 2 * X_HALF, 0.2f, 0.25f, VoxelFactory.WOOD_DARK));
        }
    }

    /** Wand aus Schichten: Tapete, Holzpaneel, Stuhlleiste, Sockelleiste, Deckenleiste. */
    private void wall(boolean alongX, float position, float inward) {
        float length = alongX ? 2 * X_HALF : Z_FRONT - Z_BACK;
        float mid = alongX ? 0f : (Z_FRONT + Z_BACK) / 2f;
        layer(alongX, position, inward, mid, length + 0.4f, -0.1f, 0.2f, HEIGHT / 2f, HEIGHT, WALLPAPER, Pattern.WALLPAPER);
        layer(alongX, position, inward, mid, length, 0.03f, 0.06f, 0.5f, 1.0f, VoxelFactory.WOOD_DARK, Pattern.PLANKS);
        layer(alongX, position, inward, mid, length, 0.06f, 0.12f, 1.03f, 0.06f, VoxelFactory.WOOD, Pattern.WOOD);
        layer(alongX, position, inward, mid, length, 0.07f, 0.14f, 0.06f, 0.12f, VoxelFactory.WOOD_DARK, Pattern.WOOD);
        layer(alongX, position, inward, mid, length, 0.06f, 0.12f, HEIGHT - 0.08f, 0.16f, VoxelFactory.GOLD, Pattern.METAL);
        // senkrechte Paneelleisten in der Holzvertaefelung
        for (float along = -length / 2f + 0.45f; along < length / 2f; along += 0.9f) {
            float normal = position + inward * 0.065f;
            Vector3f center = alongX ? new Vector3f(mid + along, 0.55f, normal) : new Vector3f(normal, 0.55f, mid + along);
            Vector3f size = alongX ? new Vector3f(0.035f, 0.88f, 0.012f) : new Vector3f(0.012f, 0.88f, 0.035f);
            add(voxels.box("Paneelleiste", center, size, VoxelFactory.WOOD_LIGHT, Pattern.WOOD));
        }
    }

    /** Einbau-Deckenlichter ueber den Seitengaengen. */
    private void buildDownlights() {
        for (float z = 2f; z > Z_BACK + 0.5f; z -= 2f) {
            for (float x : new float[]{-3.9f, 3.9f}) {
                add(voxels.box("Deckenlichtrahmen", x, HEIGHT - 0.005f, z, 0.3f, 0.02f, 0.3f, VoxelFactory.GOLD));
                room.attachChild(voxels.glowBox("Deckenlicht", x, HEIGHT - 0.02f, z, 0.22f, 0.02f, 0.22f, LIGHT_WARM));
            }
        }
    }

    private void layer(boolean alongX, float position, float inward, float mid, float length,
                       float offset, float thickness, float y, float height, ColorRGBA color, Pattern pattern) {
        float normal = position + inward * offset;
        Vector3f center = alongX ? new Vector3f(mid, y, normal) : new Vector3f(normal, y, mid);
        Vector3f size = alongX ? new Vector3f(length, height, thickness) : new Vector3f(thickness, height, length);
        add(voxels.box("Wand", center, size, color, pattern));
    }

    private void buildPillars() {
        for (float x : new float[]{-4.8f, -2.4f, 2.4f, 4.8f}) {
            pillar(x, Z_BACK + 0.15f, 0f, 1f);
        }
        for (float z : new float[]{1f, -1.5f, -4f, -6.5f, -9f, -11.5f}) {
            pillar(-X_HALF + 0.15f, z, 1f, 0f);
            pillar(X_HALF - 0.15f, z, -1f, 0f);
        }
    }

    /** Saeule mit Goldsockel, Kapitell und Wandleuchte; (dx, dz) zeigt in den Raum. */
    private void pillar(float x, float z, float dx, float dz) {
        add(voxels.box("Saeule", x, HEIGHT / 2f, z, 0.3f, HEIGHT, 0.3f, VoxelFactory.WOOD, Pattern.PLANKS));
        add(voxels.box("Saeulensockel", x, 0.08f, z, 0.38f, 0.16f, 0.38f, VoxelFactory.GOLD));
        add(voxels.box("Kapitell", x, HEIGHT - 0.3f, z, 0.38f, 0.12f, 0.38f, VoxelFactory.GOLD));
        float lx = x + dx * 0.22f;
        float lz = z + dz * 0.22f;
        add(voxels.box("Wandleuchte", lx, 1.9f, lz, 0.1f, 0.12f, 0.1f, VoxelFactory.GOLD));
        room.attachChild(voxels.glowBox("Leuchtmittel", lx, 2.02f, lz, 0.08f, 0.1f, 0.08f, LIGHT_WARM));
    }

    private void buildTableLamp() {
        Vector3f lamp = LIGHTS[0];
        float shadeY = lamp.y + 0.2f;
        add(voxels.box("Lampenkabel", lamp.x, (HEIGHT + shadeY) / 2f, lamp.z, 0.03f, HEIGHT - shadeY, 0.03f, METAL, Pattern.METAL));
        add(voxels.box("Lampenschirm", lamp.x, shadeY, lamp.z, 1.3f, 0.12f, 0.42f, LAMP_SHADE, Pattern.FELT));
        add(voxels.box("Lampenrand", lamp.x, shadeY - 0.07f, lamp.z, 1.34f, 0.03f, 0.46f, VoxelFactory.GOLD));
        for (float dx : new float[]{-0.4f, 0f, 0.4f}) {
            room.attachChild(voxels.glowBox("Gluehbirne", lamp.x + dx, shadeY - 0.1f, lamp.z, 0.12f, 0.04f, 0.12f, LIGHT_WARM));
        }
    }

    /** "CASINO" als leuchtende Pixel-Buchstaben ueber dem Croupier. */
    private void buildSign() {
        String[][] glyphs = {
                {"111", "100", "100", "100", "111"},
                {"010", "101", "111", "101", "101"},
                {"111", "100", "111", "001", "111"},
                {"111", "010", "010", "010", "111"},
                {"1001", "1101", "1011", "1001", "1001"},
                {"111", "101", "101", "101", "111"},
        };
        float pixel = 0.11f;
        int width = -1;
        for (String[] glyph : glyphs) {
            width += glyph[0].length() + 1;
        }
        float left = -width * pixel / 2f;
        float top = 3.02f;
        float z = Z_BACK + 0.08f;
        add(voxels.box("Schildrahmen", 0f, top - 2.5f * pixel, Z_BACK + 0.04f,
                (width + 4) * pixel, 7 * pixel, 0.04f, VoxelFactory.WOOD_DARK));
        int column = 0;
        for (String[] glyph : glyphs) {
            for (int row = 0; row < glyph.length; row++) {
                for (int c = 0; c < glyph[row].length(); c++) {
                    if (glyph[row].charAt(c) == '1') {
                        room.attachChild(voxels.glowBox("Schild", left + (column + c + 0.5f) * pixel,
                                top - (row + 0.5f) * pixel, z, pixel * 0.9f, pixel * 0.9f, 0.04f, VoxelFactory.GOLD));
                    }
                }
            }
            column += glyph[0].length() + 1;
        }
    }

    private void buildPaintings() {
        Map<Character, ColorRGBA> landscapeColors = Map.of(
                'S', new ColorRGBA(0.45f, 0.70f, 0.95f, 1f),
                'W', ColorRGBA.White,
                'Y', new ColorRGBA(1f, 0.85f, 0.2f, 1f),
                'M', new ColorRGBA(0.45f, 0.45f, 0.50f, 1f),
                'G', new ColorRGBA(0.25f, 0.65f, 0.20f, 1f),
                'D', new ColorRGBA(0.12f, 0.42f, 0.12f, 1f));
        pixelArt(new String[]{
                "SSSSSSSSYY",
                "SSWWSSSSYY",
                "SSSSSMSSSS",
                "SSSSMMMSSS",
                "SSSMMWMMSS",
                "GGMMMMMMGG",
                "DGGGDGGGDG",
        }, landscapeColors, new Vector3f(-3.6f, 1.9f, Z_BACK + 0.06f), true);

        Map<Character, ColorRGBA> heartColors = Map.of(
                'W', new ColorRGBA(0.95f, 0.93f, 0.86f, 1f),
                'R', VoxelFactory.RED);
        pixelArt(new String[]{
                "WWWWWWWWW",
                "WRRWWWRRW",
                "RRRRWRRRR",
                "RRRRRRRRR",
                "WRRRRRRRW",
                "WWRRRRRWW",
                "WWWRRRWWW",
                "WWWWRWWWW",
        }, heartColors, new Vector3f(3.6f, 1.9f, Z_BACK + 0.06f), true);

        Map<Character, ColorRGBA> spadeColors = Map.of(
                'W', new ColorRGBA(0.95f, 0.93f, 0.86f, 1f),
                'B', VoxelFactory.BLACK);
        pixelArt(new String[]{
                "WWWWBWWWW",
                "WWWBBBWWW",
                "WWBBBBBWW",
                "WBBBBBBBW",
                "WBBBBBBBW",
                "WWBWBWBWW",
                "WWWWBWWWW",
                "WWWBBBWWW",
        }, spadeColors, new Vector3f(X_HALF - 0.06f, 1.9f, -0.25f), false);
    }

    /** Bild aus einzelnen Pixel-Boxen mit Goldrahmen; {@code alongX}: Bild haengt an einer Wand entlang X. */
    private void pixelArt(String[] rows, Map<Character, ColorRGBA> colors, Vector3f center, boolean alongX) {
        float pixel = 0.09f;
        int columns = rows[0].length();
        float width = columns * pixel;
        float height = rows.length * pixel;
        float inward = alongX ? 1f : -1f;
        Vector3f frameSize = alongX ? new Vector3f(width + 0.12f, height + 0.12f, 0.04f)
                : new Vector3f(0.04f, height + 0.12f, width + 0.12f);
        add(voxels.box("Bilderrahmen", center, frameSize, VoxelFactory.GOLD));
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < columns; column++) {
                float along = -width / 2f + (column + 0.5f) * pixel;
                float y = center.y + height / 2f - (row + 0.5f) * pixel;
                Vector3f position = alongX
                        ? new Vector3f(center.x + along, y, center.z + inward * 0.03f)
                        : new Vector3f(center.x + inward * 0.03f, y, center.z - along);
                Vector3f size = alongX ? new Vector3f(pixel, pixel, 0.02f) : new Vector3f(0.02f, pixel, pixel);
                add(voxels.box("Bildpixel", position, size, colors.get(rows[row].charAt(column)), Pattern.FELT));
            }
        }
    }

    /** Bar an der linken Wand mit Flaschenregal und Barhockern. */
    private void buildBar() {
        float x = -X_HALF + 0.9f;
        // zwischen den Saeulen bei z=-4 und z=-1.5
        float z = -2.75f;
        float length = 2.2f;
        add(voxels.box("Theke", x, 0.55f, z, 0.6f, 1.1f, length, VoxelFactory.WOOD_DARK, Pattern.PLANKS));
        add(voxels.box("Thekenplatte", x, 1.13f, z, 0.72f, 0.06f, length + 0.1f, VoxelFactory.WOOD_LIGHT));
        add(voxels.box("Fussleiste", x + 0.32f, 0.15f, z, 0.04f, 0.04f, length, VoxelFactory.GOLD));
        ColorRGBA[] glass = {
                new ColorRGBA(0.15f, 0.55f, 0.20f, 1f), new ColorRGBA(0.75f, 0.45f, 0.10f, 1f),
                new ColorRGBA(0.20f, 0.35f, 0.80f, 1f), new ColorRGBA(0.85f, 0.85f, 0.80f, 1f),
                new ColorRGBA(0.60f, 0.10f, 0.15f, 1f)};
        float shelfX = -X_HALF + 0.2f;
        for (float y : new float[]{1.45f, 1.95f}) {
            add(voxels.box("Regal", shelfX, y, z, 0.25f, 0.04f, length, VoxelFactory.WOOD));
            int i = y > 1.5f ? 2 : 0;
            for (float bz = z - length / 2f + 0.15f; bz < z + length / 2f - 0.1f; bz += 0.2f) {
                ColorRGBA color = glass[i++ % glass.length];
                float h = 0.2f + (i % 3) * 0.04f;
                add(voxels.box("Flasche", shelfX, y + 0.02f + h / 2f, bz, 0.07f, h, 0.07f, color, Pattern.METAL));
                add(voxels.box("Flaschenhals", shelfX, y + 0.02f + h + 0.04f, bz, 0.03f, 0.08f, 0.03f, color, Pattern.METAL));
            }
        }
        for (float sz = z - 1.1f; sz <= z + 1.1f; sz += 1.1f) {
            barStool(x + 0.75f, sz);
        }
    }

    private void buildDoor() {
        // vor Paneel und Leisten (die reichen bis 0.14 m in den Raum)
        float x = X_HALF - 0.2f;
        float z = -2.75f;
        add(voxels.box("Tuerrahmen", x, 1.1f, z, 0.08f, 2.3f, 1.2f, VoxelFactory.WOOD));
        add(voxels.box("Tuer", x - 0.03f, 1.05f, z, 0.06f, 2.1f, 1.0f, VoxelFactory.WOOD_DARK, Pattern.PLANKS));
        add(voxels.box("Tuergriff", x - 0.09f, 1.05f, z + 0.35f, 0.05f, 0.05f, 0.12f, VoxelFactory.GOLD));
        add(voxels.box("Schild Ausgang", x - 0.03f, 2.35f, z, 0.03f, 0.12f, 0.5f, new ColorRGBA(0.1f, 0.6f, 0.2f, 1f)));
    }

    /** Kleiner Kartentisch rechts mit Stuehlen, Karten und Chips. */
    private void buildCardTable() {
        float x = 3.2f;
        float z = -4.4f;
        add(voxels.box("Kartentisch", x, 0.74f, z, 1.4f, 0.06f, 0.9f, VoxelFactory.FELT));
        add(voxels.box("Kartentisch Rand", x, 0.72f, z, 1.52f, 0.06f, 1.02f, VoxelFactory.WOOD));
        add(voxels.box("Kartentisch Fuss", x, 0.36f, z, 0.25f, 0.7f, 0.25f, VoxelFactory.WOOD_DARK));
        add(voxels.box("Kartentisch Platte", x, 0.03f, z, 0.7f, 0.06f, 0.7f, VoxelFactory.WOOD_DARK));
        for (int i = 0; i < 4; i++) {
            add(voxels.box("Karte", x - 0.3f + i * 0.16f, 0.775f, z + 0.1f, 0.1f, 0.01f, 0.14f,
                    new ColorRGBA(0.95f, 0.95f, 0.9f, 1f)));
        }
        ColorRGBA[] chipColors = {VoxelFactory.RED, VoxelFactory.BLACK, new ColorRGBA(0.2f, 0.3f, 0.8f, 1f)};
        for (int stack = 0; stack < 3; stack++) {
            for (int chip = 0; chip <= stack + 1; chip++) {
                add(voxels.box("Chip", x + 0.35f + stack * 0.09f, 0.78f + chip * 0.018f, z - 0.2f,
                        0.07f, 0.016f, 0.07f, chipColors[stack]));
            }
        }
        for (float cx : new float[]{x - 1.0f, x + 1.0f}) {
            add(voxels.box("Stuhl Sitz", cx, 0.46f, z, 0.45f, 0.08f, 0.45f, LEATHER));
            add(voxels.box("Stuhl Unterbau", cx, 0.22f, z, 0.38f, 0.42f, 0.38f, VoxelFactory.WOOD_DARK));
            float back = cx + (cx < x ? -0.2f : 0.2f);
            add(voxels.box("Stuhl Lehne", back, 0.8f, z, 0.07f, 0.6f, 0.45f, LEATHER));
        }
    }

    private void stool(float x, float z) {
        add(voxels.box("Hocker Sitz", x, 0.66f, z, 0.36f, 0.08f, 0.36f, LEATHER));
        add(voxels.box("Hocker Rand", x, 0.61f, z, 0.38f, 0.03f, 0.38f, VoxelFactory.GOLD));
        add(voxels.box("Hocker Bein", x, 0.3f, z, 0.07f, 0.6f, 0.07f, METAL, Pattern.METAL));
        add(voxels.box("Hocker Fuss", x, 0.02f, z, 0.32f, 0.04f, 0.32f, METAL, Pattern.METAL));
    }

    private void barStool(float x, float z) {
        add(voxels.box("Barhocker Sitz", x, 0.82f, z, 0.34f, 0.08f, 0.34f, LEATHER));
        add(voxels.box("Barhocker Bein", x, 0.4f, z, 0.07f, 0.8f, 0.07f, METAL, Pattern.METAL));
        add(voxels.box("Barhocker Ring", x, 0.3f, z, 0.26f, 0.03f, 0.26f, METAL, Pattern.METAL));
        add(voxels.box("Barhocker Fuss", x, 0.02f, z, 0.3f, 0.04f, 0.3f, METAL, Pattern.METAL));
    }

    private void plant(float x, float z) {
        add(voxels.box("Topf", x, 0.25f, z, 0.45f, 0.5f, 0.45f, POT));
        add(voxels.box("Erde", x, 0.49f, z, 0.4f, 0.04f, 0.4f, VoxelFactory.WOOD_DARK));
        int[][] leaves = {{0, 0, 0}, {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1}, {0, 2, 0}, {1, 2, 1}, {-1, 2, -1},
                {0, 3, 0}, {1, 3, -1}, {-1, 3, 1}, {0, 4, 0}};
        float leaf = 0.2f;
        for (int i = 0; i < leaves.length; i++) {
            int[] l = leaves[i];
            add(voxels.box("Blatt", x + l[0] * leaf, 0.62f + l[1] * leaf, z + l[2] * leaf, leaf, leaf, leaf,
                    i % 3 == 0 ? PLANT_DARK : PLANT, Pattern.NOISE));
        }
    }

    private void add(Spatial spatial) {
        room.attachChild(spatial);
    }
}

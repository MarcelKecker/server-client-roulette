/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Erzeugt Voxel-Boxen mit grober Pixel-Textur (Nearest-Filter, wie in Voxel-Spielen).
 * <p>
 * Die Texturkoordinaten werden aus der Position berechnet, dadurch sind die Pixel auf jeder Box
 * gleich gross (unabhaengig von der Box-Groesse) und Muster laufen ueber benachbarte Boxen weiter.
 * Die Muster sind 32x32 Graustufen-Bilder, die mit der Box-Farbe multipliziert werden.
 */
public class VoxelFactory {
    public static final ColorRGBA FELT = new ColorRGBA(0.05f, 0.45f, 0.19f, 1f);
    public static final ColorRGBA FELT_LIGHT = new ColorRGBA(0.10f, 0.66f, 0.30f, 1f);
    public static final ColorRGBA LAYOUT_LINES = new ColorRGBA(0.95f, 0.93f, 0.84f, 1f);
    public static final ColorRGBA RED = new ColorRGBA(0.85f, 0.10f, 0.10f, 1f);
    public static final ColorRGBA BLACK = new ColorRGBA(0.10f, 0.10f, 0.11f, 1f);
    public static final ColorRGBA WOOD = new ColorRGBA(0.46f, 0.26f, 0.12f, 1f);
    public static final ColorRGBA WOOD_DARK = new ColorRGBA(0.26f, 0.14f, 0.07f, 1f);
    public static final ColorRGBA WOOD_LIGHT = new ColorRGBA(0.70f, 0.48f, 0.26f, 1f);
    public static final ColorRGBA GOLD = new ColorRGBA(1f, 0.80f, 0.24f, 1f);
    public static final ColorRGBA CARPET = new ColorRGBA(0.42f, 0.07f, 0.10f, 1f);
    public static final ColorRGBA CARPET_DARK = new ColorRGBA(0.30f, 0.04f, 0.08f, 1f);

    /** Texturpixel pro Meter: kleiner = groebere Pixel (vorher 36 bei 16er-Texturen). */
    private static final float TEXELS_PER_METER = 80f;
    private static final int TEXTURE_SIZE = 32;

    /** Pixelmuster der Oberflaechen. */
    public enum Pattern {
        /** leichtes Rauschen */
        NOISE,
        /** feiner Filz */
        FELT,
        /** Holzmaserung */
        WOOD,
        /** Dielen/Paneele mit Fugen */
        PLANKS,
        /** Teppich mit Rautenmuster */
        CARPET,
        /** Streifentapete */
        WALLPAPER,
        /** glaenzendes Metall */
        METAL,
        /** farbiger Casino-Teppich mit Ornamenten (Farbe steckt in der Textur, Materialfarbe weiss) */
        ORNATE
    }

    private record MaterialKey(ColorRGBA color, Pattern pattern) {
    }

    private final AssetManager assetManager;
    private final Map<MaterialKey, Material> materials = new HashMap<>();
    private final Map<ColorRGBA, Material> glowMaterials = new HashMap<>();
    private final Map<Pattern, Texture2D> textures = new EnumMap<>(Pattern.class);
    private final Map<Pattern, Material> vertexColorMaterials = new EnumMap<>(Pattern.class);

    public VoxelFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Material material(ColorRGBA color, Pattern pattern) {
        return materials.computeIfAbsent(new MaterialKey(color.clone(), pattern), key -> {
            // Hinweis: bei ORNATE steckt die Farbe in der Textur, als Farbe dann Weiss uebergeben
            Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            material.setTexture("DiffuseMap", texture(pattern));
            material.setBoolean("UseMaterialColors", true);
            material.setColor("Diffuse", key.color());
            material.setColor("Ambient", key.color());
            boolean shiny = pattern == Pattern.METAL;
            material.setColor("Specular", shiny ? ColorRGBA.White.mult(0.6f) : new ColorRGBA(0.08f, 0.08f, 0.08f, 1f));
            material.setFloat("Shininess", shiny ? 24f : 6f);
            return material;
        });
    }

    /**
     * Material, dessen Farbe aus den Vertexfarben kommt (fuer {@link MeshBuilder}): viele verschiedenfarbige
     * Boxen werden so zu einem einzigen Mesh und einem Draw-Call.
     */
    public Material vertexColorMaterial(Pattern pattern) {
        return vertexColorMaterials.computeIfAbsent(pattern, p -> {
            Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            material.setTexture("DiffuseMap", texture(p));
            material.setBoolean("UseMaterialColors", true);
            material.setBoolean("UseVertexColor", true);
            material.setColor("Diffuse", ColorRGBA.White);
            material.setColor("Ambient", ColorRGBA.White);
            material.setColor("Specular", new ColorRGBA(0.08f, 0.08f, 0.08f, 1f));
            material.setFloat("Shininess", 6f);
            return material;
        });
    }

    public MeshBuilder meshBuilder() {
        return new MeshBuilder();
    }

    /** Sammelt achsenparallele, farbige Boxen und baut daraus ein Mesh mit Vertexfarben. */
    public static final class MeshBuilder {
        private static final Box UNIT = new Box(0.5f, 0.5f, 0.5f);
        private final List<float[]> boxes = new ArrayList<>();

        /** Box mit Mittelpunkt (x, y, z) und voller Kantenlaenge (sx, sy, sz). */
        public MeshBuilder box(float x, float y, float z, float sx, float sy, float sz, ColorRGBA color) {
            boxes.add(new float[]{x, y, z, sx, sy, sz, color.r, color.g, color.b, color.a});
            return this;
        }

        public boolean isEmpty() {
            return boxes.isEmpty();
        }

        public Mesh build() {
            FloatBuffer unitPositions = UNIT.getFloatBuffer(VertexBuffer.Type.Position);
            FloatBuffer unitNormals = UNIT.getFloatBuffer(VertexBuffer.Type.Normal);
            ShortBuffer unitIndices = (ShortBuffer) UNIT.getBuffer(VertexBuffer.Type.Index).getData();
            int vertsPerBox = UNIT.getVertexCount();
            int indicesPerBox = unitIndices.limit();
            int count = boxes.size();
            FloatBuffer positions = BufferUtils.createFloatBuffer(count * vertsPerBox * 3);
            FloatBuffer normals = BufferUtils.createFloatBuffer(count * vertsPerBox * 3);
            FloatBuffer uvs = BufferUtils.createFloatBuffer(count * vertsPerBox * 2);
            FloatBuffer colors = BufferUtils.createFloatBuffer(count * vertsPerBox * 4);
            IntBuffer indices = BufferUtils.createIntBuffer(count * indicesPerBox);
            float scale = TEXELS_PER_METER / TEXTURE_SIZE;
            for (int b = 0; b < count; b++) {
                float[] box = boxes.get(b);
                for (int i = 0; i < vertsPerBox; i++) {
                    float x = unitPositions.get(i * 3) * box[3] + box[0];
                    float y = unitPositions.get(i * 3 + 1) * box[4] + box[1];
                    float z = unitPositions.get(i * 3 + 2) * box[5] + box[2];
                    float nx = unitNormals.get(i * 3);
                    float ny = unitNormals.get(i * 3 + 1);
                    float nz = unitNormals.get(i * 3 + 2);
                    positions.put(x).put(y).put(z);
                    normals.put(nx).put(ny).put(nz);
                    if (Math.abs(nx) > 0.5f) {
                        uvs.put(z * scale).put(y * scale);
                    } else if (Math.abs(ny) > 0.5f) {
                        uvs.put(x * scale).put(z * scale);
                    } else {
                        uvs.put(x * scale).put(y * scale);
                    }
                    colors.put(box[6]).put(box[7]).put(box[8]).put(box[9]);
                }
                for (int i = 0; i < indicesPerBox; i++) {
                    indices.put(unitIndices.get(i) + b * vertsPerBox);
                }
            }
            positions.flip();
            normals.flip();
            uvs.flip();
            colors.flip();
            indices.flip();
            Mesh mesh = new Mesh();
            mesh.setBuffer(VertexBuffer.Type.Position, 3, positions);
            mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, uvs);
            mesh.setBuffer(VertexBuffer.Type.Color, 4, colors);
            mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
            mesh.updateBound();
            return mesh;
        }
    }

    /** Geometrie aus einem {@link MeshBuilder} mit Vertexfarben-Material. */
    public Geometry geometry(String name, MeshBuilder builder, Pattern pattern) {
        Geometry geometry = new Geometry(name, builder.build());
        geometry.setMaterial(vertexColorMaterial(pattern));
        return geometry;
    }

    /** Selbstleuchtendes Material (Lampen, Kerzen), unabhaengig von der Beleuchtung. */
    public Material glow(ColorRGBA color) {
        return glowMaterials.computeIfAbsent(color.clone(), c -> {
            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", c);
            return material;
        });
    }

    /** Box mit Mittelpunkt {@code center} und voller Kantenlaenge {@code size}. */
    public Geometry box(String name, Vector3f center, Vector3f size, ColorRGBA color, Pattern pattern) {
        Geometry geometry = new Geometry(name, pixelBox(center, size));
        geometry.setMaterial(material(color, pattern));
        geometry.setLocalTranslation(center);
        return geometry;
    }

    public Geometry box(String name, Vector3f center, Vector3f size, ColorRGBA color) {
        return box(name, center, size, color, defaultPattern(color));
    }

    public Geometry box(String name, float x, float y, float z, float sx, float sy, float sz, ColorRGBA color) {
        return box(name, new Vector3f(x, y, z), new Vector3f(sx, sy, sz), color);
    }

    public Geometry box(String name, float x, float y, float z, float sx, float sy, float sz,
                        ColorRGBA color, Pattern pattern) {
        return box(name, new Vector3f(x, y, z), new Vector3f(sx, sy, sz), color, pattern);
    }

    public Geometry glowBox(String name, float x, float y, float z, float sx, float sy, float sz, ColorRGBA color) {
        Geometry geometry = new Geometry(name, new Box(sx / 2f, sy / 2f, sz / 2f));
        geometry.setMaterial(glow(color));
        geometry.setLocalTranslation(x, y, z);
        return geometry;
    }

    private static Pattern defaultPattern(ColorRGBA color) {
        if (Objects.equals(color, WOOD) || Objects.equals(color, WOOD_DARK) || Objects.equals(color, WOOD_LIGHT)) {
            return Pattern.WOOD;
        }
        if (Objects.equals(color, FELT) || Objects.equals(color, FELT_LIGHT)) {
            return Pattern.FELT;
        }
        if (Objects.equals(color, GOLD)) {
            return Pattern.METAL;
        }
        return Pattern.NOISE;
    }

    /**
     * Box-Mesh mit positionsabhaengigen Texturkoordinaten: Jede Seite wird auf die beiden Achsen
     * projiziert, die in ihrer Ebene liegen. {@code offset} (Mittelpunkt) laesst Muster ueber
     * benachbarte Boxen weiterlaufen.
     */
    private static Mesh pixelBox(Vector3f offset, Vector3f size) {
        Box box = new Box(size.x / 2f, size.y / 2f, size.z / 2f);
        FloatBuffer positions = box.getFloatBuffer(VertexBuffer.Type.Position);
        FloatBuffer normals = box.getFloatBuffer(VertexBuffer.Type.Normal);
        int vertices = box.getVertexCount();
        FloatBuffer uvs = BufferUtils.createFloatBuffer(vertices * 2);
        float scale = TEXELS_PER_METER / TEXTURE_SIZE;
        for (int i = 0; i < vertices; i++) {
            float x = positions.get(i * 3) + offset.x;
            float y = positions.get(i * 3 + 1) + offset.y;
            float z = positions.get(i * 3 + 2) + offset.z;
            float nx = Math.abs(normals.get(i * 3));
            float ny = Math.abs(normals.get(i * 3 + 1));
            float u;
            float v;
            if (nx > 0.5f) {
                u = z;
                v = y;
            } else if (ny > 0.5f) {
                u = x;
                v = z;
            } else {
                u = x;
                v = y;
            }
            uvs.put(u * scale).put(v * scale);
        }
        uvs.flip();
        box.setBuffer(VertexBuffer.Type.TexCoord, 2, uvs);
        return box;
    }

    private Texture2D texture(Pattern pattern) {
        return textures.computeIfAbsent(pattern, p -> {
            int n = TEXTURE_SIZE;
            ByteBuffer data = BufferUtils.createByteBuffer(n * n * 4);
            Random random = new Random(p.ordinal() * 7919L + 17);
            int plank = n / 4;
            int diamond = n / 2;
            float[] rowTone = new float[n];
            float[] columnTone = new float[n];
            for (int i = 0; i < n; i++) {
                rowTone[i] = random.nextFloat();
                columnTone[i] = random.nextFloat();
            }
            for (int y = 0; y < n; y++) {
                for (int x = 0; x < n; x++) {
                    float noise = random.nextFloat();
                    float value = switch (p) {
                        case NOISE -> 0.86f + 0.14f * noise;
                        case FELT -> 0.90f + 0.10f * noise;
                        case WOOD -> 0.78f + 0.14f * rowTone[y] + 0.08f * noise - (rowTone[y] > 0.9f && x % 5 == 0 ? 0.15f : 0f);
                        case PLANKS -> x % plank == 0 ? 0.55f
                                : (y == (int) (columnTone[x / plank * plank] * n) ? 0.62f
                                : 0.80f + 0.12f * columnTone[x / plank * plank] + 0.08f * noise);
                        case CARPET -> ((x + y) % diamond == 0 || (x - y + n) % diamond == 0) ? 1f
                                : ((x + diamond / 2) % diamond == 0 && (y + diamond / 2) % diamond == 0) ? 0.95f
                                : 0.74f + 0.08f * noise;
                        case WALLPAPER -> (x % 8 < 2 ? 1f : 0.80f) + ((x % 8 == 5 && y % 4 == 1) ? 0.15f : 0f) - 0.06f * noise;
                        case METAL -> 0.82f + 0.18f * ((x + 2 * y) % 7 == 0 ? 1f : noise * 0.6f);
                        case ORNATE -> 1f;
                    };
                    if (p == Pattern.ORNATE) {
                        float[] rgb = ornate(x, y, n, noise);
                        data.put(toByte(rgb[0])).put(toByte(rgb[1])).put(toByte(rgb[2])).put((byte) 255);
                    } else {
                        byte b = toByte(value);
                        data.put(b).put(b).put(b).put((byte) 255);
                    }
                }
            }
            data.flip();
            Texture2D texture = new Texture2D(new Image(Image.Format.RGBA8, n, n, data, ColorSpace.Linear));
            texture.setWrap(Texture.WrapMode.Repeat);
            texture.setMagFilter(Texture.MagFilter.Nearest);
            texture.setMinFilter(Texture.MinFilter.NearestLinearMipMap);
            texture.setAnisotropicFilter(8);
            return texture;
        });
    }

    private static byte toByte(float value) {
        return (byte) (Math.min(1f, Math.max(0f, value)) * 255);
    }

    /**
     * Casino-Teppich: tiefrot mit goldenem Rautengitter, kleinen Rosetten in Tuerkis/Gold in den Rauten
     * und dunklen Punkten an den Kreuzungen.
     */
    private static float[] ornate(int x, int y, int n, float noise) {
        int half = n / 2;
        int dx = Math.abs((x % half) - half / 2);
        int dy = Math.abs((y % half) - half / 2);
        int diamond = dx + dy;
        float shade = 0.92f + 0.08f * noise;
        if (diamond == half / 2) {
            return new float[]{0.78f * shade, 0.56f * shade, 0.20f * shade};
        }
        if (diamond == 0) {
            return new float[]{0.95f * shade, 0.78f * shade, 0.32f * shade};
        }
        if (diamond == 2 && (dx == 0 || dy == 0)) {
            return new float[]{0.10f * shade, 0.45f * shade, 0.42f * shade};
        }
        if (diamond > half / 2) {
            return new float[]{0.22f * shade, 0.03f * shade, 0.06f * shade};
        }
        return new float[]{0.46f * shade, 0.06f * shade, 0.10f * shade};
    }
}

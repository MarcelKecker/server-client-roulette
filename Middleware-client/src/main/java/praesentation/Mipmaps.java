/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Mipmaps fuer RGBA8-Bilder mit Transparenz (Schrift, HUD-Grafiken).
 * <p>
 * Der jME-{@code MipMapGenerator} mittelt die Farbe ohne Alpha-Gewichtung und verdunkelt dadurch
 * halbtransparente Kanten mit jeder Stufe; verkleinerte Schrift wird grau und flau. Hier wird die
 * Farbe nach Deckkraft gewichtet gemittelt, die Deckkraft selbst normal.
 */
final class Mipmaps {
    private Mipmaps() {
    }

    static void generate(Image image) {
        if (image.getFormat() != Image.Format.RGBA8) {
            throw new IllegalArgumentException("Nur RGBA8 wird unterstuetzt, nicht " + image.getFormat());
        }
        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer source = image.getData(0);
        source.rewind();
        byte[] level = new byte[width * height * 4];
        source.get(level);
        source.rewind();

        List<byte[]> levels = new ArrayList<>();
        levels.add(level);
        while (width > 1 || height > 1) {
            int nextWidth = Math.max(1, width / 2);
            int nextHeight = Math.max(1, height / 2);
            byte[] next = new byte[nextWidth * nextHeight * 4];
            for (int y = 0; y < nextHeight; y++) {
                for (int x = 0; x < nextWidth; x++) {
                    long r = 0;
                    long g = 0;
                    long b = 0;
                    long alpha = 0;
                    long plainR = 0;
                    long plainG = 0;
                    long plainB = 0;
                    for (int dy = 0; dy < 2; dy++) {
                        for (int dx = 0; dx < 2; dx++) {
                            int sx = Math.min(width - 1, x * 2 + dx);
                            int sy = Math.min(height - 1, y * 2 + dy);
                            int i = (sy * width + sx) * 4;
                            int a = level[i + 3] & 255;
                            r += (long) (level[i] & 255) * a;
                            g += (long) (level[i + 1] & 255) * a;
                            b += (long) (level[i + 2] & 255) * a;
                            plainR += level[i] & 255;
                            plainG += level[i + 1] & 255;
                            plainB += level[i + 2] & 255;
                            alpha += a;
                        }
                    }
                    int o = (y * nextWidth + x) * 4;
                    next[o] = (byte) (alpha > 0 ? r / alpha : plainR / 4);
                    next[o + 1] = (byte) (alpha > 0 ? g / alpha : plainG / 4);
                    next[o + 2] = (byte) (alpha > 0 ? b / alpha : plainB / 4);
                    next[o + 3] = (byte) (alpha / 4);
                }
            }
            levels.add(next);
            level = next;
            width = nextWidth;
            height = nextHeight;
        }

        int[] sizes = new int[levels.size()];
        int total = 0;
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = levels.get(i).length;
            total += sizes[i];
        }
        ByteBuffer all = BufferUtils.createByteBuffer(total);
        for (byte[] data : levels) {
            all.put(data);
        }
        all.flip();
        image.setData(0, all);
        image.setMipMapSizes(sizes);
    }
}

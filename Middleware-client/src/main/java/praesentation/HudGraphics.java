/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Mit Java2D gezeichnete, glatte HUD-Grafiken: Panels mit runden Ecken und Casino-Chips.
 */
public final class HudGraphics {
    /** Kantenlaenge der Panel-Textur; die Ecken sind {@link #PANEL_CORNER} Pixel gross (Neun-Teilung). */
    public static final int PANEL_SIZE = 64;
    public static final int PANEL_CORNER = 18;

    private HudGraphics() {
    }

    /** Abgerundetes Rechteck mit leichtem Verlauf und Rand, fuer TbtQuadBackgroundComponent. */
    public static Texture2D panel(ColorRGBA fill, ColorRGBA border, float borderWidth) {
        int n = PANEL_SIZE;
        BufferedImage image = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        clear(g, n, fill);
        int arc = 28;
        Color top = awt(fill.clone().interpolateLocal(ColorRGBA.White, 0.08f));
        g.setPaint(new GradientPaint(0, 0, top, 0, n, awt(fill)));
        g.fillRoundRect(1, 1, n - 2, n - 2, arc, arc);
        if (borderWidth > 0) {
            g.setStroke(new BasicStroke(borderWidth));
            g.setColor(awt(border));
            float inset = 1 + borderWidth / 2f;
            g.draw(new RoundRectangle2D.Float(inset, inset, n - 2 * inset, n - 2 * inset, arc - 2, arc - 2));
        }
        g.dispose();
        return texture(image);
    }

    /**
     * Casino-Chip: farbiger Rand mit weissen Markierungen, heller Innenkreis mit dem Wert.
     *
     * @param selected ausgewaehlter Chip bekommt einen goldenen Leuchtring
     */
    public static Texture2D chip(ColorRGBA color, ColorRGBA textColor, String label, boolean selected) {
        int n = 128;
        BufferedImage image = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        clear(g, n, color);
        float c = n / 2f;
        if (selected) {
            for (int i = 6; i > 0; i--) {
                g.setColor(new Color(1f, 0.82f, 0.25f, 0.10f * (7 - i)));
                fillCircle(g, c, c, 62 - i + 6);
            }
        }
        float r = selected ? 56 : 54;
        g.setColor(new Color(0, 0, 0, 90));
        fillCircle(g, c + 1.5f, c + 3, r);
        g.setColor(awt(color));
        fillCircle(g, c, c, r);

        // Kantenmarkierungen
        g.setColor(new Color(0.97f, 0.97f, 0.95f));
        AffineTransform original = g.getTransform();
        for (int i = 0; i < 8; i++) {
            g.setTransform(original);
            g.rotate(Math.toRadians(i * 45 + 22.5), c, c);
            g.fillRoundRect((int) (c - 7), (int) (c - r), 14, 16, 4, 4);
        }
        g.setTransform(original);

        g.setColor(awt(color.clone().interpolateLocal(ColorRGBA.Black, 0.25f)));
        g.setStroke(new BasicStroke(3f));
        drawCircle(g, c, c, r * 0.66f);
        g.setColor(awt(color.clone().interpolateLocal(ColorRGBA.White, 0.18f)));
        fillCircle(g, c, c, r * 0.62f);

        g.setColor(awt(textColor));
        g.setFont(new Font("Segoe UI", Font.BOLD, label.length() > 2 ? 30 : 38));
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(label, c - metrics.stringWidth(label) / 2f, c + (metrics.getAscent() - metrics.getDescent()) / 2f);

        if (selected) {
            g.setColor(new Color(1f, 0.85f, 0.3f));
            g.setStroke(new BasicStroke(4f));
            drawCircle(g, c, c, r + 1);
        }
        g.dispose();
        return texture(image);
    }

    /** Weicher, runder Lichtpunkt fuer Partikel (weiss, nach aussen transparent). */
    public static Texture2D softDot() {
        int n = 32;
        BufferedImage image = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                float dx = (x + 0.5f - n / 2f) / (n / 2f);
                float dy = (y + 0.5f - n / 2f) / (n / 2f);
                float alpha = clamp(1f - (float) Math.sqrt(dx * dx + dy * dy));
                image.setRGB(x, y, new Color(1f, 1f, 1f, alpha * alpha).getRGB());
            }
        }
        return texture(image);
    }

    /** Hintergrund transparent, aber in der Randfarbe, damit Mipmaps keine dunklen Kanten erzeugen. */
    private static void clear(Graphics2D g, int n, ColorRGBA color) {
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(clamp(color.r), clamp(color.g), clamp(color.b), 0f));
        g.fillRect(0, 0, n, n);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private static void fillCircle(Graphics2D g, float cx, float cy, float r) {
        g.fill(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
    }

    private static void drawCircle(Graphics2D g, float cx, float cy, float r) {
        g.draw(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g;
    }

    private static Color awt(ColorRGBA c) {
        return new Color(clamp(c.r), clamp(c.g), clamp(c.b), clamp(c.a));
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static Texture2D texture(BufferedImage bufferedImage) {
        Image image = new AWTLoader().load(bufferedImage, true);
        Mipmaps.generate(image);
        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.Trilinear);
        return texture;
    }
}

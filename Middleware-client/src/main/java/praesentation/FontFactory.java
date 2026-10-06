/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * Erzeugt zur Laufzeit scharfe jME-{@link BitmapFont}s aus Systemschriften (z.B. Segoe UI).
 * <p>
 * Die mitgelieferte jME-Schrift ist nur 17 px gross und wird beim Vergroessern unscharf. Hier werden
 * die Zeichen mit Java2D in hoher Aufloesung in eine Textur gezeichnet (inkl. Umlaute und Euro-Zeichen).
 */
public final class FontFactory {
    private static final String EXTRA_CHARS = "ÄÖÜäöüß€·–—…°×●";
    private static final int ATLAS_WIDTH = 1024;

    private FontFactory() {
    }

    /**
     * @param families bevorzugte Schriftfamilien, die erste installierte wird genommen
     * @param style    {@link Font#PLAIN}, {@link Font#BOLD}, ...
     * @param size     Rendergroesse in Pixeln (Texte bis zu dieser Groesse bleiben scharf)
     */
    public static BitmapFont create(AssetManager assetManager, String[] families, int style, int size) {
        Font font = new Font(pickFamily(families), style, size);
        StringBuilder chars = new StringBuilder();
        for (char c = 32; c < 127; c++) {
            chars.append(c);
        }
        chars.append(EXTRA_CHARS);

        FontMetrics metrics = graphics(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), font).getFontMetrics();
        int pad = Math.max(2, size / 12);
        int lineHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int cellHeight = lineHeight + 2 * pad;

        // Zeichen zeilenweise im Atlas anordnen
        int x = 0;
        int y = 0;
        int[][] positions = new int[chars.length()][2];
        for (int i = 0; i < chars.length(); i++) {
            int cellWidth = metrics.charWidth(chars.charAt(i)) + 2 * pad;
            if (x + cellWidth > ATLAS_WIDTH) {
                x = 0;
                y += cellHeight;
            }
            positions[i][0] = x;
            positions[i][1] = y;
            x += cellWidth;
        }
        int atlasHeight = Integer.highestOneBit(y + cellHeight - 1) << 1;

        BufferedImage atlas = new BufferedImage(ATLAS_WIDTH, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(atlas, font);
        // Transparente Pixel weiss statt schwarz: sonst mitteln die Mipmaps dunkle Raender in die Glyphen
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(1f, 1f, 1f, 0f));
        g.fillRect(0, 0, ATLAS_WIDTH, atlasHeight);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(Color.WHITE);
        BitmapCharacterSet charSet = new BitmapCharacterSet();
        charSet.setRenderedSize(size);
        charSet.setLineHeight(lineHeight);
        charSet.setBase(ascent);
        charSet.setWidth(ATLAS_WIDTH);
        charSet.setHeight(atlasHeight);
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            int advance = metrics.charWidth(c);
            g.drawString(String.valueOf(c), positions[i][0] + pad, positions[i][1] + pad + ascent);

            BitmapCharacter character = new BitmapCharacter();
            character.setChar(c);
            character.setX(positions[i][0]);
            character.setY(positions[i][1]);
            character.setWidth(advance + 2 * pad);
            character.setHeight(cellHeight);
            character.setXOffset(-pad);
            character.setYOffset(-pad);
            character.setXAdvance(advance);
            character.setPage(0);
            charSet.addCharacter(c, character);
        }
        g.dispose();

        // wie der jME-BitmapFontLoader: Textur mit flipY laden, Vertex-Farben, Alpha-Blending
        Image image = new AWTLoader().load(atlas, true);
        Mipmaps.generate(image);
        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.Trilinear);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", texture);
        material.setBoolean("VertexColor", true);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        BitmapFont bitmapFont = new BitmapFont();
        bitmapFont.setCharSet(charSet);
        bitmapFont.setPages(new Material[]{material});
        return bitmapFont;
    }

    private static String pickFamily(String[] families) {
        Set<String> installed = Set.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String family : families) {
            if (installed.contains(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }

    private static Graphics2D graphics(BufferedImage image, Font font) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setFont(font);
        return g;
    }
}

/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import interfaces.IPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.function.ToIntFunction;

/**
 * 2D-Overlay mit Lemur im Casino-Stil.
 * <ul>
 *     <li>oben links: Spieler, Saldo, Einsatz auf dem Tisch, Verbindung; darunter die letzten Gewinnzahlen</li>
 *     <li>oben Mitte bleibt frei fuer den Croupier und seine Sprechblase</li>
 *     <li>oben rechts: Verlauf der letzten Runden</li>
 *     <li>unten: Chip-Leiste, Zurueck/Loeschen/Wiederholen und DREHEN</li>
 *     <li>Tooltip am Mauszeiger, grosse Einblendungen fuer Ergebnisse und Hinweise</li>
 * </ul>
 * Das HUD ist fuer 720 px Hoehe ausgelegt und wird bei hoeheren Aufloesungen mitskaliert; die
 * Schriften sind hochaufloesend, deshalb bleibt es dabei scharf.
 */
public class HudState extends BaseAppState implements ActionListener {
    /** Aktionen, die das HUD (Buttons und Tastatur) ausloest. */
    public interface Actions {
        void spin();

        void undo();

        void clear();

        void repeat();
    }

    public static final int[] CHIP_VALUES = {1, 5, 10, 25, 100, 500};
    public static final ColorRGBA GOOD = new ColorRGBA(0.42f, 0.92f, 0.52f, 1f);
    public static final ColorRGBA BAD = new ColorRGBA(1f, 0.45f, 0.40f, 1f);
    public static final ColorRGBA GOLD = new ColorRGBA(1f, 0.82f, 0.32f, 1f);
    public static final ColorRGBA TEXT = new ColorRGBA(0.95f, 0.95f, 0.93f, 1f);

    private static final float REFERENCE_HEIGHT = 720f;
    private static final float MARGIN = 14f;
    private static final int RESULT_COUNT = 12;
    private static final int ROUND_COUNT = 6;

    private static final ColorRGBA PANEL = new ColorRGBA(0.035f, 0.05f, 0.06f, 0.86f);
    private static final ColorRGBA PANEL_BORDER = new ColorRGBA(0.85f, 0.68f, 0.28f, 0.55f);
    private static final ColorRGBA MUTED = new ColorRGBA(0.66f, 0.70f, 0.72f, 1f);
    private static final ColorRGBA DARK = new ColorRGBA(0.02f, 0.03f, 0.04f, 0.92f);
    private static final ColorRGBA NUMBER_RED = new ColorRGBA(0.78f, 0.10f, 0.12f, 1f);
    private static final ColorRGBA NUMBER_BLACK = new ColorRGBA(0.12f, 0.12f, 0.14f, 1f);
    private static final ColorRGBA NUMBER_GREEN = new ColorRGBA(0.08f, 0.55f, 0.25f, 1f);

    private static final String[] KEY_MAPPINGS = {"Hud_Chip1", "Hud_Chip2", "Hud_Chip3", "Hud_Chip4", "Hud_Chip5",
            "Hud_Chip6", "Hud_Spin", "Hud_Undo", "Hud_Clear", "Hud_Repeat"};

    private final IPlayer player;
    private final Actions actions;
    private final ToIntFunction<String> amountOnField;
    private final BitmapFont smallFont;
    private final BitmapFont font;
    private final BitmapFont boldFont;

    private final Node hud = new Node("HUD");
    private InputManager inputManager;
    private Texture2D panelTexture;
    private Texture2D whiteTexture;

    private Container infoPanel;
    private Label saldoLabel;
    private Label onTableLabel;
    private Label connectionLabel;
    private Label tableMessageLabel;
    private Container resultsPanel;
    private Container resultsRow;
    private Container roundsPanel;
    private Label[] roundLabels;
    private Container tray;
    private Button[] chipButtons;
    private Texture2D[] chipTextures;
    private Texture2D[] chipTexturesSelected;
    private Label stakeLabel;
    private Button undoButton;
    private Button clearButton;
    private Button repeatButton;
    private Button spinButton;
    private Label hintLabel;
    private Label toast;
    private Label tooltip;
    private Node crosshair;

    private final Deque<Integer> results = new ArrayDeque<>();
    private final Deque<Label> roundEntries = new ArrayDeque<>();
    private int selectedChip = 2;
    private boolean spinning;
    private boolean hasBets;
    private boolean canRepeat;
    private boolean dragging;
    private int pixelSize = 1;
    private String hoveredBet;
    private float toastTime;
    private float toastDuration;
    private float scale = 1f;
    private boolean walking;
    /** Schwarze Ueberblendung (z.B. beim Zuruecksetzen an den Platz). */
    private Geometry fadeOverlay;
    private Material fadeMaterial;
    private float fadeTime;

    /**
     * Baut alle Elemente sofort auf, damit Statusmeldungen (z.B. aus dem Netzwerk-Thread) auch vor der
     * ersten Frame-Aktualisierung ankommen. {@link GuiGlobals#initialize} muss vorher gelaufen sein.
     */
    public HudState(AssetManager assetManager, IPlayer player, Actions actions, ToIntFunction<String> amountOnField,
                    BitmapFont smallFont, BitmapFont font, BitmapFont boldFont) {
        this.player = player;
        this.actions = actions;
        this.amountOnField = amountOnField;
        this.smallFont = smallFont;
        this.font = font;
        this.boldFont = boldFont;
        build(assetManager);
    }

    @Override
    protected void initialize(Application app) {
        inputManager = app.getInputManager();
    }

    private void build(AssetManager assetManager) {
        GuiGlobals.getInstance().getStyles().setDefault(font);
        panelTexture = HudGraphics.panel(PANEL, PANEL_BORDER, 2f);
        whiteTexture = HudGraphics.panel(ColorRGBA.White, ColorRGBA.White, 0f);

        buildInfoPanel();
        buildResultsPanel();
        buildRoundsPanel();
        buildTray();

        hintLabel = label("", 13f, MUTED);
        toast = label("", 30f, TEXT);
        toast.setFont(boldFont);
        toast.setFontSize(30f);
        toast.setBackground(rounded(DARK, 26f, 14f));
        toast.setTextHAlignment(HAlignment.Center);
        toast.setCullHint(Spatial.CullHint.Always);
        tooltip = label("", 15f, TEXT);
        tooltip.setBackground(rounded(DARK, 12f, 8f));
        tooltip.setCullHint(Spatial.CullHint.Always);
        crosshair = createCrosshair(assetManager);
        fadeMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fadeMaterial.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0f));
        fadeMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        fadeOverlay = new Geometry("Ueberblendung", new Quad(1f, 1f));
        fadeOverlay.setMaterial(fadeMaterial);
        fadeOverlay.setCullHint(Spatial.CullHint.Always);
        hud.attachChild(fadeOverlay);
        crosshair.setCullHint(Spatial.CullHint.Always);

        for (Spatial spatial : new Spatial[]{infoPanel, resultsPanel, roundsPanel, tray, hintLabel, toast, tooltip, crosshair}) {
            hud.attachChild(spatial);
        }
        refreshHint();
        setBalance(0, 0);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getGuiNode().attachChild(hud);
        int[] chipKeys = {KeyInput.KEY_1, KeyInput.KEY_2, KeyInput.KEY_3, KeyInput.KEY_4, KeyInput.KEY_5, KeyInput.KEY_6};
        for (int i = 0; i < chipKeys.length; i++) {
            inputManager.addMapping(KEY_MAPPINGS[i], new KeyTrigger(chipKeys[i]));
        }
        inputManager.addMapping("Hud_Spin", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Hud_Undo", new KeyTrigger(KeyInput.KEY_BACK));
        inputManager.addMapping("Hud_Clear", new KeyTrigger(KeyInput.KEY_DELETE));
        inputManager.addMapping("Hud_Repeat", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addListener(this, KEY_MAPPINGS);
    }

    @Override
    protected void onDisable() {
        hud.removeFromParent();
        for (String mapping : KEY_MAPPINGS) {
            inputManager.deleteMapping(mapping);
        }
        inputManager.removeListener(this);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }
        switch (name) {
            case "Hud_Spin" -> actions.spin();
            case "Hud_Undo" -> actions.undo();
            case "Hud_Clear" -> actions.clear();
            case "Hud_Repeat" -> actions.repeat();
            default -> selectChip(Integer.parseInt(name.substring("Hud_Chip".length())) - 1);
        }
    }

    // ---------------------------------------------------------------- Layout pro Frame

    @Override
    public void update(float tpf) {
        float screenWidth = getApplication().getGuiViewPort().getCamera().getWidth();
        float screenHeight = getApplication().getGuiViewPort().getCamera().getHeight();
        scale = Math.max(1f, screenHeight / REFERENCE_HEIGHT);
        hud.setLocalScale(scale);
        // Ab hier in HUD-Koordinaten (vor der Skalierung)
        float width = screenWidth / scale;
        float height = screenHeight / scale;

        infoPanel.setLocalTranslation(MARGIN, height - MARGIN, 0f);
        Vector3f infoSize = infoPanel.getPreferredSize();
        resultsPanel.setLocalTranslation(MARGIN, height - MARGIN - infoSize.y - 8f, 0f);
        Vector3f roundsSize = roundsPanel.getPreferredSize();
        roundsPanel.setLocalTranslation(width - roundsSize.x - MARGIN, height - MARGIN, 0f);

        Vector3f hintSize = hintLabel.getPreferredSize();
        hintLabel.setLocalTranslation((width - hintSize.x) / 2f, hintSize.y + 4f, 0f);
        Vector3f traySize = tray.getPreferredSize();
        tray.setLocalTranslation((width - traySize.x) / 2f, traySize.y + hintSize.y + 10f, 0f);

        crosshair.setLocalTranslation(width / 2f, height / 2f, 0f);
        if (fadeTime > 0f) {
            fadeTime -= tpf;
            fadeOverlay.setLocalScale(width, height, 1f);
            fadeOverlay.setLocalTranslation(0f, 0f, 20f);
            fadeMaterial.setColor("Color", new ColorRGBA(0f, 0f, 0f, Math.max(0f, Math.min(1f, fadeTime / 0.5f))));
            fadeOverlay.setCullHint(fadeTime > 0f ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
        }

        if (toastTime > 0f) {
            toastTime -= tpf;
            // kurz einblenden, am Ende ausblenden
            float fadeIn = (toastDuration - toastTime) / 0.15f;
            float fadeOut = toastTime / 0.5f;
            toast.setAlpha(Math.max(0f, Math.min(1f, Math.min(fadeIn, fadeOut))));
            // direkt ueber der Chipleiste, damit Zeiger und Kugel oben am Rad frei bleiben
            Vector3f toastSize = toast.getPreferredSize();
            toast.setLocalTranslation((width - toastSize.x) / 2f, traySize.y + hintSize.y + 22f + toastSize.y, 5f);
            if (toastTime <= 0f) {
                toast.setCullHint(Spatial.CullHint.Always);
            }
        }

        boolean showTooltip = hoveredBet != null && !dragging && !spinning;
        tooltip.setCullHint(showTooltip ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
        if (showTooltip) {
            tooltip.setText(tooltipText(hoveredBet));
            Vector2f cursor = inputManager.getCursorPosition();
            Vector3f size = tooltip.getPreferredSize();
            float x = Math.min(cursor.x / scale + 18f, width - size.x - 4f);
            float y = Math.max(cursor.y / scale - 14f, size.y + 4f);
            tooltip.setLocalTranslation(x, y, 6f);
        }
    }

    // ---------------------------------------------------------------- oeffentliche API

    public int getSelectedChip() {
        return CHIP_VALUES[selectedChip];
    }

    public void setBalance(int saldo, int onTable) {
        saldoLabel.setText(euro(saldo));
        onTableLabel.setText(onTable > 0 ? "Auf dem Tisch: " + euro(onTable) : "Keine Chips auf dem Tisch");
        stakeLabel.setText("Einsatz\n" + euro(onTable));
        hasBets = onTable > 0;
        refreshButtons();
    }

    public void setConnectionStatus(String text, boolean online) {
        connectionLabel.setText("● " + text);
        connectionLabel.setColor(online ? GOOD : BAD);
    }

    public void showTableMessage(String message) {
        tableMessageLabel.setText("Tisch: " + message);
    }

    /** Grosse Einblendung ueber der Chip-Leiste. */
    public void toast(String text, ColorRGBA color, float seconds) {
        toast.setText(text);
        toast.setColor(color);
        toast.setCullHint(Spatial.CullHint.Inherit);
        toastDuration = seconds;
        toastTime = seconds;
    }

    public void addResult(int number) {
        results.addFirst(number);
        while (results.size() > RESULT_COUNT) {
            results.removeLast();
        }
        resultsRow.clearChildren();
        boolean newest = true;
        for (int result : results) {
            Label badge = new Label(String.valueOf(result));
            badge.setFont(boldFont);
            badge.setFontSize(newest ? 20f : 15f);
            badge.setColor(TEXT);
            badge.setTextHAlignment(HAlignment.Center);
            badge.setTextVAlignment(VAlignment.Center);
            float size = newest ? 38f : 30f;
            badge.setPreferredSize(new Vector3f(size, size, 0f));
            badge.setBackground(rounded(result == 0 ? NUMBER_GREEN : RouletteRules.isRed(result) ? NUMBER_RED : NUMBER_BLACK, 0f, 0f));
            badge.setInsets(new Insets3f(newest ? 0 : 4, 2, 0, 2));
            resultsRow.addChild(badge);
            newest = false;
        }
    }

    /** Neuer Eintrag im Rundenverlauf, gruen bei Gewinn, rot bei Verlust. */
    public void addRound(String text, int net) {
        Label entry = label(text, 14f, net > 0 ? GOOD : net < 0 ? BAD : TEXT);
        roundEntries.addFirst(entry);
        while (roundEntries.size() > ROUND_COUNT) {
            roundEntries.removeLast();
        }
        int i = 0;
        for (Label round : roundEntries) {
            roundLabels[i].setText(round.getText());
            roundLabels[i].setColor(round.getColor());
            i++;
        }
    }

    /** Waehrend das Rad dreht, sind Chips und Buttons gesperrt. */
    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
        refreshButtons();
    }

    public void setCanRepeat(boolean canRepeat) {
        this.canRepeat = canRepeat;
        refreshButtons();
    }

    public void setHover(String bet) {
        hoveredBet = bet;
    }

    /** Umschauen mit rechter Maustaste: Fadenkreuz zeigen, HUD-Mausereignisse aussetzen. */
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
        GuiGlobals.getInstance().setCursorEventsEnabled(!dragging, true);
        crosshair.setCullHint(dragging ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
    }

    /** Kurz schwarz und wieder aufblenden. */
    public void fade() {
        fadeTime = 0.6f;
    }

    /** Gehmodus: andere Hilfezeile. */
    public void setWalking(boolean walking) {
        this.walking = walking;
        refreshHint();
    }

    public void setPixelSize(int pixelSize) {
        this.pixelSize = pixelSize;
        refreshHint();
    }

    /** true, wenn die Bildschirmposition (Fensterpixel) auf einem HUD-Panel liegt. */
    public boolean isOverHud(Vector2f cursor) {
        for (Panel panel : new Panel[]{infoPanel, resultsPanel, roundsPanel, tray}) {
            Vector3f topLeft = panel.getWorldTranslation();
            Vector3f size = panel.getSize().mult(scale);
            if (cursor.x >= topLeft.x && cursor.x <= topLeft.x + size.x
                    && cursor.y <= topLeft.y && cursor.y >= topLeft.y - size.y) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- Aufbau

    private void buildInfoPanel() {
        infoPanel = panel();
        label(infoPanel, player.getName().toUpperCase(Locale.ROOT), 13f, MUTED);
        saldoLabel = label(infoPanel, "", 34f, GOLD);
        saldoLabel.setFont(boldFont);
        saldoLabel.setFontSize(34f);
        onTableLabel = label(infoPanel, "", 15f, TEXT);
        connectionLabel = label(infoPanel, "● Verbinde mit Server ...", 13f, MUTED);
        tableMessageLabel = label(infoPanel, "", 12f, MUTED);
        tableMessageLabel.setMaxWidth(260f);
    }

    private void buildResultsPanel() {
        resultsPanel = panel();
        Label title = label(resultsPanel, "LETZTE ZAHLEN", 12f, MUTED);
        title.setTextHAlignment(HAlignment.Center);
        resultsRow = resultsPanel.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.None, FillMode.None)));
        label(resultsRow, "noch keine Drehung", 14f, MUTED);
    }

    private void buildRoundsPanel() {
        roundsPanel = panel();
        label(roundsPanel, "VERLAUF", 12f, MUTED);
        roundLabels = new Label[ROUND_COUNT];
        for (int i = 0; i < ROUND_COUNT; i++) {
            roundLabels[i] = label(roundsPanel, i == 0 ? "Noch keine Runde gespielt" : "", 14f, MUTED);
        }
    }

    private void buildTray() {
        tray = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.None, FillMode.Even));
        tray.setBackground(bordered(16f, 10f));

        chipButtons = new Button[CHIP_VALUES.length];
        chipTextures = new Texture2D[CHIP_VALUES.length];
        chipTexturesSelected = new Texture2D[CHIP_VALUES.length];
        for (int i = 0; i < CHIP_VALUES.length; i++) {
            int value = CHIP_VALUES[i];
            String text = String.valueOf(value);
            chipTextures[i] = HudGraphics.chip(ChipStackView.color(value), ChipStackView.textColor(value), text, false);
            chipTexturesSelected[i] = HudGraphics.chip(ChipStackView.color(value), ChipStackView.textColor(value), text, true);
            Button chip = tray.addChild(new Button(""));
            chip.setPreferredSize(new Vector3f(58f, 58f, 0f));
            chip.setInsets(new Insets3f(0, 3, 0, 3));
            int index = i;
            chip.addClickCommands(source -> selectChip(index));
            chip.addCommands(Button.ButtonAction.HighlightOn, source -> tintChip(index, true));
            chip.addCommands(Button.ButtonAction.HighlightOff, source -> tintChip(index, false));
            chipButtons[i] = chip;
        }

        stakeLabel = label(tray, "", 15f, TEXT);
        stakeLabel.setTextHAlignment(HAlignment.Center);
        stakeLabel.setTextVAlignment(VAlignment.Center);
        stakeLabel.setPreferredSize(new Vector3f(96f, 58f, 0f));
        stakeLabel.setInsets(new Insets3f(0, 10, 0, 6));

        undoButton = trayButton("Zurück", 96f, actions::undo);
        clearButton = trayButton("Löschen", 104f, actions::clear);
        repeatButton = trayButton("Wiederholen", 136f, actions::repeat);
        spinButton = trayButton("DREHEN", 150f, actions::spin);
        spinButton.setFont(boldFont);
        spinButton.setFontSize(24f);
        spinButton.setInsets(new Insets3f(0, 10, 0, 0));
        selectChip(selectedChip);
    }

    private Button trayButton(String text, float width, Runnable action) {
        Button button = tray.addChild(new Button(text));
        button.setFont(fontFor(16f));
        button.setFontSize(16f);
        button.setTextHAlignment(HAlignment.Center);
        button.setTextVAlignment(VAlignment.Center);
        button.setPreferredSize(new Vector3f(width, 58f, 0f));
        button.setInsets(new Insets3f(0, 3, 0, 3));
        button.addClickCommands(source -> {
            if (source.isEnabled()) {
                action.run();
            }
        });
        button.addCommands(Button.ButtonAction.HighlightOn, source -> styleButton(source, true));
        button.addCommands(Button.ButtonAction.HighlightOff, source -> styleButton(source, false));
        return button;
    }

    private void selectChip(int index) {
        selectedChip = index;
        for (int i = 0; i < chipButtons.length; i++) {
            chipButtons[i].setBackground(new QuadBackgroundComponent(i == index ? chipTexturesSelected[i] : chipTextures[i]));
            tintChip(i, false);
        }
    }

    private void tintChip(int index, boolean highlight) {
        QuadBackgroundComponent background = (QuadBackgroundComponent) chipButtons[index].getBackground();
        boolean active = index == selectedChip || highlight;
        float brightness = spinning ? 0.45f : active ? 1f : 0.75f;
        background.setColor(new ColorRGBA(brightness, brightness, brightness, 1f));
    }

    private void refreshButtons() {
        if (spinButton == null) {
            return;
        }
        undoButton.setEnabled(!spinning && hasBets);
        clearButton.setEnabled(!spinning && hasBets);
        repeatButton.setEnabled(!spinning && canRepeat);
        spinButton.setEnabled(!spinning && hasBets);
        for (Button button : new Button[]{undoButton, clearButton, repeatButton, spinButton}) {
            styleButton(button, false);
        }
        for (int i = 0; i < chipButtons.length; i++) {
            tintChip(i, false);
        }
    }

    private void styleButton(Button button, boolean highlight) {
        boolean enabled = button.isEnabled();
        boolean primary = button == spinButton;
        ColorRGBA fill;
        if (!enabled) {
            fill = new ColorRGBA(0.13f, 0.14f, 0.15f, 0.9f);
        } else if (primary) {
            fill = new ColorRGBA(0.08f, 0.52f, 0.24f, 1f);
        } else {
            fill = new ColorRGBA(0.17f, 0.20f, 0.23f, 1f);
        }
        if (highlight && enabled) {
            fill.interpolateLocal(ColorRGBA.White, 0.15f);
        }
        button.setBackground(rounded(fill, 10f, 6f));
        button.setColor(enabled ? ColorRGBA.White : new ColorRGBA(0.45f, 0.47f, 0.5f, 1f));
    }

    private void refreshHint() {
        if (hintLabel == null) {
            return;
        }
        String pixel = "P: Pixel " + (pixelSize <= 1 ? "aus" : pixelSize + "×");
        if (walking) {
            hintLabel.setText("W/A/S/D: gehen  ·  Shift: laufen  ·  Rechte Maustaste: umschauen  ·  Pfeiltasten: drehen  ·  "
                    + "F: zurück an den Platz  ·  Leertaste: Drehen  ·  M: Ton  ·  " + pixel);
        } else {
            hintLabel.setText("Klick: Chip setzen  ·  Rechte Maustaste: umschauen  ·  W/A/S/D: aufstehen & gehen  ·  V: zum Rad  ·  "
                    + "Leertaste: Drehen  ·  1–6: Chipwert  ·  Rücktaste: zurück  ·  R: wiederholen  ·  M: Ton  ·  " + pixel);
        }
    }

    private String tooltipText(String bet) {
        String payout = "zahlt " + RouletteRules.odds(bet) + ":1";
        String text = RouletteRules.describe(bet) + "  ·  " + payout;
        int amount = amountOnField.applyAsInt(bet);
        if (amount > 0) {
            text += "\nDein Einsatz: " + euro(amount);
        }
        if (!spinning) {
            text += "\nKlick: +" + euro(getSelectedChip());
        }
        return text;
    }

    // ---------------------------------------------------------------- Helfer

    private Container panel() {
        Container container = new Container();
        container.setBackground(bordered(16f, 12f));
        return container;
    }

    /** Dunkles Panel mit Goldrand und runden Ecken (Neun-Teilung). */
    private TbtQuadBackgroundComponent bordered(float marginX, float marginY) {
        TbtQuadBackgroundComponent background = nineSlice(panelTexture);
        background.setMargin(marginX, marginY);
        return background;
    }

    /** Einfarbiger Hintergrund mit runden Ecken, eingefaerbt ueber die Farbe. */
    private TbtQuadBackgroundComponent rounded(ColorRGBA color, float marginX, float marginY) {
        TbtQuadBackgroundComponent background = nineSlice(whiteTexture);
        background.setColor(color);
        background.setMargin(marginX, marginY);
        return background;
    }

    private static TbtQuadBackgroundComponent nineSlice(Texture2D texture) {
        int corner = HudGraphics.PANEL_CORNER;
        int far = HudGraphics.PANEL_SIZE - corner;
        return TbtQuadBackgroundComponent.create(texture, 0.6f, corner, corner, far, far, 0.01f, false);
    }

    private Label label(String text, float size, ColorRGBA color) {
        Label label = new Label(text);
        label.setFont(fontFor(size));
        label.setFontSize(size);
        label.setColor(color);
        return label;
    }

    /** Kleine Texte nehmen eine nahe an ihrer Groesse gerenderte Schrift, sonst verschwinden duenne Striche. */
    private BitmapFont fontFor(float size) {
        return size <= 16f ? smallFont : font;
    }

    private Label label(Container container, String text, float size, ColorRGBA color) {
        return container.addChild(label(text, size, color));
    }

    public static String euro(int amount) {
        return String.format(Locale.GERMANY, "%,d €", amount);
    }

    private Node createCrosshair(AssetManager assetManager) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.85f));
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        Node node = new Node("Fadenkreuz");
        float[][] bars = {{-12f, -1f, 8f, 2f}, {4f, -1f, 8f, 2f}, {-1f, -12f, 2f, 8f}, {-1f, 4f, 2f, 8f}, {-1f, -1f, 2f, 2f}};
        for (float[] bar : bars) {
            Geometry geometry = new Geometry("Fadenkreuz-Balken", new Quad(bar[2], bar[3]));
            geometry.setMaterial(material);
            geometry.setLocalTranslation(bar[0], bar[1], 0f);
            node.attachChild(geometry);
        }
        return node;
    }
}

/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Raycasting auf die Wettfelder: Das Feld unter dem Mauszeiger leuchtet auf, ein Linksklick setzt
 * einen Chip darauf. Liegt der Zeiger ueber dem HUD, wird nichts gepickt.
 */
public class BetSelectionState extends BaseAppState implements ActionListener {
    private static final String PLACE = "Bet_Place";

    private final TableLayout table;
    private final SeatCameraState seat;

    private InputManager inputManager;
    private Geometry hoverHighlight;
    private Material highlightMaterial;
    private float time;

    private String hoveredBet;
    private Consumer<String> clickListener = bet -> {
    };
    private Consumer<String> hoverListener = bet -> {
    };
    private Predicate<Vector2f> blockedByHud = cursor -> false;

    public BetSelectionState(TableLayout table, SeatCameraState seat) {
        this.table = table;
        this.seat = seat;
    }

    @Override
    protected void initialize(Application app) {
        inputManager = app.getInputManager();
        highlightMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        highlightMaterial.setColor("Color", new ColorRGBA(1f, 0.92f, 0.45f, 0.45f));
        highlightMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        hoverHighlight = new Geometry("Hover", new Box(0.5f, 0.5f, 0.5f));
        hoverHighlight.setMaterial(highlightMaterial);
        hoverHighlight.setQueueBucket(RenderQueue.Bucket.Transparent);
        hoverHighlight.setCullHint(Spatial.CullHint.Always);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getRootNode().attachChild(hoverHighlight);
        inputManager.addMapping(PLACE, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, PLACE);
    }

    @Override
    protected void onDisable() {
        hoverHighlight.removeFromParent();
        inputManager.deleteMapping(PLACE);
        inputManager.removeListener(this);
    }

    /** Linksklick auf ein Feld. */
    public void setClickListener(Consumer<String> clickListener) {
        this.clickListener = clickListener;
    }

    /** Aenderung des Feldes unter dem Zeiger (null = kein Feld). */
    public void setHoverListener(Consumer<String> hoverListener) {
        this.hoverListener = hoverListener;
    }

    /** Liefert true, wenn die Bildschirmposition von einem HUD-Element verdeckt ist. */
    public void setBlockedByHud(Predicate<Vector2f> blockedByHud) {
        this.blockedByHud = blockedByHud;
    }

    public String getHoveredBet() {
        return hoveredBet;
    }

    @Override
    public void update(float tpf) {
        time += tpf;
        // sanftes Pulsieren der Markierung
        highlightMaterial.setColor("Color", new ColorRGBA(1f, 0.92f, 0.45f, 0.32f + 0.14f * FastMath.sin(time * 5f)));

        String bet = pickBet();
        if (bet == null ? hoveredBet == null : bet.equals(hoveredBet)) {
            return;
        }
        hoveredBet = bet;
        hoverListener.accept(bet);
        if (bet == null) {
            hoverHighlight.setCullHint(Spatial.CullHint.Always);
            return;
        }
        TableLayout.Field field = table.getField(bet);
        hoverHighlight.setLocalTranslation(field.topCenter());
        hoverHighlight.setLocalScale(field.size().x + 0.006f, 0.004f, field.size().z + 0.006f);
        hoverHighlight.setCullHint(Spatial.CullHint.Never);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!PLACE.equals(name) || !isPressed) {
            return;
        }
        String bet = pickBet();
        if (bet != null) {
            clickListener.accept(bet);
        }
    }

    /** Raycast vom Mauszeiger (bzw. beim Umschauen von der Bildschirmmitte) gegen die Wettfelder. */
    private String pickBet() {
        if (!seat.isDragging() && blockedByHud.test(inputManager.getCursorPosition())) {
            return null;
        }
        CollisionResults results = new CollisionResults();
        table.getBetFields().collideWith(seat.getPickRay(), results);
        if (results.size() == 0) {
            return null;
        }
        return results.getClosestCollision().getGeometry().getUserData(TableLayout.BET_KEY);
    }
}

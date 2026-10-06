/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

import java.util.function.IntConsumer;

/**
 * Retro-Pixel-Look: Die 3D-Szene wird in geringer Aufloesung in eine Textur gerendert und dann
 * ohne Glaettung (Nearest-Filter) bildschirmfuellend hinter dem HUD angezeigt. Das HUD bleibt scharf.
 * <p>
 * Taste P schaltet die Pixelgroesse durch (1 = aus, 2, 3, 4).
 */
public class PixelationState extends BaseAppState implements ActionListener {
    private static final String CYCLE = "Pixel_Cycle";
    private static final int MAX_PIXEL_SIZE = 4;

    private int pixelSize;
    private IntConsumer listener = size -> {
    };

    private SimpleApplication app;
    private InputManager inputManager;
    private ViewPort viewPort;
    private Camera cam;
    private Camera guiCam;
    private FrameBuffer frameBuffer;
    private Picture picture;

    public PixelationState(int pixelSize) {
        this.pixelSize = pixelSize;
    }

    public int getPixelSize() {
        return pixelSize;
    }

    /** Wird nach jedem Wechsel der Pixelgroesse aufgerufen. */
    public void setListener(IntConsumer listener) {
        this.listener = listener;
    }

    @Override
    protected void initialize(Application application) {
        app = (SimpleApplication) application;
        inputManager = app.getInputManager();
        viewPort = app.getViewPort();
        cam = app.getCamera();
        guiCam = app.getGuiViewPort().getCamera();
    }

    @Override
    protected void cleanup(Application application) {
        release();
    }

    @Override
    protected void onEnable() {
        inputManager.addMapping(CYCLE, new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(this, CYCLE);
        apply(pixelSize);
    }

    @Override
    protected void onDisable() {
        inputManager.deleteMapping(CYCLE);
        inputManager.removeListener(this);
        apply(1);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (CYCLE.equals(name) && isPressed) {
            pixelSize = pixelSize % MAX_PIXEL_SIZE + 1;
            apply(pixelSize);
        }
    }

    private void apply(int size) {
        release();
        int fullWidth = guiCam.getWidth();
        int fullHeight = guiCam.getHeight();
        if (size <= 1) {
            cam.resize(fullWidth, fullHeight, false);
            listener.accept(1);
            return;
        }
        int width = Math.max(1, fullWidth / size);
        int height = Math.max(1, fullHeight / size);

        Texture2D texture = new Texture2D(width, height, Image.Format.RGBA8);
        texture.setMagFilter(Texture.MagFilter.Nearest);
        texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        frameBuffer = new FrameBuffer(width, height, 1);
        frameBuffer.addColorTarget(FrameBuffer.FrameBufferTarget.newTarget(texture));
        frameBuffer.setDepthTarget(FrameBuffer.FrameBufferTarget.newTarget(Image.Format.Depth));
        viewPort.setOutputFrameBuffer(frameBuffer);
        // Seitenverhaeltnis/Frustum bleibt gleich, nur die Render-Aufloesung sinkt
        cam.resize(width, height, false);

        picture = new Picture("Pixel-Szene");
        picture.setTexture(app.getAssetManager(), texture, false);
        picture.setWidth(fullWidth);
        picture.setHeight(fullHeight);
        // Ganz hinten im GUI-Bucket, damit HUD und Fadenkreuz darueber liegen
        picture.setLocalTranslation(0f, 0f, -100f);
        app.getGuiNode().attachChild(picture);
        listener.accept(size);
    }

    private void release() {
        if (picture != null) {
            picture.removeFromParent();
            picture = null;
        }
        if (frameBuffer != null) {
            viewPort.setOutputFrameBuffer(null);
            app.getRenderer().deleteFrameBuffer(frameBuffer);
            frameBuffer = null;
        }
    }
}

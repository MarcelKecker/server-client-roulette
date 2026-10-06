/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import java.util.function.Consumer;

/**
 * First-Person-Kamera am festen Sitzplatz (Ersatz fuer die FlyByCamera).
 * <ul>
 *     <li>Der Mauszeiger ist frei, damit man Felder und HUD direkt anklicken kann.</li>
 *     <li>Rechte Maustaste halten + Maus bewegen: umschauen (nur Kopfdrehung, der Platz bleibt fest).</li>
 *     <li>Mausrad: zoomen (Sichtfeld enger/weiter).</li>
 *     <li>V: aufstehen und zum Rad gehen bzw. zurueck zum Platz. Beim Drehen passiert das automatisch.</li>
 *     <li>W/A/S/D oder Pfeiltasten: aufstehen und frei im Raum herumgehen (Shift: laufen, Pfeil links/rechts:
 *     drehen), mit Kollision gegen Waende, Tische und Personen. F: zurueck an den Platz.</li>
 * </ul>
 * Alle Blick- und Positionswechsel werden weich animiert; beim Gehen wippt die Kamera leicht.
 */
public class SeatCameraState extends BaseAppState implements AnalogListener, ActionListener {
    /** Augenposition: so gewaehlt, dass Layout und Rad im Standardblick komplett zu sehen sind. */
    public static final Vector3f SEAT_EYE = new Vector3f(0f, 1.45f, 1.05f);

    private static final float BASE_YAW = 0f;
    private static final float BASE_PITCH = -18f * FastMath.DEG_TO_RAD;
    private static final float BASE_FOV = 66f;
    private static final float MIN_FOV = 20f;
    private static final float MAX_FOV = 72f;
    private static final float MAX_YAW = 80f * FastMath.DEG_TO_RAD;
    private static final float MIN_PITCH = -70f * FastMath.DEG_TO_RAD;
    private static final float MAX_PITCH = 25f * FastMath.DEG_TO_RAD;
    private static final float LOOK_SENSITIVITY = 1.3f;
    private static final float ZOOM_SENSITIVITY = 40f;
    private static final float SMOOTHING = 5f;

    private static final String DRAG = "Seat_Drag";
    private static final String LOOK_LEFT = "Seat_LookLeft";
    private static final String LOOK_RIGHT = "Seat_LookRight";
    private static final String LOOK_UP = "Seat_LookUp";
    private static final String LOOK_DOWN = "Seat_LookDown";
    private static final String ZOOM_IN = "Seat_ZoomIn";
    private static final String ZOOM_OUT = "Seat_ZoomOut";
    private static final String FOCUS = "Seat_Focus";
    private static final String FORWARD = "Walk_Forward";
    private static final String BACKWARD = "Walk_Backward";
    private static final String STRAFE_LEFT = "Walk_Left";
    private static final String STRAFE_RIGHT = "Walk_Right";
    private static final String TURN_LEFT = "Walk_TurnLeft";
    private static final String TURN_RIGHT = "Walk_TurnRight";
    private static final String RUN = "Walk_Run";
    private static final String SIT = "Walk_Sit";
    private static final String[] MAPPINGS = {DRAG, LOOK_LEFT, LOOK_RIGHT, LOOK_UP, LOOK_DOWN, ZOOM_IN, ZOOM_OUT, FOCUS,
            FORWARD, BACKWARD, STRAFE_LEFT, STRAFE_RIGHT, TURN_LEFT, TURN_RIGHT, RUN, SIT};

    /** Augenhoehe im Stehen und Geh-/Laufgeschwindigkeit (m/s). */
    private static final float STAND_HEIGHT = 1.70f;
    private static final float WALK_SPEED = 1.5f;
    private static final float RUN_SPEED = 3.0f;
    private static final float TURN_SPEED = 1.8f;
    private static final float STEP_LENGTH = 0.7f;

    /** Ein Blick: Drehung, Neigung und Sichtfeld (Grad). */
    private record View(float yaw, float pitch, float fov) {
    }

    private Camera cam;
    private Camera guiCam;
    private InputManager inputManager;

    private float yaw = BASE_YAW;
    private float pitch = BASE_PITCH;
    private float fov = BASE_FOV;
    private float targetYaw = BASE_YAW;
    private float targetPitch = BASE_PITCH;
    private float targetFov = BASE_FOV;

    private boolean dragging;
    /** Blick vor dem Fokussieren, wird danach wiederhergestellt; null = nicht fokussiert. */
    private View viewBeforeFocus;
    /** Beim Fokussieren wird der Blick laufend auf den Fokuspunkt gerichtet (bis man selbst umschaut). */
    private boolean autoAim;
    private Vector3f focusPoint = new Vector3f(0f, 1f, -1f);
    private Vector3f focusEye = SEAT_EYE.clone();
    private float focusFov = 36f;
    private final Vector3f eye = SEAT_EYE.clone();
    private final Vector3f targetEye = SEAT_EYE.clone();
    private final Vector3f previousEye = SEAT_EYE.clone();
    private float walkPhase;
    private float bob;
    private Consumer<Boolean> dragListener = dragging -> {
    };

    private boolean walking;
    private boolean walkAllowed = true;
    private boolean forward;
    private boolean backward;
    private boolean strafeLeft;
    private boolean strafeRight;
    private boolean turnLeft;
    private boolean turnRight;
    private boolean running;
    private float stepDistance;
    private WalkCollision collision = new WalkCollision();
    private Runnable stepListener = () -> {
    };
    private Consumer<Boolean> walkListener = walking -> {
    };

    @Override
    protected void initialize(Application app) {
        cam = app.getCamera();
        guiCam = app.getGuiViewPort().getCamera();
        inputManager = app.getInputManager();
        apply();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        inputManager.addMapping(DRAG, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping(LOOK_LEFT, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(LOOK_RIGHT, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping(LOOK_UP, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping(LOOK_DOWN, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(ZOOM_IN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ZOOM_OUT, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping(FOCUS, new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping(FORWARD, new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(BACKWARD, new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping(STRAFE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(STRAFE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(TURN_LEFT, new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(TURN_RIGHT, new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(RUN, new KeyTrigger(KeyInput.KEY_LSHIFT), new KeyTrigger(KeyInput.KEY_RSHIFT));
        inputManager.addMapping(SIT, new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, MAPPINGS);
        inputManager.setCursorVisible(true);
    }

    @Override
    protected void onDisable() {
        for (String mapping : MAPPINGS) {
            inputManager.deleteMapping(mapping);
        }
        inputManager.removeListener(this);
        inputManager.setCursorVisible(true);
    }

    /**
     * Wohin der Spieler beim Fokussieren geht ({@code eyePosition}) und worauf er dann schaut ({@code point}).
     */
    public void setFocus(Vector3f point, Vector3f eyePosition, float fov) {
        focusPoint = point.clone();
        focusEye = eyePosition.clone();
        focusFov = fov;
    }

    /** Wird aufgerufen, wenn das Umschauen mit gehaltener rechter Maustaste beginnt/endet. */
    public void setDragListener(Consumer<Boolean> dragListener) {
        this.dragListener = dragListener;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isFocused() {
        return viewBeforeFocus != null;
    }

    /** Aufstehen, zum Rad gehen und es heranholen. */
    public void focus() {
        if (viewBeforeFocus == null) {
            viewBeforeFocus = new View(targetYaw, targetPitch, targetFov);
        }
        targetEye.set(focusEye);
        targetFov = focusFov;
        autoAim = true;
    }

    /** Zurueck zum Platz und zum Blick von vor dem Fokussieren. */
    public void unfocus() {
        if (viewBeforeFocus == null) {
            return;
        }
        targetEye.set(SEAT_EYE);
        targetYaw = viewBeforeFocus.yaw();
        targetPitch = viewBeforeFocus.pitch();
        targetFov = viewBeforeFocus.fov();
        viewBeforeFocus = null;
        autoAim = false;
    }

    /** Aktuelle Augenposition (waehrend des Gehens zwischen Platz und Rad). */
    public Vector3f getEye() {
        return eye.clone();
    }

    public boolean isWalking() {
        return walking;
    }

    /** Waehrend einer Runde bleibt der Spieler am Tisch. */
    public void setWalkAllowed(boolean walkAllowed) {
        this.walkAllowed = walkAllowed;
    }

    public void setCollision(WalkCollision collision) {
        this.collision = collision;
    }

    /** Wird bei jedem Schritt aufgerufen (Schrittgeraeusch). */
    public void setStepListener(Runnable stepListener) {
        this.stepListener = stepListener;
    }

    /** Wird beim Aufstehen (true) und Hinsetzen (false) aufgerufen. */
    public void setWalkListener(Consumer<Boolean> walkListener) {
        this.walkListener = walkListener;
    }

    private void standUp() {
        walking = true;
        targetEye.set(eye.x, STAND_HEIGHT, eye.z);
        targetPitch = -0.12f;
        targetFov = BASE_FOV;
        stepDistance = 0f;
        walkListener.accept(true);
    }

    /** Sofort zurueck an den Platz (die App blendet dazu kurz ab). */
    public void sitDown() {
        if (!walking) {
            return;
        }
        walking = false;
        eye.set(SEAT_EYE);
        targetEye.set(SEAT_EYE);
        previousEye.set(SEAT_EYE);
        yaw = targetYaw = BASE_YAW;
        pitch = targetPitch = BASE_PITCH;
        fov = targetFov = BASE_FOV;
        walkListener.accept(false);
    }

    private void walk(float tpf) {
        if (turnLeft || turnRight) {
            targetYaw += (turnRight ? 1f : -1f) * TURN_SPEED * tpf;
            yaw = targetYaw;
        }
        float moveForward = (forward ? 1f : 0f) - (backward ? 1f : 0f);
        float moveSide = (strafeRight ? 1f : 0f) - (strafeLeft ? 1f : 0f);
        if (moveForward == 0f && moveSide == 0f) {
            return;
        }
        float length = FastMath.sqrt(moveForward * moveForward + moveSide * moveSide);
        float step = (running ? RUN_SPEED : WALK_SPEED) * tpf / length;
        float sin = FastMath.sin(yaw);
        float cos = FastMath.cos(yaw);
        // vorwaerts = Blickrichtung (sin, -cos), rechts = (cos, sin)
        float dx = (moveForward * sin + moveSide * cos) * step;
        float dz = (-moveForward * cos + moveSide * sin) * step;
        float beforeX = targetEye.x;
        float beforeZ = targetEye.z;
        collision.move(targetEye, dx, dz);
        eye.x = targetEye.x;
        eye.z = targetEye.z;
        stepDistance += FastMath.sqrt((targetEye.x - beforeX) * (targetEye.x - beforeX) + (targetEye.z - beforeZ) * (targetEye.z - beforeZ));
        if (stepDistance > STEP_LENGTH * (running ? 1.3f : 1f)) {
            stepDistance = 0f;
            stepListener.run();
        }
    }

    /**
     * Strahl fuer das Picking durch die Mausposition; waehrend des Umschauens (Zeiger versteckt)
     * durch die Bildschirmmitte.
     */
    public Ray getPickRay() {
        if (dragging) {
            return new Ray(cam.getLocation().clone(), cam.getDirection().clone());
        }
        // Mit Pixel-Filter rendert die Kamera kleiner als das Fenster -> Mausposition umrechnen
        Vector2f cursor = inputManager.getCursorPosition().clone();
        cursor.x *= (float) cam.getWidth() / guiCam.getWidth();
        cursor.y *= (float) cam.getHeight() / guiCam.getHeight();
        Vector3f near = cam.getWorldCoordinates(cursor, 0f);
        Vector3f far = cam.getWorldCoordinates(cursor, 1f);
        return new Ray(near, far.subtractLocal(near).normalizeLocal());
    }

    @Override
    public void update(float tpf) {
        // Gehen: erst aufrichten (Hoehe folgt schneller), dann vor- bzw. zuruecklaufen
        previousEye.set(eye);
        if (walking) {
            walk(tpf);
        }
        float kUp = 1f - FastMath.exp(-3.2f * tpf);
        float kMove = 1f - FastMath.exp(-1.7f * tpf);
        eye.y += (targetEye.y - eye.y) * kUp;
        eye.x += (targetEye.x - eye.x) * kMove;
        eye.z += (targetEye.z - eye.z) * kMove;
        float speed = tpf > 0f ? previousEye.distance(eye) / tpf : 0f;
        walkPhase += speed * tpf * 11f;
        bob = 0.022f * FastMath.sin(walkPhase) * FastMath.clamp(speed / 0.5f, 0f, 1f);

        if (autoAim && !dragging) {
            Vector3f direction = focusPoint.subtract(eye);
            targetYaw = FastMath.atan2(direction.x, -direction.z);
            targetPitch = FastMath.asin(direction.normalizeLocal().y);
        }
        float k = 1f - FastMath.exp(-SMOOTHING * tpf);
        yaw += (targetYaw - yaw) * k;
        pitch += (targetPitch - pitch) * k;
        fov += (targetFov - fov) * k;
        apply();
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch (name) {
            case ZOOM_IN -> targetFov -= value * ZOOM_SENSITIVITY;
            case ZOOM_OUT -> targetFov += value * ZOOM_SENSITIVITY;
            default -> {
                if (!dragging) {
                    return;
                }
                float delta = value * LOOK_SENSITIVITY * (fov / BASE_FOV);
                switch (name) {
                    case LOOK_LEFT -> targetYaw -= delta;
                    case LOOK_RIGHT -> targetYaw += delta;
                    case LOOK_UP -> targetPitch += delta;
                    case LOOK_DOWN -> targetPitch -= delta;
                    default -> {
                        return;
                    }
                }
                // selbst umschauen: automatisches Ausrichten aufs Rad aus, Position bleibt
                autoAim = false;
            }
        }
        if (!walking) {
            targetYaw = FastMath.clamp(targetYaw, -MAX_YAW, MAX_YAW);
        }
        targetPitch = FastMath.clamp(targetPitch, MIN_PITCH, MAX_PITCH);
        targetFov = FastMath.clamp(targetFov, MIN_FOV, MAX_FOV);
        if (dragging) {
            yaw = targetYaw;
            pitch = targetPitch;
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (DRAG.equals(name)) {
            dragging = isPressed;
            inputManager.setCursorVisible(!isPressed);
            dragListener.accept(isPressed);
        } else if (FOCUS.equals(name) && isPressed) {
            if (walking) {
                return;
            }
            if (isFocused()) {
                unfocus();
            } else {
                focus();
            }
        } else if (SIT.equals(name)) {
            if (isPressed) {
                sitDown();
            }
        } else {
            switch (name) {
                case FORWARD -> forward = isPressed;
                case BACKWARD -> backward = isPressed;
                case STRAFE_LEFT -> strafeLeft = isPressed;
                case STRAFE_RIGHT -> strafeRight = isPressed;
                case TURN_LEFT -> turnLeft = isPressed;
                case TURN_RIGHT -> turnRight = isPressed;
                case RUN -> running = isPressed;
                default -> {
                    return;
                }
            }
            // die erste Bewegung laesst den Spieler aufstehen (nicht waehrend einer Runde)
            if (isPressed && !walking && walkAllowed && !isFocused() && !RUN.equals(name)) {
                standUp();
            }
        }
    }

    private void apply() {
        float aspect = (float) guiCam.getWidth() / guiCam.getHeight();
        cam.setFrustumPerspective(fov, aspect, 0.02f, 100f);
        float cosPitch = FastMath.cos(pitch);
        Vector3f direction = new Vector3f(
                FastMath.sin(yaw) * cosPitch,
                FastMath.sin(pitch),
                -FastMath.cos(yaw) * cosPitch);
        cam.lookAtDirection(direction, Vector3f.UNIT_Y);
        cam.setLocation(eye.add(0f, bob, 0f));
    }
}

/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;

/**
 * Kollision beim Gehen: Statt einer Hindernisliste werden kurze Strahlen gegen die echte Szene
 * geschossen (in Knie-, Hueft- und Brusthoehe, mittig und seitlich versetzt). Getrennt nach X und Z
 * geprueft, damit man an Waenden und Tischen entlanggleitet statt haengen zu bleiben.
 */
public class WalkCollision {
    /** Koerperradius des Spielers in Metern. */
    private static final float RADIUS = 0.3f;
    private static final float[] HEIGHTS = {0.3f, 0.75f, 1.3f};
    private static final float[] OFFSETS = {-0.2f, 0f, 0.2f};
    /** Kennzeichnet Figuren, durch die man hindurch darf (z.B. der herumlaufende Kellner). */
    public static final String PASS_THROUGH = "durchlaessig";

    private final List<Spatial> obstacles = new ArrayList<>();

    /** Nimmt ein Hindernis auf; bei Knoten werden als durchlaessig markierte Kinder ausgelassen. */
    public WalkCollision add(Spatial spatial) {
        if (spatial instanceof Node node && hasPassThroughChild(node)) {
            for (Spatial child : node.getChildren()) {
                if (!Boolean.TRUE.equals(child.getUserData(PASS_THROUGH))) {
                    obstacles.add(child);
                }
            }
        } else {
            obstacles.add(spatial);
        }
        return this;
    }

    /** Verschiebt {@code position} um (dx, dz), soweit der Weg frei ist. */
    public void move(Vector3f position, float dx, float dz) {
        if (dx != 0f && isFree(position, new Vector3f(Math.signum(dx), 0f, 0f), Math.abs(dx))) {
            position.x += dx;
        }
        if (dz != 0f && isFree(position, new Vector3f(0f, 0f, Math.signum(dz)), Math.abs(dz))) {
            position.z += dz;
        }
    }

    private boolean isFree(Vector3f position, Vector3f direction, float distance) {
        Vector3f side = new Vector3f(-direction.z, 0f, direction.x);
        float reach = distance + RADIUS;
        for (float height : HEIGHTS) {
            for (float offset : OFFSETS) {
                Ray ray = new Ray(new Vector3f(position.x + side.x * offset, height, position.z + side.z * offset), direction);
                ray.setLimit(reach);
                CollisionResults results = new CollisionResults();
                for (Spatial obstacle : obstacles) {
                    obstacle.collideWith(ray, results);
                }
                if (results.size() > 0 && results.getClosestCollision().getDistance() <= reach) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasPassThroughChild(Node node) {
        for (Spatial child : node.getChildren()) {
            if (Boolean.TRUE.equals(child.getUserData(PASS_THROUGH))) {
                return true;
            }
        }
        return false;
    }
}

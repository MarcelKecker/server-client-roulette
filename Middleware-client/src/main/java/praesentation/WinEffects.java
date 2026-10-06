/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import praesentation.VoxelFactory.Pattern;

/**
 * Effekte nach der Drehung: der "Dolly" (Gewinnmarker), den der Croupier auf die Gewinnzahl stellt,
 * und ein goldener Funkenregen, wenn der Spieler gewinnt.
 */
public class WinEffects {
    private static final float DOLLY_DROP = 0.12f;

    private final Node node = new Node("Gewinn-Effekte");
    private final Node dolly = new Node("Dolly");
    private final ParticleEmitter sparkles;
    private final Vector3f dollyTarget = new Vector3f();
    private float dollyTime = -1f;

    public WinEffects(VoxelFactory voxels, AssetManager assetManager) {
        Quaternion upright = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);
        Geometry body = new Geometry("Dolly", new Cylinder(2, 24, 0.019f, 0.05f, true));
        body.setMaterial(voxels.material(new ColorRGBA(0.92f, 0.95f, 1f, 1f), Pattern.METAL));
        body.setLocalRotation(upright);
        body.setLocalTranslation(0f, 0.025f, 0f);
        Geometry cap = new Geometry("Dolly-Kappe", new Cylinder(2, 24, 0.022f, 0.014f, true));
        cap.setMaterial(voxels.material(VoxelFactory.GOLD, Pattern.METAL));
        cap.setLocalRotation(upright);
        cap.setLocalTranslation(0f, 0.056f, 0f);
        Geometry foot = new Geometry("Dolly-Fuss", new Cylinder(2, 24, 0.023f, 0.006f, true));
        foot.setMaterial(voxels.material(VoxelFactory.GOLD, Pattern.METAL));
        foot.setLocalRotation(upright);
        foot.setLocalTranslation(0f, 0.003f, 0f);
        dolly.attachChild(body);
        dolly.attachChild(cap);
        dolly.attachChild(foot);
        dolly.setCullHint(Spatial.CullHint.Always);
        node.attachChild(dolly);

        sparkles = new ParticleEmitter("Funken", ParticleMesh.Type.Triangle, 180);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture", HudGraphics.softDot());
        sparkles.setMaterial(material);
        sparkles.setImagesX(1);
        sparkles.setImagesY(1);
        sparkles.setStartColor(new ColorRGBA(1f, 0.88f, 0.4f, 1f));
        sparkles.setEndColor(new ColorRGBA(1f, 0.45f, 0.1f, 0f));
        sparkles.setStartSize(0.03f);
        sparkles.setEndSize(0.006f);
        sparkles.setGravity(0f, 2.2f, 0f);
        sparkles.setLowLife(0.9f);
        sparkles.setHighLife(1.7f);
        sparkles.setParticlesPerSec(0f);
        sparkles.getParticleInfluencer().setInitialVelocity(new Vector3f(0f, 1.7f, 0f));
        sparkles.getParticleInfluencer().setVelocityVariation(0.75f);
        sparkles.setQueueBucket(RenderQueue.Bucket.Transparent);
        node.attachChild(sparkles);
    }

    public Node getNode() {
        return node;
    }

    /** Dolly faellt von oben auf das Feld mit der Oberseite {@code top}. */
    public void showDolly(Vector3f top) {
        dollyTarget.set(top);
        dollyTime = 0f;
        dolly.setLocalTranslation(top.add(0f, DOLLY_DROP, 0f));
        dolly.setCullHint(Spatial.CullHint.Never);
    }

    public void hideDolly() {
        dolly.setCullHint(Spatial.CullHint.Always);
        dollyTime = -1f;
    }

    /** Goldener Funkenregen ueber {@code position}. */
    public void burst(Vector3f position) {
        sparkles.setLocalTranslation(position.add(0f, 0.05f, 0f));
        sparkles.emitAllParticles();
    }

    public void update(float tpf) {
        if (dollyTime >= 0f && dollyTime < 1f) {
            dollyTime = Math.min(1f, dollyTime + tpf * 4f);
            // kurzes Aufsetzen mit kleinem Nachfedern
            float fall = 1f - (1f - dollyTime) * (1f - dollyTime);
            float bounce = 0.008f * FastMath.sin(dollyTime * FastMath.PI) * (1f - dollyTime);
            dolly.setLocalTranslation(dollyTarget.x, dollyTarget.y + DOLLY_DROP * (1f - fall) + bounce, dollyTarget.z);
        }
    }
}

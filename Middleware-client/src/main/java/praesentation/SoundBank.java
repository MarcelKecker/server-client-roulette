/*
 * KI-GENERIERT: Diese Datei wurde mit Claude (Anthropic, Modell Claude Opus 5.5) ueber Claude Code erstellt
 * und nicht vom Projektteam geschrieben. Uebersicht aller KI-generierten Dateien: KI-GENERIERT.md
 */
package praesentation;

import com.jme3.app.Application;
import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Environment;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Casino-Klangkulisse, komplett zur Laufzeit synthetisiert (keine Audiodateien):
 * <ul>
 *     <li>Hintergrund: Stimmengewirr aus synthetischen Stimmen (Grundton + Vokal-Formanten, Silben)
 *     und leise Lounge-Musik (E-Piano-Akkorde, Walking Bass, Besen).</li>
 *     <li>Raeumliche Einzelgeraeusche mit Entfernung und Raumhall: Chips, Kugel im Kessel, Karten,
 *     Spielautomaten; dazu Schritte und ein Gewinn-Jingle.</li>
 * </ul>
 * Ohne Audio-Renderer (z.B. im Headless-Test) bleibt alles stumm.
 */
public class SoundBank {
    private static final int RATE = 44100;

    private final boolean enabled;
    private final Random random = new Random(42);
    private AudioNode murmur;
    private AudioNode music;
    private Pool chips;
    private Pool ball;
    private Pool cards;
    private Pool slotSpin;
    private Pool slotWin;
    private Pool win;
    private Pool steps;
    private boolean muted;

    /** Mehrere Knoten (auch mit verschiedenen Klangvarianten), reihum an beliebigen Orten abgespielt. */
    private static final class Pool {
        private final AudioNode[] nodes;
        private int next;

        Pool(AudioNode[] nodes) {
            this.nodes = nodes;
        }

        void play(Vector3f position, float volume, float pitch) {
            AudioNode node = nodes[next];
            next = (next + 1) % nodes.length;
            if (position != null) {
                node.setLocalTranslation(position);
                node.updateGeometricState();
            }
            node.setVolume(volume);
            node.setPitch(pitch);
            node.playInstance();
        }
    }

    public SoundBank(Application app, float spinSeconds) {
        AudioRenderer renderer = app.getAudioRenderer();
        enabled = renderer != null;
        if (!enabled) {
            return;
        }
        // leichter Raumhall eines grossen, mit Teppich gedaempften Saals
        renderer.setEnvironment(new Environment(1f, 0.85f, 0.32f, 0.55f, 1.5f, 0.6f, 0.22f, 0.012f, 0.55f, 0.02f));

        murmur = node("Gemurmel", babble(14f), false);
        murmur.setLooping(true);
        murmur.setVolume(0.45f);
        music = node("Musik", lounge(), false);
        music.setLooping(true);
        music.setVolume(0.16f);

        chips = pool("Chips", 6, i -> chipClack(2 + i % 3), true);
        ball = pool("Kugel", 2, i -> ballRoll(spinSeconds), true);
        cards = pool("Karten", 4, i -> cardFlick(), true);
        slotSpin = pool("Automat", 4, i -> slotReels(), true);
        slotWin = pool("Automatengewinn", 2, i -> slotJingle(), true);
        win = pool("Gewinn", 1, i -> winChime(), false);
        steps = pool("Schritt", 4, i -> footstep(), false);
    }

    public void startAmbience() {
        if (enabled && !muted) {
            murmur.play();
            music.play();
        }
    }

    /** Chips klacken an {@code position}. */
    public void playChip(Vector3f position, float volume) {
        play(chips, position, 0.9f * volume, 0.94f + random.nextFloat() * 0.12f);
    }

    /** Kugel wird ins Rad bei {@code wheelCenter} geworfen (Dauer = eine Drehung). */
    public void playSpin(Vector3f wheelCenter, float volume) {
        play(ball, wheelCenter, volume, 1f);
    }

    public void playCard(Vector3f position) {
        play(cards, position, 0.7f, 0.9f + random.nextFloat() * 0.2f);
    }

    public void playSlotSpin(Vector3f position) {
        play(slotSpin, position, 0.45f, 0.95f + random.nextFloat() * 0.1f);
    }

    public void playSlotWin(Vector3f position) {
        play(slotWin, position, 0.55f, 1f);
    }

    public void playWin() {
        play(win, null, 0.6f, 1f);
    }

    public void playStep() {
        play(steps, null, 0.32f, 0.9f + random.nextFloat() * 0.2f);
    }

    /** Ton an/aus; liefert den neuen Zustand (true = stumm). */
    public boolean toggleMute() {
        muted = !muted;
        if (enabled) {
            if (muted) {
                murmur.pause();
                music.pause();
            } else {
                murmur.play();
                music.play();
            }
        }
        return muted;
    }

    private void play(Pool pool, Vector3f position, float volume, float pitch) {
        if (enabled && !muted) {
            pool.play(position, volume, pitch);
        }
    }

    // ---------------------------------------------------------------- Hintergrund

    /** Stimmengewirr: viele synthetische Stimmen, die in Silben "sprechen", mit Pausen und Entfernung. */
    private float[] babble(float seconds) {
        int n = (int) (seconds * RATE);
        int fade = RATE;
        float[] out = new float[n + fade];
        float[][] vowels = {{800, 1200}, {500, 1900}, {320, 2300}, {500, 900}, {350, 800}, {650, 1600}};
        for (int voice = 0; voice < 16; voice++) {
            boolean female = random.nextBoolean();
            float f0 = female ? 180f + random.nextFloat() * 60f : 95f + random.nextFloat() * 45f;
            float loudness = 0.35f + random.nextFloat() * 0.65f;
            // Tiefpass je nach Entfernung der Stimme (weiter weg = dumpfer)
            float distance = 0.12f + random.nextFloat() * 0.25f;
            float lowpass = 0f;
            double phase = 0;
            int i = (int) (random.nextFloat() * RATE * 0.8f);
            while (i < out.length) {
                // Satz aus 3-9 Silben, dann Pause
                int syllables = 3 + random.nextInt(7);
                float phrasePitch = f0 * (0.9f + random.nextFloat() * 0.2f);
                for (int s = 0; s < syllables && i < out.length; s++) {
                    float[] vowel = vowels[random.nextInt(vowels.length)];
                    float scale = female ? 1.15f : 1f;
                    Biquad f1 = Biquad.bandpass(vowel[0] * scale, 5f);
                    Biquad f2 = Biquad.bandpass(vowel[1] * scale, 7f);
                    int length = (int) (RATE * (0.09f + random.nextFloat() * 0.18f));
                    float accent = 0.6f + random.nextFloat() * 0.4f;
                    for (int k = 0; k < length && i < out.length; k++, i++) {
                        float t = k / (float) length;
                        float envelope = (float) Math.sin(Math.PI * t) * accent;
                        // Tonhoehe faellt ueber den Satz leicht ab
                        float pitch = phrasePitch * (1f - 0.08f * (s / (float) syllables)) * (1f + 0.01f * (float) Math.sin(i * 0.0007));
                        phase += pitch / RATE;
                        float source = (float) (2 * (phase - Math.floor(phase)) - 1) + 0.15f * (random.nextFloat() * 2f - 1f);
                        float voiced = f1.process(source) + 0.5f * f2.process(source);
                        lowpass += (voiced - lowpass) * distance;
                        out[i] += loudness * envelope * lowpass;
                    }
                    i += (int) (RATE * random.nextFloat() * 0.05f);
                }
                i += (int) (RATE * (0.25f + random.nextFloat() * 1.2f));
            }
        }
        // entferntes Raumrauschen
        Biquad room = Biquad.bandpass(400f, 0.5f);
        for (int i = 0; i < out.length; i++) {
            out[i] += 0.02f * room.process(random.nextFloat() * 2f - 1f);
        }
        return normalize(loop(out, n, fade), 0.6f);
    }

    /** Lounge-Jazz im 90er-Tempo: Cmaj7 - Am7 - Dm7 - G7, E-Piano, Walking Bass, Besen. */
    private float[] lounge() {
        float beat = 60f / 90f;
        int bars = 8;
        int n = (int) (bars * 4 * beat * RATE);
        int tail = 3 * RATE;
        float[] out = new float[n + tail];
        float[][] chords = {
                {261.63f, 329.63f, 392.00f, 493.88f},
                {220.00f, 261.63f, 329.63f, 392.00f},
                {293.66f, 349.23f, 440.00f, 523.25f},
                {196.00f, 246.94f, 293.66f, 349.23f}};
        float[][] bassLines = {
                {65.41f, 82.41f, 98.00f, 110.00f},
                {110.00f, 98.00f, 82.41f, 73.42f},
                {73.42f, 87.31f, 110.00f, 98.00f},
                {98.00f, 123.47f, 146.83f, 61.74f}};
        for (int bar = 0; bar < bars; bar++) {
            int chord = bar % 4;
            float barStart = bar * 4 * beat;
            // E-Piano: auf 1 und auf "2 und" (synkopiert)
            for (float hit : new float[]{0f, 1.5f * beat}) {
                for (float frequency : chords[chord]) {
                    electricPiano(out, barStart + hit, frequency, hit == 0f ? 0.22f : 0.14f);
                }
            }
            for (int b = 0; b < 4; b++) {
                bass(out, barStart + b * beat, bassLines[chord][b]);
                brush(out, barStart + b * beat, b % 2 == 1 ? 0.10f : 0.05f);
            }
        }
        return normalize(loop(out, n, 0), 0.55f);
    }

    private void electricPiano(float[] out, float start, float frequency, float volume) {
        int from = (int) (start * RATE);
        int length = (int) (2.5f * RATE);
        for (int i = 0; i < length && from + i < out.length; i++) {
            float t = i / (float) RATE;
            float envelope = (float) Math.exp(-t * 1.6f) * Math.min(1f, t / 0.004f);
            float tremolo = 1f + 0.12f * (float) Math.sin(2 * Math.PI * 4.5 * t);
            double w = 2 * Math.PI * frequency * t;
            float tone = (float) (Math.sin(w + 0.8 * Math.exp(-t * 7) * Math.sin(w))
                    + 0.25 * Math.exp(-t * 9) * Math.sin(4 * w));
            out[from + i] += volume * envelope * tremolo * tone;
        }
    }

    private void bass(float[] out, float start, float frequency) {
        int from = (int) (start * RATE);
        int length = (int) (0.62f * RATE);
        for (int i = 0; i < length && from + i < out.length; i++) {
            float t = i / (float) RATE;
            float envelope = (float) Math.exp(-t * 4f) * Math.min(1f, t / 0.006f);
            double w = 2 * Math.PI * frequency * t;
            out[from + i] += 0.5f * envelope * (float) (Math.sin(w) + 0.35 * Math.sin(2 * w) + 0.1 * Math.sin(3 * w));
        }
    }

    private void brush(float[] out, float start, float volume) {
        int from = (int) (start * RATE);
        int length = (int) (0.18f * RATE);
        Biquad hiss = Biquad.bandpass(6000f, 0.7f);
        for (int i = 0; i < length && from + i < out.length; i++) {
            float t = i / (float) RATE;
            float envelope = (float) Math.exp(-t * 18f) * Math.min(1f, t / 0.01f);
            out[from + i] += volume * envelope * hiss.process(random.nextFloat() * 2f - 1f);
        }
    }

    // ---------------------------------------------------------------- Einzelgeraeusche

    /** Tonchips: mehrere kurze, helle Anschlaege kurz hintereinander. */
    private float[] chipClack(int impacts) {
        float[] out = new float[(int) (0.16f * RATE)];
        float time = 0f;
        float strength = 1f;
        for (int k = 0; k < impacts; k++) {
            impact(out, time, strength, 2700f + random.nextFloat() * 700f, 4400f + random.nextFloat() * 800f,
                    7000f + random.nextFloat() * 800f, 0.012f, 4000f);
            time += 0.012f + random.nextFloat() * 0.022f;
            strength *= 0.55f + random.nextFloat() * 0.2f;
        }
        return normalize(out, 0.8f);
    }

    /** Kugel: surrt im Kessel (immer tiefer, langsamer), klickt an einem Deflektor und springt durch die Faecher. */
    private float[] ballRoll(float seconds) {
        int n = (int) (seconds * RATE);
        float[] out = new float[n];
        float dropStart = seconds * 0.7f;
        Biquad rolling = Biquad.bandpass(1200f, 0.9f);
        Biquad rumble = Biquad.bandpass(260f, 0.8f);
        double revolution = 0;
        int lastDeflector = 0;
        float smooth = 0f;
        for (int i = 0; i < n; i++) {
            float t = i / (float) RATE;
            if (t >= dropStart) {
                break;
            }
            float u = t / dropStart;
            if (i % 256 == 0) {
                rolling = retune(rolling, 1300f - 600f * u, 0.9f);
            }
            float revolutionsPerSecond = 3.2f - 1.9f * u;
            revolution += revolutionsPerSecond / RATE;
            float noise = random.nextFloat() * 2f - 1f;
            float amplitude = (0.55f - 0.25f * u) * (0.8f + 0.2f * (float) Math.sin(2 * Math.PI * revolution));
            float fadeIn = Math.min(1f, t / 0.25f);
            // Tiefpass (~1,5 kHz): dumpfes Kugelgrollen statt Rauschen
            smooth += ((rolling.process(noise) + 0.8f * rumble.process(noise)) - smooth) * 0.22f;
            out[i] = fadeIn * amplitude * 1.8f * smooth;
            // leichtes Tickern ueber die acht Deflektoren
            int deflector = (int) (revolution * 8);
            if (deflector != lastDeflector && u > 0.35f) {
                lastDeflector = deflector;
                impact(out, t, 0.06f + 0.1f * u, 2200f, 3600f, 5200f, 0.006f, 3000f);
            }
        }
        // Aufschlag am Deflektor, dann Springen in den Faechern
        impact(out, dropStart, 1f, 2400f, 3900f, 6100f, 0.02f, 3500f);
        float time = dropStart + 0.18f;
        float gap = 0.2f;
        float strength = 0.75f;
        while (time < seconds - 0.25f && strength > 0.06f) {
            impact(out, time, strength, 1600f + random.nextFloat() * 700f, 3000f + random.nextFloat() * 900f,
                    5200f + random.nextFloat() * 900f, 0.018f, 2500f);
            time += gap;
            gap *= 0.74f;
            strength *= 0.72f;
        }
        // kleines Rasseln, bevor die Kugel liegen bleibt
        for (int k = 0; k < 7 && time < seconds - 0.02f; k++) {
            impact(out, time, 0.08f * (1f - k / 7f), 3000f, 5000f, 7000f, 0.004f, 5000f);
            time += 0.025f;
        }
        return normalize(out, 0.85f);
    }

    /** Karte wird ueber den Filz geschoben und abgelegt. */
    private float[] cardFlick() {
        float[] out = new float[(int) (0.14f * RATE)];
        Biquad swish = Biquad.bandpass(3200f, 1.2f);
        for (int i = 0; i < out.length; i++) {
            float t = i / (float) RATE;
            float envelope = t < 0.06f ? t / 0.06f : (float) Math.exp(-(t - 0.06f) * 60f);
            out[i] += 0.6f * envelope * swish.process(random.nextFloat() * 2f - 1f);
        }
        impact(out, 0.065f, 0.5f, 900f, 1800f, 3200f, 0.006f, 2000f);
        return normalize(out, 0.7f);
    }

    /** Automat: Walzen tickern, am Anfang ein kurzes elektronisches Signal. */
    private float[] slotReels() {
        float[] out = new float[(int) (1.4f * RATE)];
        float[] bleep = {880f, 1174.66f, 1318.51f};
        for (int k = 0; k < bleep.length; k++) {
            square(out, k * 0.06f, 0.06f, bleep[k], 0.25f);
        }
        for (float t = 0.2f; t < 1.3f; t += 0.045f) {
            impact(out, t, 0.35f, 1500f, 2600f, 4000f, 0.004f, 3000f);
        }
        return normalize(out, 0.7f);
    }

    /** Automat: kleine Gewinnmelodie und klimpernde Muenzen. */
    private float[] slotJingle() {
        float[] out = new float[(int) (1.6f * RATE)];
        float[] melody = {1046.5f, 1318.51f, 1567.98f, 2093f, 1567.98f, 2093f};
        for (int k = 0; k < melody.length; k++) {
            square(out, k * 0.09f, 0.08f, melody[k], 0.3f);
        }
        for (int k = 0; k < 14; k++) {
            float t = 0.5f + random.nextFloat() * 1f;
            float f = 4200f + random.nextFloat() * 2000f;
            impact(out, t, 0.2f + random.nextFloat() * 0.2f, f, f * 1.47f, f * 2.1f, 0.05f, 8000f);
        }
        return normalize(out, 0.75f);
    }

    /** Gewinn: Glocken-Arpeggio (FM-Synthese) mit Glitzern. */
    private float[] winChime() {
        float[] out = new float[(int) (2f * RATE)];
        float[] notes = {1046.5f, 1318.51f, 1567.98f, 2093f, 2637f};
        for (int k = 0; k < notes.length; k++) {
            int from = (int) (k * 0.08f * RATE);
            for (int i = from; i < out.length; i++) {
                float t = (i - from) / (float) RATE;
                double w = 2 * Math.PI * notes[k] * t;
                float index = 2.5f * (float) Math.exp(-t * 4f);
                float envelope = (float) Math.exp(-t * 2.8f) * Math.min(1f, t / 0.003f);
                out[i] += 0.35f * envelope * (float) Math.sin(w + index * Math.sin(3.5 * w));
            }
        }
        for (int k = 0; k < 20; k++) {
            float t = 0.3f + random.nextFloat() * 1.2f;
            float f = 5000f + random.nextFloat() * 3000f;
            impact(out, t, 0.08f, f, f * 1.3f, f * 1.7f, 0.03f, 9000f);
        }
        return normalize(out, 0.7f);
    }

    /** Schritt auf Teppich: gedaempfter, tiefer Stoss. */
    private float[] footstep() {
        float[] out = new float[(int) (0.16f * RATE)];
        float low = 0f;
        for (int i = 0; i < out.length; i++) {
            float t = i / (float) RATE;
            float envelope = Math.min(1f, t / 0.004f) * (float) Math.exp(-t * 32f);
            low += ((random.nextFloat() * 2f - 1f) - low) * 0.04f;
            out[i] = envelope * (low * 3f + 0.4f * (float) Math.sin(2 * Math.PI * 75 * t));
        }
        return normalize(out, 0.8f);
    }

    // ---------------------------------------------------------------- Bausteine

    /** Harter Anschlag: drei gedaempfte Resonanzen plus kurzer, gefilterter Rauschimpuls. */
    private void impact(float[] out, float at, float strength, float f1, float f2, float f3, float decay, float noiseFrequency) {
        int from = (int) (at * RATE);
        int length = (int) (decay * 6f * RATE);
        Biquad crack = Biquad.bandpass(noiseFrequency, 1.2f);
        for (int i = 0; i < length && from + i < out.length; i++) {
            float t = i / (float) RATE;
            float envelope = (float) Math.exp(-t / decay);
            float tone = (float) (Math.sin(2 * Math.PI * f1 * t) + 0.6 * Math.sin(2 * Math.PI * f2 * t)
                    + 0.3 * Math.sin(2 * Math.PI * f3 * t));
            float noise = crack.process(random.nextFloat() * 2f - 1f) * (float) Math.exp(-t / 0.0025f);
            out[from + i] += strength * (0.45f * envelope * tone + 1.2f * noise);
        }
    }

    /** Weicher Rechteckton (Automaten-Piepser). */
    private void square(float[] out, float at, float duration, float frequency, float volume) {
        int from = (int) (at * RATE);
        int length = (int) (duration * RATE);
        for (int i = 0; i < length && from + i < out.length; i++) {
            float t = i / (float) RATE;
            float envelope = Math.min(1f, t / 0.003f) * Math.min(1f, (duration - t) / 0.01f);
            double w = 2 * Math.PI * frequency * t;
            float tone = (float) (Math.sin(w) + Math.sin(3 * w) / 3 + Math.sin(5 * w) / 5);
            out[from + i] += volume * envelope * tone;
        }
    }

    /** Macht eine Schleife: was ueber das Ende hinausklingt, wird in den Anfang eingeblendet. */
    private static float[] loop(float[] source, int length, int crossfade) {
        float[] loop = new float[length];
        System.arraycopy(source, 0, loop, 0, length);
        for (int i = length; i < source.length; i++) {
            int target = i - length;
            float weight = crossfade > 0 ? 1f - Math.min(1f, target / (float) crossfade) : 1f;
            if (crossfade > 0) {
                float fadeIn = Math.min(1f, target / (float) crossfade);
                loop[target] = loop[target] * fadeIn + source[i] * weight;
            } else {
                loop[target] += source[i];
            }
        }
        return loop;
    }

    private static float[] normalize(float[] samples, float peak) {
        float max = 1e-6f;
        for (float sample : samples) {
            max = Math.max(max, Math.abs(sample));
        }
        float factor = peak / max;
        for (int i = 0; i < samples.length; i++) {
            samples[i] *= factor;
        }
        return samples;
    }

    private interface Generator {
        float[] create(int variant);
    }

    private Pool pool(String name, int size, Generator generator, boolean positional) {
        AudioNode[] nodes = new AudioNode[size];
        for (int i = 0; i < size; i++) {
            nodes[i] = node(name, generator.create(i), positional);
        }
        return new Pool(nodes);
    }

    private static AudioNode node(String name, float[] samples, boolean positional) {
        ByteBuffer data = BufferUtils.createByteBuffer(samples.length * 2);
        for (float sample : samples) {
            data.putShort((short) (Math.max(-1f, Math.min(1f, sample)) * 32767));
        }
        data.flip();
        AudioBuffer buffer = new AudioBuffer();
        buffer.setupFormat(1, 16, RATE);
        buffer.updateData(data);
        AudioNode node = new AudioNode(buffer, new AudioKey(name));
        node.setPositional(positional);
        if (positional) {
            node.setRefDistance(1.6f);
            node.setMaxDistance(30f);
            node.setReverbEnabled(true);
        } else {
            node.setReverbEnabled(false);
        }
        return node;
    }

    private static Biquad retune(Biquad old, float frequency, float q) {
        Biquad tuned = Biquad.bandpass(frequency, q);
        tuned.x1 = old.x1;
        tuned.x2 = old.x2;
        tuned.y1 = old.y1;
        tuned.y2 = old.y2;
        return tuned;
    }

    /** Einfacher Bandpass (RBJ-Biquad). */
    private static final class Biquad {
        private final float b0;
        private final float b2;
        private final float a1;
        private final float a2;
        private float x1;
        private float x2;
        private float y1;
        private float y2;

        private Biquad(float b0, float b2, float a1, float a2) {
            this.b0 = b0;
            this.b2 = b2;
            this.a1 = a1;
            this.a2 = a2;
        }

        static Biquad bandpass(float frequency, float q) {
            double w = 2 * Math.PI * frequency / RATE;
            double alpha = Math.sin(w) / (2 * q);
            double a0 = 1 + alpha;
            return new Biquad((float) (alpha / a0), (float) (-alpha / a0),
                    (float) (-2 * Math.cos(w) / a0), (float) ((1 - alpha) / a0));
        }

        float process(float x) {
            float y = b0 * x + b2 * x2 - a1 * y1 - a2 * y2;
            x2 = x1;
            x1 = x;
            y2 = y1;
            y1 = y;
            return y;
        }
    }
}

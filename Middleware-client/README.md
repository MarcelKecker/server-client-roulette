# Middleware-client – Voxel-Roulette

> **KI-generiert:** Diese README wurde mit Claude (Anthropic, Modell Claude Opus 5.5) über Claude Code erstellt. Welche Dateien KI-generiert sind und welche vom Projektteam stammen, steht in [KI-GENERIERT.md](KI-GENERIERT.md).

Client zur Socket-Middleware des Roulette-Tischs. Neben der Konsolen-Version (`Main.java`) gibt es ein
3D-Voxel-Frontend mit jMonkeyEngine, gespielt aus der First-Person-Perspektive am Tisch: ein belebter
Casino-Raum im Voxel-Look mit einem europäischen Roulettetisch, einem Croupier, der die Kugel wirft und
das Ergebnis ansagt, Mitspielern mit eigenen Farbchips, Bar, Kellner und Casino-Geräuschen. Dahinter
liegt eine Spielhalle mit weiterem Roulettetisch, Blackjack- und Pokertischen, Spielautomaten und Kasse.

## Voraussetzungen

- JDK 25 (`Main.java` nutzt eine *compact source file*, deshalb `release 25`)
- Maven 3.9+
- Grafikkarte mit OpenGL 3.2 oder neuer

> **Windows on ARM (z.B. Surface/Snapdragon X):** Der Qualcomm-Treiber bringt kein OpenGL mit.
> Installiere aus dem Microsoft Store das *„OpenCL™-, OpenGL®- und Vulkan®-Kompatibilitätspaket“*.
> Die passenden LWJGL-Natives (`natives-windows-arm64`) ergänzt die `pom.xml` automatisch über das
> Profil `windows-arm64`.

## Bauen

```bash
mvn clean package
```

Die JUnit-Tests (`RoulettetableClientProxyTest`) brauchen einen laufenden Server und sind deshalb
standardmäßig übersprungen. Ausführen mit `mvn test -DskipTests=false`.

## Starten

Hauptklasse: **`praesentation.RouletteApp`**

```bash
mvn compile exec:exec
```

Mit eigenem Spielernamen (sonst wird `Spieler-XXXX` verwendet):

```bash
mvn compile exec:exec -Dspieler=Leo
```

Im Vollbild (native Auflösung des Hauptbildschirms, das HUD skaliert mit):

```bash
mvn compile exec:exec -Dvollbild=true -Dspieler=Leo
```

In IntelliJ: `pom.xml` als Maven-Projekt laden, dann `RouletteApp.main` ausführen. Für Vollbild in der
Run-Configuration bei *Program arguments* `--vollbild` eintragen (optional mit Namen: `Leo --vollbild`).

Beim Start verbindet sich die App mit dem Server auf `127.0.0.1:12345`
(`RoulettetableClientProxy` + `Player`, anschließend `enter`). Läuft kein Server, startet die App im
Offline-Modus: Du kannst trotzdem spielen, das HUD zeigt dann „Offline“.

## Steuerung

| Eingabe | Aktion |
|---|---|
| Linksklick auf ein Feld | Chip des gewählten Werts setzen (mehrfach klicken = mehr Chips) |
| Chip in der Leiste anklicken oder 1–6 | Chipwert wählen: 1, 5, 10, 25, 100, 500 € |
| DREHEN oder Leertaste/Enter | Runde starten |
| Zurück oder Rücktaste | letzten Chip zurücknehmen |
| Löschen oder Entf | alle Chips vom Tisch nehmen |
| Wiederholen oder R | Wetten der letzten Runde erneut setzen |
| Rechte Maustaste halten + Maus | Umschauen (nur Kopfdrehung, der Sitzplatz bleibt fest) |
| Mausrad | Zoom |
| V | aufstehen und zum Rad gehen / zurück zum Platz (beim Drehen automatisch) |
| W/A/S/D oder Pfeil hoch/runter | aufstehen und frei im Raum herumgehen (mit Kollision) |
| Shift | laufen |
| Pfeil links/rechts | drehen (beim Gehen) |
| F | zurück an den Platz |
| M | Ton an/aus |
| P | Retro-Pixel-Look: aus, 2×, 3×, 4× (Start: aus) |
| ESC | Beenden |

Ablauf: Chipwert wählen, Chips auf beliebige Felder setzen (Zahl 0–36, Rot/Schwarz, Gerade/Ungerade,
1–18/19–36, Dutzende, Kolonnen „2:1“, auch mehrere Felder pro Runde), dann DREHEN. Du stehst auf und
gehst zum Rad, nach der Runde zurück an deinen Platz; der Croupier stellt den Gewinnmarker („Dolly“)
auf die Gewinnzahl. Der Einsatz wird beim Setzen vom Saldo abgezogen. Beim
Drehen geht jede Wette per `proxy.post(player, "<feld> <einsatz>")` (z.B. `"rot 10"`) an den Server,
der Croupier wirft die Kugel und die Kamera schwenkt aufs Rad. Der Server wertet bisher nichts aus,
deshalb zieht der Client die Gewinnzahl lokal und schreibt Gewinne gut (Zahl 35:1, einfache Chancen
1:1, Dutzend/Kolonne 2:1). Startsaldo: 1.000 €.

Das HUD zeigt oben links Saldo, Einsatz und Verbindung, darunter die letzten Gewinnzahlen, oben rechts
den Rundenverlauf und unten die Chipleiste. Fährt man mit der Maus über ein Feld, erscheint ein
Tooltip mit Quote und eigenem Einsatz.

## Aufbau

```
src/main/java/
├── Main.java               Konsolen-Version (derzeit vom Build ausgenommen, s.u.)
├── communication/          Middleware: RoulettetableClientProxy, PlayerServerProxy, Log*
├── fachlogik/Player.java   IPlayer-Implementierung (jetzt mit echtem saldo-Feld)
├── interfaces/             IPlayer, IRoulettetable
└── praesentation/          3D-Frontend
    ├── RouletteApp         SimpleApplication, Verbindung zum Server, Wettablauf
    ├── SeatCameraState     First-Person-Kamera am Sitzplatz: Umschauen, Zoom, Fokus aufs Rad
    ├── BetSelectionState   Raycasting auf die Wettfelder (Mauszeiger), Hover-Markierung
    ├── BetBoard            Chips der laufenden Runde, Zurück, Wiederholen, Auszahlung
    ├── ChipStackView       3D-Chipstapel auf den Feldern
    ├── HudState            Lemur-HUD: Saldo, Chipleiste, DREHEN, letzte Zahlen, Verlauf, Tooltip
    ├── HudGraphics         mit Java2D gezeichnete Chips und Panels mit runden Ecken
    ├── FontFactory         scharfe Schriften aus Systemfonts (Segoe UI, Georgia) zur Laufzeit
    ├── PixelationState     Retro-Pixel-Filter (Szene in niedriger Aufloesung, scharf hochskaliert)
    ├── Room                Casino-Raum: Waende, Decke, Saeulen, Lampen, Bilder, Bar, Tuer, Deko
    ├── TableLayout         Tisch und Wett-Layout aus Voxel-Boxen
    ├── RouletteWheel       Rad mit 37 Segment-Boxen, Kugel, Dreh-Animation
    ├── Dealer              Voxel-Croupier mit Gesten und Sprechblase
    ├── VoxelPerson         Voxel-Figur mit Gelenken (stehen, sitzen, gehen, jubeln)
    ├── Crowd               Mitspieler, Zuschauer, Barkeeper, Bargäste, Kartenspieler, Paar, Kellner
    ├── Hall                Spielhalle: Roulette, 2x Blackjack, Poker, Automaten, Kasse, Kronleuchter
    ├── WinEffects          Gewinnmarker (Dolly) und Funkenregen
    ├── SoundBank           synthetisierter 3D-Ton mit Raumhall: Stimmengewirr, Lounge-Musik, Chips, Kugel, Karten, Automaten, Schritte
    ├── WalkCollision       Kollision beim Gehen per Strahlentest gegen die Szene
    ├── RouletteRules       lokale Gewinnregeln
    └── VoxelFactory        Boxen mit Pixel-Texturen, Mesh-Builder mit Vertexfarben für Figuren
```

Für das HUD wird **Lemur** statt Nifty GUI verwendet. Lemur wird aktiv gepflegt, wird komplett in Java
aufgebaut (ohne XML-Screens mit Controller-Klassen) und hängt direkt im jME-Szenengraphen. Für ein
kleines Overlay mit Labels, Textfeld und Button ist es deutlich schlanker.

## Offene Punkte

- `post(player, bet, stake)` gibt es inzwischen im `RoulettetableClientProxy`; der `<excludes>`-Block für
  `Main.java` in der `pom.xml` (siehe `TODO`) kann entfernt werden.
- `PlayerServerProxy` ruft `Player.hearResults(result, win)` mit zwei Argumenten auf, `IPlayer`/`Player`
  haben aber nur `hearResults(String)` – das muss in der Middleware angeglichen werden, sonst baut nichts.
- `Player.getSaldo()` liefert derzeit 0; das Frontend führt den angezeigten Saldo deshalb selbst.
- Der Server (`../Middleware`) kompiliert derzeit nicht (Merge-Konflikte, Reste aus dem Chat-Projekt).

# KI-generierte Dateien

> **KI-generiert:** Auch diese Übersicht wurde mit Claude erstellt.

Die folgenden Dateien wurden mit **Claude** (Anthropic, Modell Claude Opus 5.5) über **Claude Code**
erstellt und **nicht vom Projektteam** geschrieben. Jede dieser Dateien trägt zusätzlich einen
Kopfvermerk `KI-GENERIERT`.

## Build und Dokumentation

| Datei | Inhalt |
|---|---|
| [pom.xml](pom.xml) | Maven-Build: jMonkeyEngine, Lemur, JUnit, Windows-ARM64-Natives, Start per `exec:exec` |
| [README.md](README.md) | Bauen, Starten, Steuerung, Aufbau des Frontends |
| [KI-GENERIERT.md](KI-GENERIERT.md) | diese Übersicht |

## 3D-Frontend (`src/main/java/praesentation/`)

| Datei | Inhalt |
|---|---|
| [RouletteApp.java](src/main/java/praesentation/RouletteApp.java) | Hauptklasse (`SimpleApplication`), Verbindung zum Server über die Middleware, Spielablauf |
| [SeatCameraState.java](src/main/java/praesentation/SeatCameraState.java) | First-Person-Kamera: Sitzplatz, Umschauen, Zoom, Gang zum Rad, freies Gehen |
| [WalkCollision.java](src/main/java/praesentation/WalkCollision.java) | Kollision beim Gehen |
| [BetSelectionState.java](src/main/java/praesentation/BetSelectionState.java) | Raycasting auf die Wettfelder |
| [BetBoard.java](src/main/java/praesentation/BetBoard.java) | Chips der laufenden Runde, Zurück, Wiederholen, Auszahlung |
| [RouletteRules.java](src/main/java/praesentation/RouletteRules.java) | lokale Gewinnregeln und Quoten |
| [HudState.java](src/main/java/praesentation/HudState.java) | HUD mit Lemur |
| [HudGraphics.java](src/main/java/praesentation/HudGraphics.java) | gezeichnete HUD-Grafiken (Chips, Panels) |
| [FontFactory.java](src/main/java/praesentation/FontFactory.java) | hochauflösende Schriften aus Systemfonts |
| [Mipmaps.java](src/main/java/praesentation/Mipmaps.java) | Mipmaps für transparente Texturen |
| [PixelationState.java](src/main/java/praesentation/PixelationState.java) | Retro-Pixel-Filter |
| [VoxelFactory.java](src/main/java/praesentation/VoxelFactory.java) | Voxel-Boxen, Pixel-Texturen, Mesh-Builder |
| [Room.java](src/main/java/praesentation/Room.java) | Casino-Raum |
| [Hall.java](src/main/java/praesentation/Hall.java) | Spielhalle mit weiteren Tischen, Automaten und Kasse |
| [TableLayout.java](src/main/java/praesentation/TableLayout.java) | eigener Roulettetisch und Wett-Layout |
| [RouletteWheel.java](src/main/java/praesentation/RouletteWheel.java) | Roulette-Rad mit Dreh-Animation |
| [ChipStackView.java](src/main/java/praesentation/ChipStackView.java) | 3D-Chipstapel |
| [WinEffects.java](src/main/java/praesentation/WinEffects.java) | Gewinnmarker (Dolly) und Funkenregen |
| [VoxelPerson.java](src/main/java/praesentation/VoxelPerson.java) | Voxel-Figur mit Gelenken |
| [Dealer.java](src/main/java/praesentation/Dealer.java) | Croupier |
| [Crowd.java](src/main/java/praesentation/Crowd.java) | Mitspieler, Bar, Kellner und weitere Gäste |
| [SoundBank.java](src/main/java/praesentation/SoundBank.java) | synthetisierte Geräusche und Musik |

## Dateien des Projektteams

Nicht KI-generiert und **nicht gekennzeichnet** sind:

- [Main.java](src/main/java/Main.java)
- [communication/](src/main/java/communication/) (`RoulettetableClientProxy`, `PlayerServerProxy`, `LogReader`, `LogWriter`)
- [fachlogik/Player.java](src/main/java/fachlogik/Player.java)
- [interfaces/](src/main/java/interfaces/) (`IPlayer`, `IRoulettetable`)
- [RoulettetableClientProxyTest.java](src/test/java/communication/RoulettetableClientProxyTest.java)

**KI-Beteiligung an diesen Dateien:**
- Claude hat sie für den Maven-Build aus `src/` nach `src/main/java/` bzw. `src/test/java/` verschoben (`git mv`), am Inhalt aber nichts geändert.
- Claude hatte `Player.java` anfangs um ein `saldo`-Feld ergänzt. Die aktuelle Fassung stammt vom Projektteam.

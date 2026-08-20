# Projektplan: image-exolorer (Münz-Erkennungs-App)

## Ziel

Eine Java-Desktop-App, die per Kamera ein Foto einer Münze aufnimmt und (perspektivisch) per KI erkennt, um welche Münze es sich handelt (Wert, Jahr). In der aktuellen Ausbaustufe wird **nur das UI-Modul** gebaut: Kamera öffnen, Bild aufnehmen, "Vergleich starten"-Button, der einen austauschbaren KI-Vergleichsservice aufruft. Die eigentliche KI-Erkennung ist als Platzhalter/Interface angelegt, nicht als echte Implementierung.

## Tech-Stack

| Bereich | Technologie |
|---|---|
| Sprache/Build | Java 24+, Maven |
| UI | JavaFX (org.openjfx:javafx-controls, javafx-fxml) |
| Kamera | Sarxos Webcam Capture API (`com.github.sarxos:webcam-capture`) — plattformunabhängig (Mac/Windows/Linux) |
| KI (später, austauschbar) | Kandidaten: DJL + ONNX-Runtime (lokal), Cloud-Vision-API, oder Claude Vision API |
| Tests | JUnit 5 |

## Architektur

```
peekra.image_exolorer
├── App.java                     # JavaFX Application Entry Point
├── ui/
│   ├── MainView.java            # Hauptfenster: Kamera-Preview, Buttons, Ergebnis-Anzeige
│   └── MainController.java      # UI-Logik, verbindet View mit Kamera & Vergleichsservice
├── camera/
│   └── CameraService.java       # Kapselt Sarxos Webcam: Geräte öffnen, Preview-Stream, Snapshot
└── recognition/
    ├── CoinRecognitionService.java   # Interface: erkenne(BufferedImage) -> CoinResult
    ├── CoinResult.java               # Datenklasse: Wert, Jahr, Confidence
    └── StubCoinRecognitionService.java  # Platzhalter-Implementierung
```

Das `recognition`-Paket ist bewusst als Interface + austauschbare Implementierung angelegt, damit später ein echtes KI-Backend eingesetzt werden kann, ohne das UI anzufassen.

## Umsetzungsschritte

1. `pom.xml`: JavaFX-, Webcam-Capture- und JUnit-5-Dependencies, Java-Version auf 24, `javafx-maven-plugin` für `mvn javafx:run`
2. `CameraService`: Webcam öffnen, Live-Preview-Frames liefern, Snapshot-Funktion, sauberes Schließen
3. `recognition`-Platzhalter: `CoinResult`, `CoinRecognitionService`-Interface, `StubCoinRecognitionService`
4. UI (`MainView`, `MainController`): Kamera-Vorschau, Snapshot-Button, Vergleich-Button, Ergebnisanzeige
5. `App.java` zu JavaFX-`Application` umbauen
6. Tests: `AppTest` auf JUnit 5, Test für `StubCoinRecognitionService`

## Verifikation

- `mvn clean install` und `mvn test` laufen fehlerfrei
- `mvn javafx:run` startet die App, Kamera-Preview erscheint
- "Foto aufnehmen" zeigt Standbild, "Vergleich starten" zeigt Platzhalter-Ergebnis

## Umsetzungsstand (18.08.2026)

Schritte 1-6 sind umgesetzt. Zusaetzlich zum urspruenglichen Plan:

- **Maven Wrapper** (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`) ergaenzt, damit Build und Tests
  ohne systemweite Maven-Installation laufen.
- `CameraService`: Geraeteerkennung vom Konstruktor nach `open()` verschoben. `Webcam.getDefault()`
  scannt die Hardware und blockiert dabei - im Konstruktor aufgerufen haette das den JavaFX-Thread
  beim Start eingefroren. `open()` gehoert auf einen Hintergrund-Thread und meldet Fehler
  ueber `getLastError()` zurueck.
- Bild-Konvertierung AWT -> JavaFX von Hand in `MainController` statt ueber `SwingFXUtils`,
  um die Abhaengigkeit `javafx-swing` und den Swing-Stack zu vermeiden.

Verifiziert: `./mvnw clean install` gruen, 5/5 Tests bestanden, `./mvnw javafx:run` startet
die Anwendung stabil.

## Bekannte Einschraenkung: Kamera auf Apple Silicon

Die Annahme im Tech-Stack, Sarxos webcam-capture sei plattformunabhaengig, trifft fuer
Apple Silicon **nicht** zu. Version 0.3.12 (letzte Veroeffentlichung, 2020) liefert ueber
BridJ nur x86_64-Natives aus:

```
UnsatisfiedLinkError: libbridj.dylib: mach-o file, but is an
incompatible architecture (have 'x86_64', need 'arm64')
```

Die Anwendung faengt das ab und laeuft weiter mit dem Hinweis "Keine Kamera verfuegbar",
statt abzustuerzen. Die Live-Vorschau bleibt auf arm64-Macs damit leer.

Bewusste Entscheidung: Das Kamera-Backend wird **zurueckgestellt** und gemeinsam mit der
echten KI-Anbindung geloest - dann steht ohnehin fest, welches Bildformat gebraucht wird.
Kandidat fuer den Austausch ist JavaCV/OpenCV (`org.bytedeco`), das native macOS-arm64-
Bibliotheken mitbringt. Der Austausch betrifft nur `CameraService`; UI und Controller
bleiben unveraendert.

## Nachtrag: Startbarkeit ("Code ist nicht ausfuehrbar")

Der Maven-Build war immer fehlerfrei - das Problem lag ausschliesslich im Starten.
Drei unabhaengige Ursachen, alle behoben bzw. dokumentiert:

**1. Eclipse stand auf Java 1.8.** `.classpath` band `JavaSE-1.8`, die JDT-Einstellungen
sagten `compliance=1.8`. Beide wurden aus dem urspruenglichen `pom.xml` erzeugt, das keine
Compiler-Angabe hatte - m2e nimmt dann 1.8. `CoinResult` ist ein `record`, den gibt es erst
ab Java 16, daher meldete Eclipse `records are not supported in -source 8`.
*Behebung:* In Eclipse **Rechtsklick auf das Projekt > Maven > Update Project (Alt+F5)**.
m2e erzeugt beide Dateien aus dem `pom.xml` neu, das jetzt `maven.compiler.release=24` setzt.
Die Dateien stehen in `.gitignore` und werden bewusst nicht eingecheckt.

**2. `java -jar` scheiterte an "no main manifest attribute".** Das JAR-Manifest hatte keine
`Main-Class`. *Behebung:* `maven-jar-plugin` setzt jetzt `Main-Class` und `Class-Path`,
`maven-dependency-plugin` legt die Abhaengigkeiten nach `target/libs`.

**3. Direktstart scheiterte an "JavaFX runtime components are missing".** Der Java-Launcher
verweigert den Start einer Klasse, die von `Application` erbt, wenn JavaFX nicht als benanntes
Modul geladen ist. *Behebung:* `Launcher.java` erbt bewusst nicht von `Application` und reicht
nur an `App` weiter. Damit funktionieren "Run As - Java Application" in Eclipse und die JAR.

### Die drei Startwege

| Weg | Befehl | JavaFX liegt auf |
|---|---|---|
| Maven (empfohlen zum Entwickeln) | `./mvnw javafx:run` | Modulpfad |
| Ausfuehrbare JAR | `./mvnw clean package` dann `java -jar target/image-exolorer-0.0.1-SNAPSHOT.jar` | Klassenpfad |
| Eclipse | Rechtsklick `Launcher.java` > Run As > Java Application | Klassenpfad |

Beim Start ueber den Klassenpfad warnt JavaFX mit "Unsupported JavaFX configuration:
classes were loaded from unnamed module". Das ist unkritisch und betrifft nur den Modulstatus.

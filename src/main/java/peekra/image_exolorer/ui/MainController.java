package peekra.image_exolorer.ui;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import peekra.image_exolorer.camera.CameraService;
import peekra.image_exolorer.recognition.CoinRecognitionService;
import peekra.image_exolorer.recognition.CoinResult;

/**
 * Verbindet die {@link MainView} mit Kamera und Erkennungsdienst.
 *
 * <p>Threading-Regel in dieser Klasse: alles, was blockieren kann (Kamera oeffnen,
 * Frames abholen, Erkennung ausfuehren), laeuft auf Hintergrund-Threads; jede Aenderung
 * am Szenengraphen wird ueber {@link Platform#runLater} auf den JavaFX Application
 * Thread zurueckgereicht.
 */
public class MainController {

    /** Ziel-Bildrate der Live-Vorschau. */
    private static final long FRAME_PAUSE_MS = 33;

    private final MainView view;
    private final CameraService camera;
    private final CoinRecognitionService recognition;

    private volatile boolean laeuft;
    private Thread vorschauThread;

    /** Letztes von der Kamera geliefertes Bild - Quelle fuer den Schnappschuss. */
    private volatile BufferedImage letzterFrame;

    /** Das Foto, das der Nutzer aufgenommen hat und das verglichen werden soll. */
    private volatile BufferedImage aufgenommenesFoto;

    /** Verhindert, dass sich unbearbeitete runLater-Aufrufe stauen. */
    private final AtomicBoolean vorschauUpdateOffen = new AtomicBoolean(false);

    public MainController(MainView view, CameraService camera, CoinRecognitionService recognition) {
        this.view = view;
        this.camera = camera;
        this.recognition = recognition;

        view.getFotoButton().setOnAction(e -> fotoAufnehmen());
        view.getVergleichButton().setOnAction(e -> vergleichStarten());
        view.getVergleichButton().setDisable(true);
        view.getFotoButton().setDisable(true);
    }

    /** Oeffnet die Kamera im Hintergrund und startet danach die Live-Vorschau. */
    public void start() {
        view.getStatusLabel().setText("Kamera wird geoeffnet ...");

        Thread starter = new Thread(() -> {
            boolean offen = camera.open();
            Platform.runLater(() -> {
                if (offen) {
                    view.getLivePlatzhalter().setVisible(false);
                    view.getFotoButton().setDisable(false);
                    view.getStatusLabel().setText("Kamera aktiv: " + camera.getDeviceName());
                    vorschauStarten();
                } else {
                    view.getLivePlatzhalter().setText("Keine Kamera verfuegbar");
                    view.getStatusLabel().setText("Kamera nicht verfuegbar - " + camera.getLastError());
                }
            });
        }, "kamera-start");
        starter.setDaemon(true);
        starter.start();
    }

    private void vorschauStarten() {
        laeuft = true;
        vorschauThread = new Thread(this::vorschauSchleife, "kamera-vorschau");
        vorschauThread.setDaemon(true);
        vorschauThread.start();
    }

    private void vorschauSchleife() {
        while (laeuft) {
            BufferedImage frame = camera.getCurrentImage();
            if (frame != null) {
                letzterFrame = frame;
                // Nur ein Update gleichzeitig einreihen: bei langsamem UI-Thread wuerden
                // sich sonst Frames in der runLater-Queue stapeln.
                if (vorschauUpdateOffen.compareAndSet(false, true)) {
                    Image fxBild = alsFxBild(frame);
                    Platform.runLater(() -> {
                        view.getLiveBild().setImage(fxBild);
                        vorschauUpdateOffen.set(false);
                    });
                }
            }
            try {
                Thread.sleep(FRAME_PAUSE_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void fotoAufnehmen() {
        BufferedImage frame = letzterFrame;
        if (frame == null) {
            view.getStatusLabel().setText("Noch kein Kamerabild vorhanden.");
            return;
        }
        aufgenommenesFoto = frame;
        view.getFotoBild().setImage(alsFxBild(frame));
        view.getFotoPlatzhalter().setVisible(false);
        view.getVergleichButton().setDisable(false);
        view.getErgebnisLabel().setText("Foto aufgenommen. Bereit fuer den Vergleich.");
        view.getStatusLabel().setText("Foto aufgenommen (" + frame.getWidth() + "x" + frame.getHeight() + ").");
    }

    private void vergleichStarten() {
        BufferedImage foto = aufgenommenesFoto;
        if (foto == null) {
            view.getStatusLabel().setText("Bitte zuerst ein Foto aufnehmen.");
            return;
        }

        view.getVergleichButton().setDisable(true);
        view.getFotoButton().setDisable(true);
        view.getErgebnisLabel().setText("Vergleich laeuft ...");

        Thread arbeiter = new Thread(() -> {
            CoinResult ergebnis;
            try {
                ergebnis = recognition.recognize(foto);
            } catch (Exception ex) {
                ergebnis = new CoinResult(null, null, 0.0, "Fehler bei der Erkennung: " + ex.getMessage());
            }
            CoinResult anzeige = ergebnis;
            Platform.runLater(() -> {
                view.getErgebnisLabel().setText(formatiere(anzeige));
                view.getVergleichButton().setDisable(false);
                view.getFotoButton().setDisable(!camera.isOpen());
                view.getStatusLabel().setText("Vergleich abgeschlossen.");
            });
        }, "muenz-erkennung");
        arbeiter.setDaemon(true);
        arbeiter.start();
    }

    private static String formatiere(CoinResult ergebnis) {
        if (ergebnis == null) {
            return "Kein Ergebnis erhalten.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Wert: ").append(ergebnis.value() == null ? "unbekannt" : ergebnis.value());
        sb.append("\nJahr: ").append(ergebnis.year() == null ? "unbekannt" : ergebnis.year());
        sb.append(String.format("%nKonfidenz: %.0f %%", ergebnis.confidence() * 100));
        if (ergebnis.message() != null && !ergebnis.message().isBlank()) {
            sb.append("\n").append(ergebnis.message());
        }
        return sb.toString();
    }

    /**
     * Wandelt ein AWT-Bild in ein JavaFX-Bild um.
     *
     * <p>Bewusst von Hand statt ueber {@code SwingFXUtils}: das haette die zusaetzliche
     * Abhaengigkeit javafx-swing noetig gemacht und zieht den Swing-Stack mit hoch.
     * {@link WritableImage} darf ausserhalb des FX-Threads erzeugt werden, solange es
     * noch nicht an einem Node haengt - deshalb ist der Aufruf aus der Vorschauschleife
     * heraus zulaessig.
     */
    private static Image alsFxBild(BufferedImage quelle) {
        int breite = quelle.getWidth();
        int hoehe = quelle.getHeight();
        int[] pixel = quelle.getRGB(0, 0, breite, hoehe, null, 0, breite);
        WritableImage ziel = new WritableImage(breite, hoehe);
        ziel.getPixelWriter().setPixels(0, 0, breite, hoehe,
                PixelFormat.getIntArgbInstance(), pixel, 0, breite);
        return ziel;
    }

    /** Stoppt die Vorschau und schliesst die Kamera. Wird beim Fensterschliessen aufgerufen. */
    public void shutdown() {
        laeuft = false;
        if (vorschauThread != null) {
            vorschauThread.interrupt();
            try {
                vorschauThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        camera.close();
    }
}

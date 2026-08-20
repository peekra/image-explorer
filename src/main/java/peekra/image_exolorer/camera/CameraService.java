package peekra.image_exolorer.camera;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;

/**
 * Kapselt den Zugriff auf die System-Webcam ueber die Sarxos Webcam Capture API.
 *
 * <p>Die Geraeteerkennung erfolgt bewusst erst in {@link #open()} und nicht im Konstruktor:
 * {@code Webcam.getDefault()} scannt die angeschlossene Hardware, blockiert dabei und kann
 * fehlschlagen. Im Konstruktor aufgerufen wuerde das den JavaFX-Thread beim Start blockieren.
 * {@link #open()} gehoert daher auf einen Hintergrund-Thread.
 */
public class CameraService {

    private volatile Webcam webcam;
    private volatile String lastError;

    /**
     * Sucht die Standard-Kamera, oeffnet sie und meldet den Erfolg zurueck.
     * Blockierend - nicht auf dem JavaFX Application Thread aufrufen.
     *
     * @return true, wenn die Kamera danach einsatzbereit ist
     */
    public synchronized boolean open() {
        if (isOpen()) {
            return true;
        }
        try {
            if (webcam == null) {
                webcam = Webcam.getDefault();
            }
            if (webcam == null) {
                lastError = "Keine Kamera gefunden.";
                return false;
            }
            if (!webcam.isOpen()) {
                webcam.open();
            }
            lastError = null;
            return true;
        } catch (Throwable t) {
            // Bewusst Throwable: die nativen Treiber koennen UnsatisfiedLinkError bzw.
            // NoClassDefFoundError werfen, nicht nur regulaere Exceptions.
            lastError = beschreibe(t);
            webcam = null;
            return false;
        }
    }

    /** Schliesst die Kamera, falls sie geoeffnet ist. Mehrfachaufruf ist unschaedlich. */
    public synchronized void close() {
        try {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        } catch (Throwable t) {
            lastError = beschreibe(t);
        }
    }

    public boolean isOpen() {
        Webcam cam = webcam;
        return cam != null && cam.isOpen();
    }

    /** Name des erkannten Geraets, oder null solange keine Kamera geoeffnet wurde. */
    public String getDeviceName() {
        Webcam cam = webcam;
        return cam == null ? null : cam.getName();
    }

    /** Aufloesung der geoeffneten Kamera, oder null falls keine Kamera offen ist. */
    public Dimension getResolution() {
        Webcam cam = webcam;
        return cam == null ? null : cam.getViewSize();
    }

    /** Grund des letzten Fehlschlags, oder null wenn zuletzt alles geklappt hat. */
    public String getLastError() {
        return lastError;
    }

    /** Liefert das aktuelle Kamerabild, oder null falls keine Kamera verfuegbar/geoeffnet ist. */
    public BufferedImage getCurrentImage() {
        Webcam cam = webcam;
        if (cam == null || !cam.isOpen()) {
            return null;
        }
        try {
            return cam.getImage();
        } catch (Throwable t) {
            lastError = beschreibe(t);
            return null;
        }
    }

    /** Nimmt ein Standbild auf (identisch zu getCurrentImage, semantisch als Snapshot gedacht). */
    public BufferedImage snapshot() {
        return getCurrentImage();
    }

    private static String beschreibe(Throwable t) {
        String msg = t.getMessage();
        return msg == null || msg.isBlank()
                ? t.getClass().getSimpleName()
                : t.getClass().getSimpleName() + ": " + msg;
    }
}

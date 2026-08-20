package peekra.image_exolorer.recognition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Sichert das Verhalten der Platzhalter-Implementierung ab. Diese Tests beschreiben
 * zugleich den Vertrag, den eine spaetere echte KI-Implementierung erfuellen muss:
 * niemals null zurueckgeben, auch nicht bei fehlendem Bild.
 */
class StubCoinRecognitionServiceTest {

    private CoinRecognitionService service;

    @BeforeEach
    void setUp() {
        service = new StubCoinRecognitionService();
    }

    @Test
    @DisplayName("Liefert bei fehlendem Bild ein Ergebnis mit Hinweis statt null")
    void ohneBildKommtHinweis() {
        CoinResult ergebnis = service.recognize(null);

        assertNotNull(ergebnis, "Auch ohne Bild muss ein Ergebnis geliefert werden");
        assertEquals("Kein Bild vorhanden.", ergebnis.message());
        assertEquals(0.0, ergebnis.confidence());
        assertNull(ergebnis.value());
        assertNull(ergebnis.year());
    }

    @Test
    @DisplayName("Liefert mit Bild ein Ergebnis ohne Erkennung, aber mit Statusmeldung")
    void mitBildKommtPlatzhalterErgebnis() {
        BufferedImage bild = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);

        CoinResult ergebnis = service.recognize(bild);

        assertNotNull(ergebnis);
        assertEquals("Erkennung noch nicht implementiert.", ergebnis.message());
        assertEquals(0.0, ergebnis.confidence());
        assertNull(ergebnis.value(), "Der Stub darf keinen Wert erfinden");
        assertNull(ergebnis.year(), "Der Stub darf kein Jahr erfinden");
    }

    @Test
    @DisplayName("CoinResult haelt uebergebene Werte unveraendert")
    void coinResultHaeltWerte() {
        CoinResult ergebnis = new CoinResult("2 Euro", 2002, 0.87, "Testtreffer");

        assertEquals("2 Euro", ergebnis.value());
        assertEquals(2002, ergebnis.year());
        assertEquals(0.87, ergebnis.confidence());
        assertEquals("Testtreffer", ergebnis.message());
    }
}

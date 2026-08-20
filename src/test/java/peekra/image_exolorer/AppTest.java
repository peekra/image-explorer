package peekra.image_exolorer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.application.Application;

/**
 * Smoke-Test fuer den Einstiegspunkt.
 *
 * <p>Die JavaFX-Oberflaeche selbst wird hier nicht gestartet: das braeuchte einen
 * laufenden FX-Toolkit und ein Display, was auf einem Build-Server nicht gegeben ist.
 * Geprueft wird daher nur, dass App korrekt als JavaFX-Application aufgesetzt ist.
 */
class AppTest {

    @Test
    @DisplayName("App ist eine JavaFX-Application")
    void appIstJavaFxApplication() {
        assertTrue(Application.class.isAssignableFrom(App.class),
                "App muss von javafx.application.Application erben");
    }

    @Test
    @DisplayName("App besitzt eine main-Methode als Einstiegspunkt")
    void appHatMainMethode() throws NoSuchMethodException {
        assertNotNull(App.class.getMethod("main", String[].class));
    }
}

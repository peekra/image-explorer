package peekra.image_exolorer;

/**
 * Startklasse fuer den Betrieb ueber den Klassenpfad.
 *
 * <p>Der Java-Launcher weigert sich, eine Klasse direkt zu starten, die von
 * {@link javafx.application.Application} erbt, solange JavaFX nicht als benanntes
 * Modul geladen ist - der Fehler lautet dann "JavaFX runtime components are missing".
 * Diese Klasse erbt bewusst nicht davon und ist daher fuer den Launcher unverdaechtig;
 * sie reicht lediglich an {@link App} weiter. Das ist der uebliche Weg, damit
 * "Run As - Java Application" in der IDE und eine ausfuehrbare JAR funktionieren.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}

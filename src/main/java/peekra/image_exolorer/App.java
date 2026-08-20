package peekra.image_exolorer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import peekra.image_exolorer.camera.CameraService;
import peekra.image_exolorer.recognition.CoinRecognitionService;
import peekra.image_exolorer.recognition.StubCoinRecognitionService;
import peekra.image_exolorer.ui.MainController;
import peekra.image_exolorer.ui.MainView;

/**
 * Einstiegspunkt der Anwendung.
 *
 * <p>Hier wird auch entschieden, welche Erkennungs-Implementierung verwendet wird.
 * Um spaeter ein echtes KI-Backend anzubinden, wird nur diese eine Zeile getauscht -
 * das UI bleibt unveraendert.
 */
public class App extends Application {

    private MainController controller;

    @Override
    public void start(Stage stage) {
        CameraService camera = new CameraService();
        CoinRecognitionService recognition = new StubCoinRecognitionService();

        MainView view = new MainView();
        controller = new MainController(view, camera, recognition);

        stage.setTitle("image-exolorer - Muenz-Erkennung");
        stage.setScene(new Scene(view.getRoot(), 960, 720));
        stage.show();

        controller.start();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

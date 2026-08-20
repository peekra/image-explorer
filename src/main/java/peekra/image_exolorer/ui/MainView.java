package peekra.image_exolorer.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Reine Sicht: baut den Szenengraphen auf und stellt die Bedienelemente bereit.
 * Enthaelt bewusst keine Logik - die liegt im {@link MainController}.
 */
public class MainView {

    private static final String KARTE =
            "-fx-background-color: #ffffff;"
            + "-fx-background-radius: 10;"
            + "-fx-border-color: #d8dce3;"
            + "-fx-border-radius: 10;";

    private final BorderPane root = new BorderPane();

    private final ImageView liveBild = new ImageView();
    private final ImageView fotoBild = new ImageView();
    private final Label livePlatzhalter = new Label("Kamera wird gestartet ...");
    private final Label fotoPlatzhalter = new Label("Noch kein Foto aufgenommen");

    private final Button fotoButton = new Button("Foto aufnehmen");
    private final Button vergleichButton = new Button("Vergleich starten");

    private final Label statusLabel = new Label("Bereit.");
    private final Label ergebnisLabel = new Label("Noch kein Vergleich gestartet.");

    public MainView() {
        root.setStyle("-fx-background-color: #eef1f5;");
        root.setTop(baueKopf());
        root.setCenter(baueBildbereich());
        root.setBottom(baueFuss());
    }

    private Region baueKopf() {
        Label titel = new Label("Muenz-Erkennung");
        titel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label untertitel = new Label("Foto der Muenze aufnehmen und per KI-Dienst vergleichen");
        untertitel.setStyle("-fx-text-fill: #5a6472;");

        VBox kopf = new VBox(2, titel, untertitel);
        kopf.setPadding(new Insets(16, 20, 16, 20));
        return kopf;
    }

    private Region baueBildbereich() {
        HBox bereich = new HBox(16, baueBildKarte("Live-Vorschau", liveBild, livePlatzhalter),
                baueBildKarte("Aufgenommenes Foto", fotoBild, fotoPlatzhalter));
        bereich.setPadding(new Insets(0, 20, 0, 20));
        return bereich;
    }

    private Region baueBildKarte(String titel, ImageView bild, Label platzhalter) {
        bild.setPreserveRatio(true);
        bild.setFitWidth(420);
        bild.setFitHeight(320);

        platzhalter.setStyle("-fx-text-fill: #8b95a3;");

        StackPane buehne = new StackPane(platzhalter, bild);
        buehne.setMinSize(420, 320);
        buehne.setPrefSize(420, 320);
        buehne.setStyle("-fx-background-color: #20242b; -fx-background-radius: 8;");

        Label kopf = new Label(titel);
        kopf.setFont(Font.font("System", FontWeight.BOLD, 13));

        VBox karte = new VBox(8, kopf, buehne);
        karte.setPadding(new Insets(12));
        karte.setStyle(KARTE);
        HBox.setHgrow(karte, Priority.ALWAYS);
        return karte;
    }

    private Region baueFuss() {
        fotoButton.setStyle("-fx-background-color: #2f6fed; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 6;");
        fotoButton.setPrefHeight(38);
        fotoButton.setPrefWidth(180);

        vergleichButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 6;");
        vergleichButton.setPrefHeight(38);
        vergleichButton.setPrefWidth(180);

        HBox knoepfe = new HBox(12, fotoButton, vergleichButton);
        knoepfe.setAlignment(Pos.CENTER_LEFT);

        Label ergebnisKopf = new Label("Ergebnis");
        ergebnisKopf.setFont(Font.font("System", FontWeight.BOLD, 13));

        ergebnisLabel.setWrapText(true);
        ergebnisLabel.setStyle("-fx-text-fill: #333a45;");

        VBox ergebnisKarte = new VBox(6, ergebnisKopf, ergebnisLabel);
        ergebnisKarte.setPadding(new Insets(12));
        ergebnisKarte.setStyle(KARTE);
        ergebnisKarte.setMinHeight(90);

        statusLabel.setStyle("-fx-text-fill: #5a6472; -fx-font-size: 11px;");

        VBox fuss = new VBox(12, knoepfe, ergebnisKarte, statusLabel);
        fuss.setPadding(new Insets(16, 20, 16, 20));
        return fuss;
    }

    public BorderPane getRoot() {
        return root;
    }

    public ImageView getLiveBild() {
        return liveBild;
    }

    public ImageView getFotoBild() {
        return fotoBild;
    }

    public Label getLivePlatzhalter() {
        return livePlatzhalter;
    }

    public Label getFotoPlatzhalter() {
        return fotoPlatzhalter;
    }

    public Button getFotoButton() {
        return fotoButton;
    }

    public Button getVergleichButton() {
        return vergleichButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Label getErgebnisLabel() {
        return ergebnisLabel;
    }
}

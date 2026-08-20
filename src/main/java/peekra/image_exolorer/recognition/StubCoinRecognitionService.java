package peekra.image_exolorer.recognition;

import java.awt.image.BufferedImage;

/**
 * Platzhalter-Implementierung: führt noch keine echte Erkennung durch,
 * verdrahtet aber die UI funktional gegen die zukünftige KI-Komponente.
 */
public class StubCoinRecognitionService implements CoinRecognitionService {

    @Override
    public CoinResult recognize(BufferedImage image) {
        if (image == null) {
            return new CoinResult(null, null, 0.0, "Kein Bild vorhanden.");
        }
        return new CoinResult(null, null, 0.0, "Erkennung noch nicht implementiert.");
    }
}

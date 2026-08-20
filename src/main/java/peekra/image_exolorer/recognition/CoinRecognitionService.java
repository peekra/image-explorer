package peekra.image_exolorer.recognition;

import java.awt.image.BufferedImage;

/**
 * Austauschbare Schnittstelle für die KI-gestützte Münzerkennung.
 * Konkrete Implementierungen (z.B. lokal via ONNX/DJL oder über eine Cloud-API)
 * können ausgetauscht werden, ohne das UI anzupassen.
 */
public interface CoinRecognitionService {

    CoinResult recognize(BufferedImage image);
}

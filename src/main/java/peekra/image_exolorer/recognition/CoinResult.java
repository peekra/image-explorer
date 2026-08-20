package peekra.image_exolorer.recognition;

/**
 * Ergebnis eines Münz-Erkennungsvorgangs.
 *
 * @param value      erkannter Münzwert (z.B. "1 Euro"), oder null falls unbekannt
 * @param year       erkanntes Prägejahr, oder null falls unbekannt
 * @param confidence Konfidenz der Erkennung zwischen 0.0 und 1.0
 * @param message    zusätzliche Information, z.B. Hinweis auf fehlende Implementierung
 */
public record CoinResult(String value, Integer year, double confidence, String message) {
}

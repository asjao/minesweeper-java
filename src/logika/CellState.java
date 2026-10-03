package logika;

/**
 * Stanje jednog polja na ploči Minesweeper-a.
 * UI sloj koristi ovo stanje za prikaz i za blokiranje nevalidnih poteza
 * (npr. FLAG polje se ne smije otvoriti).
 */
public enum CellState {
	CLOSED, OPEN, FLAG
}

package konzola;

import java.util.Scanner;

import logika.CellState;
import logika.GameState;
import logika.MinesweeperGame;

/**
 * Konzolna verzija Minesweeper igre.
 * Ova klasa služi kao UI sloj: prikazuje stanje igre u terminalu i prima korisničke komande.
 * Pravila igre su implementirana u {@link logika.MinesweeperGame}.
 *
 * <p>Komande:</p>
 * <ul>
 *   <li><b>o r c</b> - otvori polje (open)</li>
 *   <li><b>f r c</b> - postavi/ukloni zastavicu (flag)</li>
 *   <li><b>c r c</b> - chord (otvori susjedna polja ako broj flagova odgovara broju)</li>
 *   <li><b>r</b> - restart / nova igra</li>
 *   <li><b>q</b> - izlaz</li>
 * </ul>
 *
 * <p>Napomena: indeksi r i c su 0-based (0..R-1, 0..C-1).</p>
 */
public class ConsoleApp {
	/**
     * Ulazna tačka konzolne aplikacije.
     * Kreira igru prema unosu korisnika i omogućava igranje kroz tekstualne komande.
     *
     * @param args argumenti komandne linije (ne koriste se)
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        MinesweeperGame game = createGameFromInput(sc);

        while (true) {
            printBoard(game);
            
            System.out.println("Preostale mine: " + game.getRemainingMines());
            if(game.getGameState() == GameState.RUNNING)
            	System.out.println("Komande: o r c | f r c | c r c | r (restart) | q (izadji)");

            if (game.getGameState() == GameState.WIN)
                System.out.println("POBJEDA! :) Unesi 'r' za novu igru ili 'q' za izlaz.");
            else if (game.getGameState() == GameState.LOSE)
                System.out.println("PORAZ! :( Unesi 'r' za novu igru ili 'q' za izlaz.");
            
         //Ako je igra zavrsena, ne ispisuj ponovo plocu - samo trazi r ili q
            if (game.getGameState() != GameState.RUNNING) {
                while (true) {
                    System.out.print("Unos (r ili q): ");
                    String end = sc.nextLine().trim();
                    if (end.equalsIgnoreCase("q")) { 
                    	sc.close();
                    	return;
                    }
                    if (end.equalsIgnoreCase("r")) { 
                    	game = createGameFromInput(sc); 
                    	break; 
                    }
                    System.out.println("Igra je već završena! Unesi samo 'r' (nova igra) ili 'q' (izlaz).");
                }
                continue;
            }

            System.out.print("Unos: ");
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("q")) break;

            if (line.equalsIgnoreCase("r")) {
                game = createGameFromInput(sc);
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Pogrešan format. Pokušaj ponovo");
                continue;
            }

            String command = parts[0].toLowerCase();
            int r, c;
            try {
                r = Integer.parseInt(parts[1]);
                c = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.out.println("r i c moraju biti brojevi!");
                continue;
            }

            if (command.equals("o")) game.openCell(r, c);
            else if (command.equals("f")) game.toggleFlag(r, c);
            else if (command.equals("c")) game.chord(r, c);
            else System.out.println("Nepoznata komanda.");
        }
        sc.close();
    }

    /**
     * Traži od korisnika da unese parametre ploče (R, C, B) i kreira novu igru.
     * Ponovlja unos dok korisnik ne unese validne vrijednosti.
     *
     * @param sc scanner za čitanje unosa iz konzole
     * @return nova instanca {@link MinesweeperGame} sa unesenim parametrima
     */
    private static MinesweeperGame createGameFromInput(Scanner sc) {
        while (true) {
            try {
                System.out.print("Unesi R(broj redova) C(broj kolona) B(broj mina), npr 9 9 10: ");
                String[] p = sc.nextLine().trim().split("\\s+");
                if (p.length != 3) throw new IllegalArgumentException("Format je: R C B");
                int R = Integer.parseInt(p[0]);
                int C = Integer.parseInt(p[1]);
                int B = Integer.parseInt(p[2]);
                return new MinesweeperGame(R, C, B);
            } catch (Exception e) {
                System.out.println("Greška: " + e.getMessage());
            }
        }
        
    }

    /**
     * Ispisuje trenutno stanje ploče u terminalu koristeći informacije iz modela.
     * Zatvoreno polje se prikazuje kao "_", flag kao "F", otvoreno prazno kao ".", a mine kao "*"
     * (mine se prikazuju tek nakon završetka igre).
     *
     * @param game instanca igre čije se stanje ispisuje
     */
    private static void printBoard(MinesweeperGame game) {
        int R, C;
        R = game.getRows();
        C = game.getCols();

        System.out.print("   ");
        for (int c = 0; c < C; c++) System.out.printf("%2d ", c);
        System.out.println();

        for (int r = 0; r < R; r++) {
            System.out.printf("%2d ", r);
            for (int c = 0; c < C; c++) {
                CellState st = game.getCellState(r, c);
                if (game.getGameState() != GameState.RUNNING && game.hasMineForReveal(r, c)) {
                    System.out.printf("%2s ", "*");
                    continue;
                }
                
                String s = "_";
                if (st == CellState.FLAG)
                	s = "F";
                else if (st == CellState.OPEN) {
                    if (game.hasMineForReveal(r, c))
                    	s = "*";
                    else {
                        int n = game.getNeighborCount(r, c);
                        if (n == 0)
                        	s = ".";
                        else
                        	s = Integer.toString(n);
                    }
                }
                System.out.printf("%2s ", s);
            }
            System.out.println();
            
        }
    }
}

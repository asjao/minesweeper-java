package logika;
import java.util.Random;

/**
 * Predstavlja "fizičku" ploču Minesweeper-a: raspored mina i broj susjednih mina.
 * Ova klasa ne čuva stanje polja (OPEN/CLOSED/FLAG), već samo mine i susjedne brojeve.
 * Stanje polja čuva {@link MinesweeperGame}.
 */
public class Board {
    private final int rows;
    private final int cols;
    private boolean[][] mines;
    private int[][] neighborCount;   //Broj susjednih mina

    /**
     * Kreira praznu ploču dimenzija rows x cols.
     * @param rows broj redova
     * @param cols broj kolona
     */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.mines = new boolean[rows][cols];
        this.neighborCount = new int[rows][cols];
    }

    /** @return broj redova ploče */
    public int getRows() { return rows; }
    /** @return broj kolona ploče */
    public int getCols() { return cols; }

    /**
     * Provjerava da li se na polju nalazi mina.
     * @param r red
     * @param c kolona
     * @return true ako je polje mina
     */
    public boolean hasMine(int r, int c) {
        return mines[r][c];
    }

    /**
     * Vraća broj susjednih mina (0-8) za dato polje.
     * @param r red
     * @param c kolona
     * @return broj susjednih mina
     */
    public int getNeighborCount(int r, int c) {
        return neighborCount[r][c];
    }

    /**
     * Briše ploču: uklanja sve mine i resetuje brojeve susjednih mina.
     */
    public void clear() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                mines[r][c] = false;
                neighborCount[r][c] = 0;
            }
        }
    }

    //Generise mine, ali garantuje da polje (safeR, safeC) nije mina.
    /**
     * Generiše mine na slučajan način i garantuje da (safeR, safeC) nije mina
     * (pravilo: prvi klik mora biti siguran).
     * Nakon postavljanja mina računa se broj susjednih mina za svako polje.
     *
     * @param mineCount broj mina koje treba postaviti
     * @param safeR red sigurnog polja (prvi klik)
     * @param safeC kolona sigurnog polja (prvi klik)
     */
    public void generateMines(int mineCount, int safeR, int safeC) {
        clear();
        Random rnd = new Random();

        int placed = 0;
        while (placed < mineCount) {
            int r = rnd.nextInt(rows);
            int c = rnd.nextInt(cols);

            if (r == safeR && c == safeC) continue;
            if (mines[r][c]) continue;

            mines[r][c] = true;
            placed++;
        }

        //Racunanje broja susjednih mina
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                neighborCount[r][c] = countAdjacentMines(r, c);
            }
        }
    }

    private int countAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr, nc;
                nr = r + dr;
                nc = c + dc;
                if (inBounds(nr, nc) && mines[nr][nc]) 
                	count++;
            }
        }
        return count;
    }

    /**
     * Provjerava da li su koordinate unutar granica ploče.
     * @param r red
     * @param c kolona
     * @return true ako je r u opsegu 0..rows-1 i c u opsegu 0..cols-1
     */
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
}

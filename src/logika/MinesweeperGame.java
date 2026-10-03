package logika;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Model Minesweeper igre (logika).
 * Sadrži stanje partije i implementira pravila: otvaranje polja, flood fill,
 * postavljanje zastavica, chord, te provjeru pobjede/poraza.
 *
 * UI sloj (konzola i GUI) smije koristiti samo javne metode ove klase i gettere.
 */
public class MinesweeperGame {
    private int rows, cols, minesCount;
    private Board board;
    private CellState[][] state;

    private GameState gameState;
    private boolean minesGenerated;
    private int flagsPlaced;
    private int openedSafe;     

    /**
     * Kreira novu igru sa zadanim parametrima.
     * @param rows broj redova
     * @param cols broj kolona
     * @param minesCount broj mina
     * @throws IllegalArgumentException ako parametri nisu validni
     */
    public MinesweeperGame(int rows, int cols, int minesCount) {
        reset(rows, cols, minesCount);
    }

    /**
     * Resetuje igru i postavlja nove parametre.
     * @param rows broj redova
     * @param cols broj kolona
     * @param minesCount broj mina
     * @throws IllegalArgumentException ako parametri nisu validni
     */
    public void reset(int rows, int cols, int minesCount) {
        validate(rows, cols, minesCount);

        this.rows = rows;
        this.cols = cols;
        this.minesCount = minesCount;

        this.board = new Board(rows, cols);
        this.state = new CellState[rows][cols];

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                state[r][c] = CellState.CLOSED;

        this.gameState = GameState.RUNNING;
        this.minesGenerated = false;
        this.flagsPlaced = 0;
        this.openedSafe = 0;
    }

    private void validate(int R, int C, int B) {
        if (R < 5 || R > 50) 
        	throw new IllegalArgumentException("R mora biti u opsegu 5..50");
        if (C < 5 || C > 50)
        	throw new IllegalArgumentException("C mora biti u opsegu 5..50");

        int minB = Math.min(R, C);
        int maxB = (R * C * 8) / 10;

        
        if (B < minB) throw new IllegalArgumentException("B mora biti >= min(R,C) = " + minB);
        if (B > maxB) throw new IllegalArgumentException("B mora biti <= 80% polja = " + maxB);
    }

    /** @return trenutno stanje igre (RUNNING/WIN/LOSE) */
    public GameState getGameState() { return gameState; }
    /** @return broj redova */
    public int getRows() { return rows; }
    /** @return broj kolona */
    public int getCols() { return cols; }
    /** @return ukupan broj mina u igri */
    public int getMinesCount() { return minesCount; }
    /** @return broj postavljenih zastavica */
    public int getFlagsPlaced() { return flagsPlaced; }
    /**
     * Brojač preostalih mina (mineCount - flagsPlaced).
     * @return procjena preostalih mina prema broju flagova
     */
    public int getRemainingMines() { return minesCount - flagsPlaced; }

    
    /**
     * Vraća stanje polja (CLOSED/OPEN/FLAG).
     * @param r red
     * @param c kolona
     * @return stanje polja
     */
    public CellState getCellState(int r, int c) {
        return state[r][c];
    }

    /**
     * Vraća broj susjednih mina za dato polje (0-8).
     * UI koristi ovo za prikaz broja na otvorenom polju.
     * @param r red
     * @param c kolona
     * @return broj susjednih mina
     */
    public int getNeighborCount(int r, int c) {
        return board.getNeighborCount(r, c);
    }

    
    /**
     * Vraća true ako UI smije prikazati minu na datom polju.
     * Mine se prikazuju tek nakon završetka igre (WIN ili LOSE).
     *
     * @param r red
     * @param c kolona
     * @return true ako je igra završena i na polju postoji mina
     */
    public boolean hasMineForReveal(int r, int c) {
        return (gameState != GameState.RUNNING) && board.hasMine(r, c);
    }

    /**
     * Otvara polje (left-click akcija).
     * - Ne dozvoljava otvaranje ako je igra završena ili je polje FLAG/OPEN.
     * - Na prvom otvaranju generiše mine (first click safe).
     * - Ako je mina: LOSE.
     * - Ako je sigurno: otvara polje i po potrebi radi flood fill.
     *
     * @param r red
     * @param c kolona
     * @return true ako je stanje igre promijenjeno/akcija izvršena
     */
    public boolean openCell(int r, int c) {
        if (!board.inBounds(r, c)) return false;
        if (gameState != GameState.RUNNING) return false;
        if (state[r][c] == CellState.OPEN) return false;
        if (state[r][c] == CellState.FLAG) return false;

        //First click safe: generisi mine tek sada.
        if (!minesGenerated) {
            board.generateMines(minesCount, r, c);
            minesGenerated = true;
        }
     
        //Ako je mina na tom polju, poraz.
        if (board.hasMine(r, c)) {
            state[r][c] = CellState.OPEN;
            gameState = GameState.LOSE;
            return true;
        }

        //Otvori polje + eventualno flood fill ako je polje sa 0 okolnih mina.
        revealWithFloodFill(r, c);

        //Provjera pobjede:
        if (openedSafe == rows * cols - minesCount) {
            gameState = GameState.WIN;
        }
        return true;
    }

    private void revealWithFloodFill(int startR, int startC) {   
        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{startR, startC});

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];

            if (!board.inBounds(r, c)) continue;
            if (state[r][c] == CellState.OPEN) continue;
            if (state[r][c] == CellState.FLAG) continue;
            if (board.hasMine(r, c)) continue; //Za svaki slucaj, za sigurnost.

            state[r][c] = CellState.OPEN;
            openedSafe++;

            //Ako je 0, siri na susjede (flood fill):
            if (board.getNeighborCount(r, c) == 0) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue; 
                        int nr, nc;
                        nr = r + dr;
                        nc = c + dc;
                        if (board.inBounds(nr, nc) && state[nr][nc] != CellState.OPEN) {
                            q.add(new int[]{nr, nc}); 
                        }
                    }
                    
                }
            }
        }
    }

    /**
     * Postavlja ili uklanja zastavicu (right-click akcija).
     * Ne dozvoljava flag na OPEN polju i ne radi ništa ako je igra završena.
     *
     * @param r red
     * @param c kolona
     * @return true ako je zastavica promijenjena
     */
    public boolean toggleFlag(int r, int c) {
        if (!board.inBounds(r, c)) return false;
        if (gameState != GameState.RUNNING) return false;
        if (state[r][c] == CellState.OPEN) return false;

        if (state[r][c] == CellState.CLOSED) {
            state[r][c] = CellState.FLAG;
            flagsPlaced++;
            return true;
        } else if (state[r][c] == CellState.FLAG) {
            state[r][c] = CellState.CLOSED;
            flagsPlaced--;
            return true;
        }
        return false;
    }

    /**
     * Chord akcija: ako je polje OPEN i broj zastavica oko njega jednak broju na polju,
     * otvara sva susjedna polja koja nisu OPEN i nisu FLAG.
     *
     * @param r red
     * @param c kolona
     * @return true ako je otvoreno barem jedno novo polje
     */
    public boolean chord(int r, int c) {
        if (!board.inBounds(r, c)) return false;
        if (gameState != GameState.RUNNING) return false;
        if (state[r][c] != CellState.OPEN) return false;

        int number = board.getNeighborCount(r, c);
        if (number <= 0) return false;

        int flagsAround = countFlagsAround(r, c);
        if (flagsAround != number) return false;

        //Otvori sve susjede koji nisu opened i nisu flag:
        boolean changed = false;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr, nc;
                nr = r + dr;
                nc = c + dc;
                if (!board.inBounds(nr, nc)) continue;
                if (state[nr][nc] == CellState.CLOSED) {
                    openCell(nr, nc);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private int countFlagsAround(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr, nc;
                nr = r + dr;
                nc = c + dc;
                if (board.inBounds(nr, nc) && state[nr][nc] == CellState.FLAG) 
                	count++;
            }
        }
        return count;
    }
}

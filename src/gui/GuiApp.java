package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import logika.*;

/**
 * Grafički (Swing) interfejs za Minesweeper igru.
 * Ova klasa predstavlja UI sloj: prikazuje stanje igre i prosljeđuje korisničke akcije
 * modelu {@link logika.MinesweeperGame}. Pravila igre nisu implementirana ovdje.
 *
 * <p>Kontrole:</p>
 * <ul>
 *   <li><b>Lijevi klik</b> - otvori polje</li>
 *   <li><b>Desni klik</b> - postavi/ukloni zastavicu</li>
 *   <li><b>Shift + lijevi klik</b> - chord</li>
 * </ul>
 *
 * <p>GUI sadrži i timer koji mjeri trajanje partije.</p>
 */
public class GuiApp {
    private JFrame frame;
    private JPanel gridPanel;
    private JLabel minesLabel;
    private JLabel logoLabel;
    private ImageIcon logoIcon;
    private ImageIcon flagIcon;
    private ImageIcon bombIcon;
    private JScrollPane scroll;
    private Timer uiTimer;
    private int elapsedSeconds = 0;
    private boolean timerStarted = false;
    private JLabel timerLabel;
    private GameState lastState = GameState.RUNNING;

    private MinesweeperGame game;
    private JButton[][] buttons;

    //paleta boja
    private final Color TOP_BG = new Color(145, 120, 170);
    private final Color GRID_BG = new Color(242, 238, 247);
    private final Color CLOSED_BG = new Color(205, 185, 225); 
    private final Color FLAG_BG = new Color(180, 130, 185); 
    private final Color OPEN0_BG = new Color(238, 232, 245);
    private final Color OPEN_BG = new Color(230, 220, 240);
    private final Color MINE_BG = new Color(190, 110, 140); 
    private final Font BTN_FONT = new Font("Arial", Font.BOLD, 18);
    private final Font TOP_FONT = new Font("Arial", Font.BOLD, 16);

    /**
     * Ulazna tačka GUI aplikacije.
     * Pokreće Swing aplikaciju na Event Dispatch Thread-u.
     *
     * @param args argumenti komandne linije (ne koriste se)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GuiApp().start());
    }

    /**
     * Inicijalizuje glavni prozor, top bar, učitava ikonice, kreira početnu igru
     * i priprema grid dugmadi.
     */
    private void start() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        frame = new JFrame("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        //Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(TOP_BG);
        top.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        top.setPreferredSize(new Dimension(0, 70)); 
        
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(TOP_BG);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(TOP_BG);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(TOP_BG);

        minesLabel = new JLabel();
        minesLabel.setFont(TOP_FONT);
        minesLabel.setForeground(Color.WHITE);

        JButton restartBtn = new JButton("Restart");
        JButton newGameBtn = new JButton("Izaberi parametre");

        styleTopButton(restartBtn);
        styleTopButton(newGameBtn);

        restartBtn.addActionListener(e -> restartSameParams());
        newGameBtn.addActionListener(e -> showDifficultyWindow());

        left.add(Box.createVerticalGlue());
        left.add(minesLabel);
        timerLabel = new JLabel("Vrijeme: 0s");
        timerLabel.setFont(TOP_FONT);
        timerLabel.setForeground(Color.WHITE);
        left.add(timerLabel);
        left.add(Box.createVerticalGlue());
        
        flagIcon = loadIcon("/flag.png", 18, 18);
        bombIcon = loadIcon("/mine.png", 18, 18);
        
        logoLabel = new JLabel();
        logoIcon = loadIcon("/mine.png", 60, -1);
        logoLabel.setIcon(logoIcon);
        center.add(logoLabel);

        right.add(restartBtn);
        right.add(newGameBtn);

        top.add(left, BorderLayout.WEST);
        top.add(center, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        frame.add(top, BorderLayout.NORTH);

        //Defaultna ploca 9x9 sa 10 mina
        game = new MinesweeperGame(9, 9, 10);

        buildGrid();
        initTimer();
        
        frame.setMinimumSize(new Dimension(600, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        refreshUI();
    }
    
    /**
     * Učitava sliku iz resources i skalira je na zadanu veličinu.
     *
     * @param path putanja do resursa (npr. "/flag.png")
     * @param w širina ikone
     * @param h visina ikone (ako je -1, zadržava se proporcija)
     * @return učitana i skalirana ikona ili null ako resurs ne postoji
     */
    private ImageIcon loadIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) return null;
        ImageIcon icon = new ImageIcon(url);
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
    
    /**
     * Prikazuje modalni prozor za izbor težine (Beginner/Intermediate/Expert/Custom).
     * Nakon izbora poziva {@link #applyDifficulty(int, int, int)} ili {@link #newGameDialog()}.
     */
    private void showDifficultyWindow() {
        JDialog dlg = new JDialog(frame, "Odaberi težinu", true);
        dlg.setLayout(new BorderLayout());

        JLabel title = new JLabel("Odaberi težinu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        dlg.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        JButton b1 = new JButton("Beginner (9x9, 10 mina)");
        JButton b2 = new JButton("Intermediate (16x16, 40 mina)");
        JButton b3 = new JButton("Expert (16x30, 99 mina)");
        JButton bc = new JButton("Custom...");

        styleTopButton(b1);
        styleTopButton(b2);
        styleTopButton(b3);
        styleTopButton(bc);

        b1.addActionListener(e -> { 
            applyDifficulty(9, 9, 10);
            dlg.dispose();
        });

        b2.addActionListener(e -> { 
            applyDifficulty(16, 16, 40);
            dlg.dispose();
        });

        b3.addActionListener(e -> { 
            applyDifficulty(16, 30, 99);
            dlg.dispose();
        });

        bc.addActionListener(e -> {
            dlg.dispose();
            newGameDialog();
        });

        center.add(b1);
        center.add(b2);
        center.add(b3);
        center.add(bc);

        dlg.add(center, BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(frame);
        dlg.setResizable(false);
        dlg.setVisible(true);
    }

    /**
     * Primjenjuje novu težinu/parametre: resetuje model igre, timer i ponovo gradi grid.
     *
     * @param R broj redova
     * @param C broj kolona
     * @param B broj mina
     */
    private void applyDifficulty(int R, int C, int B) {
        game.reset(R, C, B);
        initTimer(); 
        lastState = GameState.RUNNING;
        buildGrid();
        refreshUI();
    }
    
    /**
     * Stilizuje dugmad na top baru i u prozoru za težine.
     *
     * @param b dugme koje se stilizuje
     */
    private void styleTopButton(JButton b) {
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBackground(new Color(200, 170, 220));             
        b.setForeground(new Color(60, 40, 80));                
        b.setBorder(
    	    BorderFactory.createCompoundBorder(
    	        BorderFactory.createLineBorder(new Color(120, 90, 150), 2, true), 
    	        BorderFactory.createEmptyBorder(6, 5, 6, 5)
    	    )
        );
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Kreira grid dugmadi prema trenutnim dimenzijama modela.
     * Svako dugme predstavlja jedno polje i prosljeđuje klikove modelu
     * (open/flag/chord). Za veće ploče koristi JScrollPane.
     */
    private void buildGrid() {
        if (scroll != null) frame.remove(scroll);

        int R = game.getRows();
        int C = game.getCols();

        gridPanel = new JPanel(new GridLayout(R, C));
        gridPanel.setBackground(GRID_BG);
        buttons = new JButton[R][C];

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                JButton btn = new JButton("");
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setPreferredSize(new Dimension(35, 35));
                btn.setFont(BTN_FONT);
                btn.setFocusPainted(false);

                final int rr = r, cc = c;
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                    	if (game.getGameState() != GameState.RUNNING) return;
                    	if (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown()) {
                             game.chord(rr, cc);
                        }
                    	else if (SwingUtilities.isLeftMouseButton(e)) {
                            game.openCell(rr, cc);
                            startTimerIfNeeded();
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            game.toggleFlag(rr, cc);
                        }
                        refreshUI();
                    }
                });
                buttons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        //scroll ako je ploca velika pa nema problema sa velikim dimenzijama
        scroll = new JScrollPane(gridPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);

        frame.add(scroll, BorderLayout.CENTER);
        frame.revalidate();
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Inicijalizuje timer (resetuje vrijeme na 0 i priprema Swing Timer).
     */
    private void initTimer() { 
        if (uiTimer != null) uiTimer.stop();
        elapsedSeconds = 0;
        timerStarted = false;
        timerLabel.setText("Vrijeme: 0s");
        uiTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText("Vrijeme: " + elapsedSeconds + "s");
        });
    }

    /**
     * Pokreće timer pri prvom potezu (ako već nije pokrenut).
     */
    private void startTimerIfNeeded() {
        if (!timerStarted && game.getGameState() == GameState.RUNNING) {
            timerStarted = true;
            uiTimer.start();
        }
    }

    /**
     * Zaustavlja timer ako je pokrenut.
     */
    private void stopTimerIfRunning() {
        if (uiTimer != null) uiTimer.stop();
    }

    /**
     * Osvježava prikaz: tekst, boje i ikonice za svako dugme prema stanju iz modela.
     * Također prikazuje poruku o pobjedi/porazu i zaustavlja timer kada igra završi.
     */
    private void refreshUI() {
        minesLabel.setText("Preostale mine: " + game.getRemainingMines());

        int R = game.getRows();
        int C = game.getCols();
        boolean running = (game.getGameState() == GameState.RUNNING);

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                JButton btn = buttons[r][c];
                CellState st = game.getCellState(r, c);
               
                btn.setRolloverEnabled(running);  //Da poslije gameover nema hover-a
                btn.setBorderPainted(true);

                if (st == CellState.CLOSED) {
                	//ako je kraj igre i ovdje je mina, pokazi minu
                    if (!running && game.hasMineForReveal(r, c)) {
                    	btn.setText("");        
                        btn.setIcon(bombIcon);
                        btn.setBackground(MINE_BG);
                    } else {
                    	btn.setText("");
                    	btn.setIcon(null);
                        btn.setBackground(CLOSED_BG);
                    }
                    btn.setEnabled(true);                    
                }
                else if (st == CellState.FLAG) {
                	btn.setText("");
                	btn.setIcon(flagIcon);   
                    btn.setBackground(FLAG_BG);
                    btn.setEnabled(true);                
                }
                else {
                    btn.setEnabled(true);                 //ostaje enabled da boje teksta budu jake
                    btn.setBorderPainted(false); 
                    if (game.hasMineForReveal(r, c)) {
                        btn.setIcon(bombIcon);
                        btn.setBackground(MINE_BG); 
                    } else {
                        int n = game.getNeighborCount(r, c);
                        if (n == 0) {
                            btn.setText("");
                            btn.setIcon(null);
                            btn.setBackground(OPEN0_BG); 
                        } else {
                            btn.setText(Integer.toString(n));
                            btn.setIcon(null);
                            btn.setBackground(OPEN_BG);
                            btn.setForeground(colorForNumber(n));
                        }
                    }
                }
            }
        }

        GameState now = game.getGameState();
        if (now != lastState) {
            lastState = now;
            if (now == GameState.WIN) {
            	stopTimerIfRunning();
            	JOptionPane.showMessageDialog(frame, "POBJEDA! :)\nVrijeme: " + elapsedSeconds + "s");
            } else if (now == GameState.LOSE) {
            	stopTimerIfRunning();
            	JOptionPane.showMessageDialog(frame, "PORAZ! :(\nVrijeme: " + elapsedSeconds + "s"); 
            }
        }
    }

    /**
     * Vraća boju teksta za broj susjednih mina (1-8).
     * @param n broj susjednih mina
     * @return boja koja se koristi za prikaz broja
     */
    private Color colorForNumber(int n) {
        switch (n) {
            case 1: return new Color(90, 60, 150);
            case 2: return new Color(100, 140, 100);
            case 3: return new Color(120, 100, 160); 
            case 4: return new Color(80, 110, 80);   
            case 5: return new Color(140, 90, 130);  
            case 6: return new Color(100, 120, 100);    
            case 7: return new Color(60, 60, 60);       
            case 8: return new Color(90, 80, 110);      
            default: return Color.BLACK;
        }
    }

    /**
     * Restartuje igru sa istim parametrima (R, C, B) i resetuje timer.
     */
    private void restartSameParams() {
        game.reset(game.getRows(), game.getCols(), game.getMinesCount());
        initTimer();
        lastState = GameState.RUNNING;
        buildGrid();
        refreshUI();
    }

    /**
     * Prikazuje dijalog za unos custom parametara (R, C, B) i pokreće novu igru.
     */
    private void newGameDialog() {
        JTextField rField = new JTextField("9");
        JTextField cField = new JTextField("9");
        JTextField bField = new JTextField("10");

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Unesi broj redova (R) između 5 i 50:"));
        p.add(rField);
        p.add(new JLabel("Unesi broj kolona (C) između 5 i 50:"));
        p.add(cField);
        p.add(new JLabel("Unesi broj mina (B) između min{R, C} i R*C*8/10:"));
        p.add(bField);

        int res = JOptionPane.showConfirmDialog(frame, p, "Nova igra", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int R = Integer.parseInt(rField.getText().trim());
                int C = Integer.parseInt(cField.getText().trim());
                int B = Integer.parseInt(bField.getText().trim());
                applyDifficulty(R,C,B);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Greška: " + e.getMessage());
            }
        }
    }
}
package codes;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class G7_Room1_PD4 implements KeyListener {

    // ---------- Background music (static) ----------
    private static Clip backgroundMusic = null;

    public static void playMusic(String filename) {
        try {
            if (backgroundMusic != null && backgroundMusic.isRunning()) {
                return; // already playing
            }
            URL url = G7_Room1_PD4.class.getResource(filename);
            if (url == null) {
                System.err.println("Music file not found: " + filename);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
            backgroundMusic = null;
        }
    }

    // ---------- rest of the class unchanged ----------
    final int TILE         = 40;
    final int PANEL_WIDTH  = 660;
    final int PANEL_HEIGHT = 660;
    private static final Font UI_FONT = new Font("Arial", Font.BOLD, 18);

    // ── Map / grid ─────────────────────────────────────────────────────────────
    int mapWidth  = 16;
    int mapHeight = 16;

    String[] mazeStrings = {
        "0P000000000000P0",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "00SS000SS000SS00",
        "0000000000000000",
        "0000000000000000",
        "0000000000000000",
        "PN00000TT00000EP",
        "0000000TT0000000"
    };

    int rows = mazeStrings.length;
    int cols = mazeStrings[0].length();
    char[][] mazeGrid;

    // ── Swing ──────────────────────────────────────────────────────────────────
    JFrame       frame;
    JLayeredPane layers;
    JLabel[]     bgLabels;      // floor tiles (layer 0)
    JLabel[]     entityLabels;  // props, NPCs, player (layer 1)
    ImageIcon[]  defaultIcons;  // store original icon for each cell (for restoring)

    // ── Images ─────────────────────────────────────────────────────────────────
    ImageIcon imgPlayerUp, imgPlayerDown, imgPlayerLeft, imgPlayerRight;
    ImageIcon imgNpc, imgDoor, imgFloor, imgSlot, imgTable, imgPlant;
    ImageIcon imgChairLeft, imgChairRight, imgChip;

    // ── Dialog ─────────────────────────────────────────────────────────────────
    Dialog dialog = new Dialog();

    // ── Player / game state ────────────────────────────────────────────────────
    int    playerRow = 13, playerCol = 7;
    String facingDirection = "DOWN";
    int    coins = 0;
    boolean hasVipPass           = false;
    boolean receivedStarterCoins = false;

    public static int carriedCoins = 0;

    // ── Slot machine state ─────────────────────────────────────────────────────
    private boolean              isSlotSpinning  = false;
    private boolean              countdownActive = false;
    private javax.swing.Timer    countdownTimer;
    private JLabel               countdownLabel;

    // ──────────────────────────────────────────────────────────────────────────

    public G7_Room1_PD4() {
        // Start background music when game starts
        playMusic("/music/RYAN.wav");

        frame = new JFrame("Room 1 - G7 Casino");

        imgPlayerUp    = safeIcon("/Images/player_up.png",      TILE,   TILE,   Color.BLUE);
        imgPlayerDown  = safeIcon("/Images/player_down.png",    TILE,   TILE,   Color.BLUE);
        imgPlayerLeft  = safeIcon("/Images/player_left.png",    TILE,   TILE,   Color.BLUE);
        imgPlayerRight = safeIcon("/Images/player_right.png",   TILE,   TILE,   Color.BLUE);
        imgNpc         = safeIcon("/Images/npc.png",            TILE,   TILE,   Color.GREEN);
        imgDoor        = safeIcon("/Images/enemy.png",          TILE,   TILE,   Color.RED);
        imgFloor       = safeIcon("/Images/floor.png",          TILE,   TILE,   Color.LIGHT_GRAY);
        imgSlot        = safeIcon("/Images/G7_slotmachines.png",TILE*2, TILE*3, Color.MAGENTA);
        imgTable       = safeIcon("/Images/G7_table.png",       TILE*2, TILE*2, Color.ORANGE);
        imgPlant       = safeIcon("/Images/G7_plant.png",       TILE,   TILE,   Color.CYAN);
        imgChairLeft   = safeIcon("/Images/chair_left.png",     TILE,   TILE,   Color.BLUE);
        imgChairRight  = safeIcon("/Images/chair_right.png",    TILE,   TILE,   Color.GREEN);
        imgChip        = safeIcon("/Images/chip.png",           32,     32,     Color.YELLOW);

        initMazeAndChairs();
        loadSaveData();
        setFrame();
    }

    // ── Frame setup (matches G4 pattern) ──────────────────────────────────────

    public void setFrame() {
        layers = new JLayeredPane();
        layers.setLayout(new GraphPaperLayout(new Dimension(mapWidth, mapHeight)));
        layers.setBounds(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        frame.setContentPane(layers);

        // Layer 0 — floor tiles
        bgLabels = new JLabel[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bgLabels[r * cols + c] = new JLabel(imgFloor);
                layers.add(bgLabels[r * cols + c], new Rectangle(c, r, 1, 1), Integer.valueOf(0));
            }
        }

        // Layer 1 — entities (props, NPCs, player)
        entityLabels = new JLabel[rows * cols];
        defaultIcons = new ImageIcon[rows * cols];
        for (int i = 0; i < entityLabels.length; i++) {
            entityLabels[i] = new JLabel();
            entityLabels[i].setHorizontalAlignment(JLabel.CENTER);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;
                char ch = mazeGrid[r][c];
                int w = 1, h = 1;

                if (isSlotTopLeft(r, c)) {
                    entityLabels[idx].setIcon(imgSlot);
                    w = 2; h = 3;
                } else if (isTableTopLeft(r, c)) {
                    entityLabels[idx].setIcon(imgTable);
                    w = 2; h = 2;
                } else {
                    switch (ch) {
                        case 'P' -> { entityLabels[idx].setIcon(imgPlant); defaultIcons[idx] = imgPlant; }
                        case 'N' -> { entityLabels[idx].setIcon(imgNpc);   defaultIcons[idx] = imgNpc; }
                        case 'E' -> { entityLabels[idx].setIcon(imgDoor);  defaultIcons[idx] = imgDoor; }
                        case 'L' -> { entityLabels[idx].setIcon(imgChairLeft); defaultIcons[idx] = imgChairLeft; }
                        case 'R' -> { entityLabels[idx].setIcon(imgChairRight); defaultIcons[idx] = imgChairRight; }
                    }
                }

                // Player
                if (r == playerRow && c == playerCol) {
                    entityLabels[idx].setIcon(imgPlayerDown);
                }

                layers.add(entityLabels[idx], new Rectangle(c, r, w, h), Integer.valueOf(1));
            }
        }

        // Layer 2 — coin HUD (top-right corner)
        JLabel chipLabel = new JLabel(imgChip);
        layers.add(chipLabel, new Rectangle(mapWidth - 2, 0, 1, 1), Integer.valueOf(2));

        JLabel coinLabel = new JLabel("", SwingConstants.RIGHT);
        coinLabel.setForeground(Color.WHITE);
        coinLabel.setFont(UI_FONT);
        layers.add(coinLabel, new Rectangle(mapWidth - 4, 0, 2, 1), Integer.valueOf(2));
        new javax.swing.Timer(100, e -> coinLabel.setText(String.format("%4d", coins))).start();

        // Layer 3 — countdown overlay (hidden until slot spins)
        countdownLabel = new JLabel("", SwingConstants.CENTER);
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 28));
        countdownLabel.setOpaque(true);
        countdownLabel.setBackground(new Color(0, 0, 0, 200));
        countdownLabel.setVisible(false);
        layers.add(countdownLabel, new Rectangle(1, mapHeight - 3, mapWidth - 2, 1), Integer.valueOf(3));

        frame.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addKeyListener(this);
        dialog.addKey(frame);
    }

    // ── Image helper ──────────────────────────────────────────────────────────

    private ImageIcon safeIcon(String path, int w, int h, Color fallback) {
        java.net.URL url = getClass().getResource(path);
        Image img;
        if (url != null) {
            img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        } else {
            System.err.println("WARNING: missing " + path);
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bi.createGraphics();
            g2.setColor(fallback); g2.fillRect(0, 0, w, h);
            g2.setColor(Color.BLACK); g2.drawRect(0, 0, w-1, h-1);
            g2.dispose();
            img = bi;
        }
        return new ImageIcon(img);
    }

    // ── Grid helpers ───────────────────────────────────────────────────────────

    private void initMazeAndChairs() {
        mazeGrid = new char[rows][cols];
        for (int r = 0; r < rows; r++) mazeGrid[r] = mazeStrings[r].toCharArray();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (isSlotTopLeft(r, c)) addChairsAroundSlot(r, c);
    }

    private void addChairsAroundSlot(int r, int c) {
        // Shift chairs down by 1 tile – place at row r+1 (below the slot block)
        int chairRow = r + 1;
        if (chairRow >= rows) return;

        int leftCol = c - 1;
        if (leftCol >= 0 && mazeGrid[chairRow][leftCol] == '0') mazeGrid[chairRow][leftCol] = 'L';

        int rightCol = c + 2;
        if (rightCol < cols && mazeGrid[chairRow][rightCol] == '0') mazeGrid[chairRow][rightCol] = 'R';
    }

    private boolean isSlotTopLeft(int r, int c) {
        if (mazeGrid[r][c] != 'S') return false;
        boolean noSLeft = (c == 0) || mazeGrid[r][c-1] != 'S';
        return noSLeft && (r % 2 == 1);
    }

    private boolean isTableTopLeft(int r, int c) {
        if (r + 1 >= rows || c + 1 >= cols) return false;
        return mazeGrid[r][c]=='T' && mazeGrid[r][c+1]=='T'
            && mazeGrid[r+1][c]=='T' && mazeGrid[r+1][c+1]=='T';
    }

    private boolean isAdjacentToSlotMachine() {
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nr = playerRow + d[0], nc = playerCol + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols)
                if (mazeGrid[nr][nc] == 'S') return true;
        }
        return false;
    }

    private Point getAdjacentTile(char target) {
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nr = playerRow + d[0], nc = playerCol + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols)
                if (mazeGrid[nr][nc] == target) return new Point(nc, nr);
        }
        return null;
    }

    // ── Player label helpers (restore original icon when leaving) ──────────────

    private ImageIcon currentPlayerIcon() {
        return switch (facingDirection) {
            case "UP"    -> imgPlayerUp;
            case "LEFT"  -> imgPlayerLeft;
            case "RIGHT" -> imgPlayerRight;
            default      -> imgPlayerDown;
        };
    }

    private void updatePlayerLabel() {
        int idx = playerRow * cols + playerCol;
        entityLabels[idx].setIcon(currentPlayerIcon());
    }

    private void clearPlayerAt(int r, int c) {
        int idx = r * cols + c;
        // Restore the default icon (chair, plant, NPC, etc.) instead of clearing to null
        if (defaultIcons[idx] != null) {
            entityLabels[idx].setIcon(defaultIcons[idx]);
        } else {
            entityLabels[idx].setIcon(null);
        }
    }

    // ── Save / load ───────────────────────────────────────────────────────────

    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G7_Room1_PD4");
        SaveSystem.startTimer(save.timeSeconds);
        coins = 0;
        hasVipPass           = save.hasFlag("hasVipPass");
        receivedStarterCoins = save.hasFlag("receivedStarterCoins");
    }

    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G7_Room1_PD4")
                .flag(hasVipPass           ? "hasVipPass"           : null)
                .flag(receivedStarterCoins ? "receivedStarterCoins" : null)
                .battles(SaveSystem.getDefeatedBosses())
        );
    }

    // ── Dialog helpers ────────────────────────────────────────────────────────

    private void showDialog(String[] lines, Runnable onClose) {
        dialog.show(layers, lines, null, null, mapWidth, mapHeight, onClose);
    }

    // ── Slot machine ──────────────────────────────────────────────────────────

    private void startSlotMachine() {
        if (isSlotSpinning || dialog.isVisible()) return;
        if (coins < 5) {
            showDialog(new String[]{"Not enough coins (5 required)."}, null);
            return;
        }
        coins -= 5;
        saveProgress();
        isSlotSpinning  = true;
        countdownActive = true;

        countdownLabel.setText("3...");
        countdownLabel.setVisible(true);
        layers.repaint();

        countdownTimer = new javax.swing.Timer(600, new ActionListener() {
            int step = 1;
            @Override public void actionPerformed(ActionEvent e) {
                if (step == 1) {
                    countdownLabel.setText("2..."); layers.repaint(); step++;
                } else if (step == 2) {
                    countdownLabel.setText("1..."); layers.repaint(); step++;
                } else {
                    countdownTimer.stop();
                    countdownActive = false;
                    countdownLabel.setVisible(false);

                    if (Math.random() < 0.2) {
                        coins += 500;
                        showDialog(new String[]{"Jackpot! You got 500 coins!"}, () -> {
                            isSlotSpinning = false; saveProgress();
                        });
                    } else {
                        showDialog(new String[]{"Aw, you lost.", "Want to try again?"}, () -> {
                            isSlotSpinning = false;
                            startSlotMachine(); // auto-retry
                        });
                    }
                    layers.repaint();
                }
            }
        });
        countdownTimer.start();
    }

    // ── NPC interactions ──────────────────────────────────────────────────────

    private void handleDealer() {
        if (!receivedStarterCoins) {
            showDialog(
                new String[]{"Take these 100 coins and turn them into 500",
                             "and maybe I'll give you this VIP pass."},
                () -> { coins += 100; receivedStarterCoins = true; saveProgress(); }
            );
        } else if (coins < 500) {
            showDialog(new String[]{"Come back when you have 500 coins."}, null);
        } else if (!hasVipPass) {
            showDialog(new String[]{"Impressive… here's your VIP pass."}, () -> {
                coins -= 500; hasVipPass = true; saveProgress();
            });
        } else {
            showDialog(new String[]{"You already have the VIP pass."}, null);
        }
    }

    private void handleDoorGuard() {
        if (!hasVipPass) {
            showDialog(new String[]{"This place is only for VIPs."}, null);
        } else {
            showDialog(new String[]{"Please, let me escort you to the VIP room."}, this::transitionToRoom2);
        }
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    void movePlayer(int dr, int dc) {
        if (dialog.isVisible() || isSlotSpinning) return;
        int nr = playerRow + dr, nc = playerCol + dc;
        if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) return;
        char tile = mazeGrid[nr][nc];
        if (tile=='S'||tile=='T'||tile=='P'||tile=='N'||tile=='M') return;
        if (tile=='E') { handleDoorGuard(); return; }
        clearPlayerAt(playerRow, playerCol);
        playerRow = nr; playerCol = nc;
        updatePlayerLabel();
        layers.repaint();
    }

    void interact() {
        if (dialog.isVisible() || isSlotSpinning) return;
        if (isAdjacentToSlotMachine()) { startSlotMachine(); return; }
        if (getAdjacentTile('N') != null) { handleDealer();    return; }
        if (getAdjacentTile('E') != null) { handleDoorGuard(); }
    }

    // ── Room transition ───────────────────────────────────────────────────────

    void transitionToRoom2() {
        saveProgress();
        carriedCoins = coins;
        frame.dispose();
        new G7_Room2_PD6();
    }

    // ── Key input ─────────────────────────────────────────────────────────────

    @Override
    public void keyPressed(KeyEvent e) {
        if (dialog.isVisible() || Battle.paused) return;
        if (countdownActive) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP,    KeyEvent.VK_W -> facingDirection = "UP";
            case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> facingDirection = "DOWN";
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> facingDirection = "LEFT";
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> facingDirection = "RIGHT";
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP,    KeyEvent.VK_W -> movePlayer(-1,  0);
            case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> movePlayer( 1,  0);
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> movePlayer( 0, -1);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> movePlayer( 0,  1);
            case KeyEvent.VK_SPACE                -> interact();
        }
        updatePlayerLabel();
        layers.repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new G7_Room1_PD4();
    }
}

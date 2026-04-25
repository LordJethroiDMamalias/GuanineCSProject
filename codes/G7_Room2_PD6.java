package codes;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

public class G7_Room2_PD6 implements KeyListener {

    final int TILE         = 40;
    final int PANEL_WIDTH  = 660;
    final int PANEL_HEIGHT = 660;
    private static final Font UI_FONT = new Font("Arial", Font.BOLD, 18);

    int mapWidth  = 16;
    int mapHeight = 16;

    String[] mazeStrings = {
        "00000CCECC000000",
        "00000CCCCC000000",
        "00AA000H000BBB00",
        "00AAN000000BBB00",
        "00AA0000000BBB00",
        "0000000000000000",
        "00AA0000000BBB00",
        "00AA0000000BBB00",
        "00AA0000000BBB00",
        "0000000000000000",
        "00AA0000000BBB00",
        "00AA0000000BBB00",
        "00AA0000000BBB00",
        "0000000000000000",
        "0000000000000000",
        "0000000000000000"
    };

    int rows = mazeStrings.length;
    int cols = mazeStrings[0].length();

    char[][] terrainGrid;
    char[][] entityGrid;
    boolean[][] isTalkSpot;

    Point npcPosition   = null;
    Point enemyPosition = null;

    private static class AssetInfo {
        int width, height;
        ImageIcon icon;
        AssetInfo(int w, int h, ImageIcon i) { width = w; height = h; icon = i; }
    }
    private final Map<Character, AssetInfo> assetMap = new LinkedHashMap<>();

    JFrame       frame;
    JLayeredPane layers;
    JLabel[]     bgLabels;
    JLabel[]     entityLabels;

    ImageIcon imgPlayerUp, imgPlayerDown, imgPlayerLeft, imgPlayerRight;
    ImageIcon imgWall, imgNpc, imgEnemy, imgFloor, imgChip;

    Dialog dialog = new Dialog();

    int     playerRow = 1, playerCol = 1;
    String  facingDirection = "DOWN";
    int     coins = 0;
    boolean blackjackCompleted = false;
    boolean firstEncounterDone = false;
    boolean bossDefeated       = false;
    boolean dealerIntroShown   = false;
    int     playerHP = 100;
    Battle  battle = new Battle();

    // ── Blackjack state ────────────────────────────────────────────────────────
    private boolean blackjackActive = false;
    private ArrayList<Integer> playerHand = new ArrayList<>();
    private ArrayList<Integer> dealerHand = new ArrayList<>();
    private boolean playerTurn   = true;
    private boolean dealerHidden = true;
    private boolean gameResolved = false;
    private final int WAGER = 10;
    private String blackjackPrompt = "";

    // ── Blackjack UI ───────────────────────────────────────────────────────────
    private JPanel  bjGlass;
    private JPanel  bjHudPanel;
    private JLabel  bjHudLabel;
    private JButton hitButton, standButton;

    public G7_Room2_PD6() {
        frame  = new JFrame("Room 2 - G7");
        layers = new JLayeredPane();
        layers.setLayout(new GraphPaperLayout(new Dimension(mapWidth, mapHeight)));
        layers.setBounds(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        frame.setContentPane(layers);

        imgPlayerUp    = safeIcon("images/player_up.png",    TILE, TILE, Color.BLUE);
        imgPlayerDown  = safeIcon("images/player_down.png",  TILE, TILE, Color.BLUE);
        imgPlayerLeft  = safeIcon("images/player_left.png",  TILE, TILE, Color.BLUE);
        imgPlayerRight = safeIcon("images/player_right.png", TILE, TILE, Color.BLUE);
        imgWall        = safeIcon("images/wall.png",         TILE, TILE, Color.DARK_GRAY);
        imgNpc         = safeIcon("images/npc2.png",         TILE, TILE, Color.GREEN);
        imgEnemy       = safeIcon("images/npc3.png",         TILE, TILE, Color.RED);
        imgFloor       = safeIcon("images/floor2.png",       TILE, TILE, Color.LIGHT_GRAY);
        imgChip        = safeIcon("images/chip.png",         32,   32,   Color.YELLOW);

        assetMap.put('A', new AssetInfo(2, 3, safeIcon("images/assetA.png", TILE*2, TILE*3, Color.MAGENTA)));
        assetMap.put('B', new AssetInfo(3, 3, safeIcon("images/assetB.png", TILE*3, TILE*3, Color.ORANGE)));
        assetMap.put('C', new AssetInfo(5, 2, safeIcon("images/assetC.png", TILE*5, TILE*2, Color.CYAN)));

        coins = G7_Room1_PD4.carriedCoins;
        if (coins <= 0) coins = 100;

        buildGrids();
        setupUI();
        setupBlackjackGlass();
        setupCoinHUD();

        frame.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addKeyListener(this);
        dialog.addKey(frame);
    }

    // ── Safe image loader ──────────────────────────────────────────────────────
    private ImageIcon safeIcon(String path, int w, int h, Color fallbackColor) {
        URL url = getClass().getResource(path);
        BufferedImage img;
        if (url != null) {
            try {
                Image original = new ImageIcon(url).getImage();
                img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(original, 0, 0, w, h, null);
                g2.dispose();
            } catch (Exception e) {
                img = createFallbackImage(w, h, fallbackColor);
            }
        } else {
            System.err.println("WARNING: missing " + path + " – using fallback colour");
            img = createFallbackImage(w, h, fallbackColor);
        }
        return new ImageIcon(img);
    }

    private BufferedImage createFallbackImage(int w, int h, Color color) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(color); g2.fillRect(0, 0, w, h);
        g2.setColor(Color.BLACK); g2.drawRect(0, 0, w-1, h-1);
        g2.dispose();
        return img;
    }

    // ── Grid builder ───────────────────────────────────────────────────────────
    private void buildGrids() {
        terrainGrid = new char[rows][cols];
        entityGrid  = new char[rows][cols];
        isTalkSpot  = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) Arrays.fill(entityGrid[r], ' ');

        boolean[][] isAssetCell = new boolean[rows][cols];
        for (char ch : assetMap.keySet()) {
            AssetInfo info = assetMap.get(ch);
            int w = info.width, h = info.height;
            for (int r = 0; r <= rows - h; r++) {
                for (int c = 0; c <= cols - w; c++) {
                    boolean match = true;
                    outer:
                    for (int dr = 0; dr < h; dr++)
                        for (int dc = 0; dc < w; dc++) {
                            char cell = mazeStrings[r+dr].charAt(c+dc);
                            if (cell != ch && cell != 'N' && cell != 'E') { match = false; break outer; }
                        }
                    if (match)
                        for (int dr = 0; dr < h; dr++)
                            for (int dc = 0; dc < w; dc++) {
                                terrainGrid[r+dr][c+dc] = ch;
                                isAssetCell[r+dr][c+dc] = true;
                            }
                }
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char orig = mazeStrings[r].charAt(c);
                if (orig == 'N') {
                    entityGrid[r][c] = 'N'; npcPosition = new Point(c, r);
                    if (!isAssetCell[r][c]) terrainGrid[r][c] = '0';
                } else if (orig == 'E') {
                    entityGrid[r][c] = 'E'; enemyPosition = new Point(c, r);
                    if (!isAssetCell[r][c]) terrainGrid[r][c] = '0';
                } else if (orig == '1') {
                    terrainGrid[r][c] = '1';
                } else if (orig == 'H') {
                    terrainGrid[r][c] = '0'; isTalkSpot[r][c] = true;
                } else if (!isAssetCell[r][c]) {
                    terrainGrid[r][c] = '0';
                }
            }
        }
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (terrainGrid[r][c] == 0) terrainGrid[r][c] = '0';
    }

    // ── UI layers ──────────────────────────────────────────────────────────────
    private void setupUI() {
        bgLabels     = new JLabel[rows * cols];
        entityLabels = new JLabel[rows * cols];

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                bgLabels[r * cols + c] = new JLabel(terrainGrid[r][c] == '1' ? imgWall : imgFloor);
                layers.add(bgLabels[r * cols + c], new Rectangle(c, r, 1, 1), Integer.valueOf(0));
            }

        boolean[][] placed = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (placed[r][c]) continue;
                char terrain = terrainGrid[r][c];
                if (assetMap.containsKey(terrain) && isTopLeftOfAsset(r, c, terrain)) {
                    AssetInfo info = assetMap.get(terrain);
                    int idx = r * cols + c;
                    entityLabels[idx] = new JLabel(info.icon);
                    layers.add(entityLabels[idx], new Rectangle(c, r, info.width, info.height), Integer.valueOf(1));
                    for (int dr = 0; dr < info.height; dr++)
                        for (int dc = 0; dc < info.width; dc++)
                            placed[r+dr][c+dc] = true;
                }
            }
        }

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                char entity = entityGrid[r][c];
                if (entity == 'N' || entity == 'E') {
                    JLabel label = new JLabel(entity == 'N' ? imgNpc : imgEnemy);
                    layers.add(label, new Rectangle(c, r, 1, 1), Integer.valueOf(2));
                }
            }

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;
                entityLabels[idx] = new JLabel();
                if (r == playerRow && c == playerCol) entityLabels[idx].setIcon(imgPlayerDown);
                layers.add(entityLabels[idx], new Rectangle(c, r, 1, 1), Integer.valueOf(3));
            }
    }

    // ── Blackjack glass pane ───────────────────────────────────────────────────
    private void setupBlackjackGlass() {
        bjGlass = new JPanel(null);
        bjGlass.setOpaque(false);
        frame.setGlassPane(bjGlass);
        bjGlass.setVisible(true);

        bjHudPanel = new JPanel(null);
        bjHudPanel.setBounds(0, 0, PANEL_WIDTH, 185);
        bjHudPanel.setBackground(new Color(15, 15, 15, 230));
        bjHudPanel.setOpaque(true);
        bjHudPanel.setVisible(false);
        bjGlass.add(bjHudPanel);

        bjHudLabel = new JLabel();
        bjHudLabel.setBounds(12, 8, PANEL_WIDTH - 24, 130);
        bjHudLabel.setForeground(Color.WHITE);
        bjHudLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        bjHudLabel.setVerticalAlignment(JLabel.TOP);
        bjHudPanel.add(bjHudLabel);

        hitButton   = new JButton("HIT");
        standButton = new JButton("STAND");
        hitButton.setFont(UI_FONT);
        standButton.setFont(UI_FONT);
        hitButton.setBounds(PANEL_WIDTH - 230, 140, 100, 38);
        standButton.setBounds(PANEL_WIDTH - 120, 140, 100, 38);
        hitButton.setVisible(true);
        standButton.setVisible(true);
        hitButton.addActionListener(e -> onHit());
        standButton.addActionListener(e -> onStand());
        bjHudPanel.add(hitButton);
        bjHudPanel.add(standButton);
    }

    // ── Coin HUD ───────────────────────────────────────────────────────────────
    private void setupCoinHUD() {
        JLabel chipLabel = new JLabel(imgChip);
        layers.add(chipLabel, new Rectangle(mapWidth - 2, 0, 1, 1), Integer.valueOf(2));

        JLabel coinLabel = new JLabel("", SwingConstants.RIGHT);
        coinLabel.setForeground(Color.WHITE);
        coinLabel.setFont(UI_FONT);
        layers.add(coinLabel, new Rectangle(mapWidth - 4, 0, 2, 1), Integer.valueOf(2));
        new javax.swing.Timer(100, e -> coinLabel.setText(String.format("%4d", coins))).start();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private boolean isTopLeftOfAsset(int r, int c, char ch) {
        AssetInfo info = assetMap.get(ch);
        if (info == null) return false;
        if (r + info.height > rows || c + info.width > cols) return false;
        if (r > 0 && terrainGrid[r-1][c] == ch) return false;
        if (c > 0 && terrainGrid[r][c-1] == ch) return false;
        for (int dr = 0; dr < info.height; dr++)
            for (int dc = 0; dc < info.width; dc++)
                if (terrainGrid[r+dr][c+dc] != ch) return false;
        return true;
    }

    private Point getAdjacentTile(char target) {
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs)
            for (int dist = 1; dist <= 10; dist++) {
                int nr = playerRow + d[0]*dist, nc = playerCol + d[1]*dist;
                if (nr<0||nr>=rows||nc<0||nc>=cols) break;
                if (terrainGrid[nr][nc] == '1') break;
                if (entityGrid[nr][nc] == target) return new Point(nc, nr);
            }
        return null;
    }

    private ImageIcon currentPlayerIcon() {
        return switch (facingDirection) {
            case "UP"    -> imgPlayerUp;
            case "LEFT"  -> imgPlayerLeft;
            case "RIGHT" -> imgPlayerRight;
            default      -> imgPlayerDown;
        };
    }

    private void updatePlayerLabel() {
        entityLabels[playerRow * cols + playerCol].setIcon(currentPlayerIcon());
    }

    private void clearPlayerAt(int r, int c) {
        entityLabels[r * cols + c].setIcon(null);
    }

    private void showDialog(String[] lines, Runnable onClose) {
        dialog.show(layers, lines, null, null, mapWidth, mapHeight, onClose);
    }

    // ── Blackjack logic ────────────────────────────────────────────────────────
    private int handValue(ArrayList<Integer> hand) {
        int sum = 0, aces = 0;
        for (int v : hand) { if (v == 1) aces++; sum += Math.min(v, 10); }
        while (aces > 0 && sum + 10 <= 21) { sum += 10; aces--; }
        return sum;
    }

    private String cardToString(int v) {
        return switch (v) { case 1->"A"; case 11->"J"; case 12->"Q"; case 13->"K"; default->String.valueOf(v); };
    }

    private int drawCard() { return (int)(Math.random() * 13) + 1; }

    private void dealInitialCards() {
        playerHand.clear(); dealerHand.clear();
        playerHand.add(drawCard()); playerHand.add(drawCard());
        dealerHand.add(drawCard()); dealerHand.add(drawCard());
        dealerHidden = true; playerTurn = true;
        blackjackPrompt = "Your turn — Hit or Stand?";
    }

    private void dealerTurn() {
        dealerHidden = false;
        playerTurn   = false;
        updateBjHud();
        javax.swing.Timer dealerTimer = new javax.swing.Timer(700, null);
        dealerTimer.addActionListener(e -> {
            if (handValue(dealerHand) < 17) {
                dealerHand.add(drawCard());
                updateBjHud();
            } else {
                dealerTimer.stop();
                int pv = handValue(playerHand), dv = handValue(dealerHand);
                endBlackjack(pv <= 21 && (dv > 21 || pv > dv));
            }
        });
        dealerTimer.start();
    }

    private void showBjUi(boolean visible) {
        bjHudPanel.setVisible(visible);
        bjGlass.revalidate();
        bjGlass.repaint();
    }

    private void endBlackjack(boolean playerWon) {
        gameResolved    = true;
        blackjackActive = false;
        showBjUi(false);

        String[] lines;
        if (playerWon) {
            coins += WAGER * 2;
            blackjackCompleted = true;
            lines = new String[]{
                "Impressive, I think you're ready.",
                "Please go talk to the man behind the counter.",
                "He might have a job for you to complete."
            };
        } else {
            lines = new String[]{
                "You lost. Better luck next time."
            };
        }
        dialog.show(layers, lines, null, null, mapWidth, mapHeight, () -> {
            frame.requestFocusInWindow();
        });
        javax.swing.Timer autoCloseTimer = new javax.swing.Timer(2000, e -> dialog.hide());
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();
    }

    private void startBlackjack() {
        if (coins < WAGER) { resetGameOnInsufficientCoins(); return; }
        coins -= WAGER;
        blackjackActive = true;
        gameResolved    = false;
        dealInitialCards();
        showBjUi(true);
        updateBjHud();
        frame.requestFocusInWindow();
    }

    private void resetGameOnInsufficientCoins() {
        coins = 100; blackjackCompleted = false;
        showDialog(new String[]{
            "Not enough coins to play (10 required).",
            "I'll spot you 100 coins. Try again!"
        }, null);
    }

    private void updateBjHud() {
        String cardStyle = "background:#1a1a1a;color:white;padding:2px 6px;"
                         + "border:1px solid #aaa;border-radius:3px;font-size:15px;font-weight:bold;";
        String hiddenStyle = "background:#444;color:#777;padding:2px 6px;"
                           + "border:1px solid #555;border-radius:3px;font-size:15px;";

        StringBuilder sb = new StringBuilder(
            "<html><body style='color:white;font-family:Arial;font-size:14px;'>");

        // Dealer row
        sb.append("<div style='margin-bottom:6px;'><b>DEALER</b>&nbsp;&nbsp;");
        for (int i = 0; i < dealerHand.size(); i++) {
            if (i == 1 && dealerHidden && playerTurn) {
                sb.append("<span style='").append(hiddenStyle).append("'>?</span>&nbsp;");
            } else {
                int v = dealerHand.get(i);
                String cs = cardStyle + (v == 1 || v >= 11 ? "color:#FF8888;" : "color:white;");
                sb.append("<span style='").append(cs).append("'>")
                  .append(cardToString(v)).append("</span>&nbsp;");
            }
        }
        if (!dealerHidden || !playerTurn) {
            int dv = handValue(dealerHand);
            String col = dv > 21 ? "#FF4444" : dv == 21 ? "#FFD700" : "#ccc";
            sb.append("&nbsp;<span style='color:").append(col).append(";font-size:13px;'>(")
              .append(dv).append(")</span>");
        } else {
            sb.append("&nbsp;<span style='color:#aaa;font-size:13px;'>(showing ")
              .append(Math.min(dealerHand.get(0), 10)).append(")</span>");
        }
        sb.append("</div>");

        // Player row
        sb.append("<div style='margin-bottom:8px;'><b>YOU&nbsp;&nbsp;&nbsp;</b>&nbsp;&nbsp;");
        for (int v : playerHand) {
            String cs = cardStyle + (v == 1 || v >= 11 ? "color:#FF8888;" : "color:white;");
            sb.append("<span style='").append(cs).append("'>")
              .append(cardToString(v)).append("</span>&nbsp;");
        }
        int pv = handValue(playerHand);
        String pvCol = pv > 21 ? "#FF4444" : pv == 21 ? "#FFD700" : "#ccc";
        sb.append("&nbsp;<span style='color:").append(pvCol).append(";font-size:13px;'>(")
          .append(pv).append(")</span></div>");

        // Prompt
        sb.append("<div style='color:#FFD700;font-size:13px;'>")
          .append(blackjackPrompt).append("</div>");
        sb.append("</body></html>");

        bjHudLabel.setText(sb.toString());
        bjHudPanel.repaint();
    }

    private void onHit() {
        if (!blackjackActive || !playerTurn || gameResolved) return;
        playerHand.add(drawCard());
        if (handValue(playerHand) > 21) {
            blackjackPrompt = "Bust! You went over 21.";
            updateBjHud();
            javax.swing.Timer t = new javax.swing.Timer(900, e -> endBlackjack(false));
            t.setRepeats(false); t.start();
        } else {
            blackjackPrompt = "Hit or Stand?";
            updateBjHud();
        }
    }

    private void onStand() {
        if (!blackjackActive || !playerTurn || gameResolved) return;
        blackjackPrompt = "Dealer drawing...";
        updateBjHud();
        dealerTurn();
    }

    // ── Movement ───────────────────────────────────────────────────────────────
    void movePlayer(int dr, int dc) {
        if (dialog.isVisible() || blackjackActive) return;
        int nr = playerRow + dr, nc = playerCol + dc;
        if (nr<0||nr>=rows||nc<0||nc>=cols) return;
        char terrain = terrainGrid[nr][nc];
        char entity  = entityGrid[nr][nc];
        if (terrain=='1' || assetMap.containsKey(terrain) || entity=='N' || entity=='E') return;

        clearPlayerAt(playerRow, playerCol);
        playerRow = nr; playerCol = nc;
        updatePlayerLabel();
        layers.repaint();
    }

    // ── Interaction ────────────────────────────────────────────────────────────
    void interact() {
        if (blackjackActive) return;
        if (dialog.isVisible()) return;

        if (isTalkSpot[playerRow][playerCol]) {
            handleBossEncounter();
            return;
        }

        if (getAdjacentTile('N') != null) {
            handleDealerTalk();
            return;
        }

        if (getAdjacentTile('E') != null) {
            handleBossEncounter();
        }
    }

    private void handleDealerTalk() {
        if (bossDefeated) {
            showDialog(new String[]{
                "The casino is safe now. Well played."
            }, () -> frame.requestFocusInWindow());
            return;
        }

        if (!dealerIntroShown) {
            dealerIntroShown = true;
            showDialog(new String[]{
                "Welcome to the blackjack table.",
                "Let's see if you have what it takes.",
                "Place your bet — " + WAGER + " coins. Good luck."
            }, () -> startBlackjack());
        } else {
            String prompt = blackjackCompleted
                ? "Ready for another hand? (" + WAGER + " coins)"
                : "Let's try again. (" + WAGER + " coins)";
            showDialog(new String[]{ prompt }, () -> startBlackjack());
        }
    }

    private void handleBossEncounter() {
        if (!blackjackCompleted) {
            showDialog(new String[]{
                "You haven't proven yourself at the blackjack table yet.",
                "Win a game against the dealer first."
            }, () -> frame.requestFocusInWindow());
        } else if (!firstEncounterDone) {
            showDialog(new String[]{
                "A dark presence looms...",
                "RYAN is watching you."
            }, () -> { firstEncounterDone = true; frame.requestFocusInWindow(); });
        } else if (!bossDefeated) {
            triggerBossBattle();
        } else {
            showDialog(new String[]{
                "RYAN has been defeated.",
                "The casino is safe."
            }, () -> frame.requestFocusInWindow());
        }
    }

    // ================== BOSS BATTLE FIXED ==================
    void triggerBossBattle() {
        // Stop background music before the battle starts
        G7_Room1_PD4.stopMusic();

        // Set player HP in the battle instance
        battle.maxHp = playerHP;
        battle.hp = playerHP;

        // Start the battle with a background image and the boss name
        // Adjust the path to your actual background image (e.g., "/Images/G7_boss_bg.png")
        battle.start(frame, "images/G7_boss_bg.png", "RYAN");

        // Timer to check when the battle ends
        javax.swing.Timer t = new javax.swing.Timer(500, e -> {
            if (!Battle.paused) {
                ((javax.swing.Timer) e.getSource()).stop();
                playerHP = battle.hp;
                if (playerHP <= 0) {
                    JOptionPane.showMessageDialog(frame, "You were defeated...");
                    System.exit(0);
                } else {
                    bossDefeated = true;
                    JOptionPane.showMessageDialog(frame, "You defeated RYAN!\nYou got the money 💰");
                    layers.repaint();
                }
                frame.requestFocusInWindow();
            }
        });
        t.start();
    }

    // ── Key input ──────────────────────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        if (dialog.isVisible() || Battle.paused || blackjackActive) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> facingDirection = "UP";
            case KeyEvent.VK_DOWN  -> facingDirection = "DOWN";
            case KeyEvent.VK_LEFT  -> facingDirection = "LEFT";
            case KeyEvent.VK_RIGHT -> facingDirection = "RIGHT";
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> movePlayer(-1,  0);
            case KeyEvent.VK_DOWN  -> movePlayer( 1,  0);
            case KeyEvent.VK_LEFT  -> movePlayer( 0, -1);
            case KeyEvent.VK_RIGHT -> movePlayer( 0,  1);
            case KeyEvent.VK_SPACE -> interact();
        }
        updatePlayerLabel();
        layers.repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) { new G7_Room2_PD6(); }
}

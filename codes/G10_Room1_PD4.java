package bossroom;

/*
 * ── SETUP NOTE ───────────────────────────────────────────────────────────────
 * This file lives in  src/bossroom/minibossroom.java
 * Battle.java lives in  src/codes/Battle.java
 *
 * In NetBeans: right-click the project → Properties → Sources
 * Make sure the Source Package Folder is "src" (not "src/bossroom").
 * Both packages must sit under the SAME source root.
 *
 * Still failing? Right-click project → Clean and Build, then Run.
 * ─────────────────────────────────────────────────────────────────────────────
 */

//it doesnt work fully pa pero na implement ko na ang dialogue and battle system imma fix tmrw

import codes.Battle;
import codes.SaveSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class minibossroom extends JPanel implements KeyListener {

    // ── Grid / display ────────────────────────────────────────────────────────
    final int COLS = 33;
    final int ROWS = 20;
    final int WIDTH  = 660;
    final int HEIGHT = 660;
    final int TILE_W = WIDTH  / COLS;
    final int TILE_H = HEIGHT / ROWS;

    // ── Key positions ─────────────────────────────────────────────────────────
    final int SPAWN_X          = 11;
    final int SPAWN_Y          = 14;
    final int DOOR_X           = 15;
    final int DOOR_Y           = 7;
    final int BOSS_TRIGGER_X   = 15;
    final int BOSS_TRIGGER_Y   = 8;
    final int BOSS_TILE_X      = 14;
    final int BOSS_TILE_Y      = 6;

    int gridX = SPAWN_X;
    int gridY = SPAWN_Y;

    // ── Images ────────────────────────────────────────────────────────────────
    BufferedImage mapImg;
    BufferedImage playerUp, playerDown, playerLeft, playerRight;
    BufferedImage currentPlayer;
    BufferedImage bossImg;

    // ── Walkability ───────────────────────────────────────────────────────────
    boolean[][] walkable = new boolean[ROWS][COLS];

    // ── Battle ────────────────────────────────────────────────────────────────
    private final Battle battle       = new Battle();
    private boolean      battleActive  = false;
    private boolean      bossDefeated  = false;

    // ── Save ──────────────────────────────────────────────────────────────────
    private SaveSystem.SaveData saveData;
    private long                battleStartTime = 0;

    // ── Dialog ────────────────────────────────────────────────────────────────
    private final List<String> dialogLines = new ArrayList<>();
    private int      dialogIndex   = 0;
    private boolean  dialogVisible = false;
    private Runnable dialogOnClose = null;

    // =========================================================================
    // Constructor
    // =========================================================================
    public minibossroom() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        mapImg      = load("G10_map 2.png");
        playerUp    = load("G10_pBack.png");
        playerDown  = load("G10_pFront.png");
        playerLeft  = load("G10_pLeft.png");
        playerRight = load("G10_pRight.png");
        bossImg     = load("G10_Don Malek.png");

        currentPlayer = playerDown;
        generateCollision();

        saveData = SaveSystem.loadGame();
        if (saveData.isDefeated("Don Malek")) bossDefeated = true;
        if (!walkable[gridY][gridX]) gridY = 12;

        SwingUtilities.invokeLater(this::playIntroDialog);
        requestFocusInWindow();
    }

    // =========================================================================
    // Asset loading
    // =========================================================================
    BufferedImage load(String name) {
        try   { return ImageIO.read(new File("src/images/" + name)); }
        catch (Exception e) { System.out.println("Could not load: " + name); return null; }
    }

    // =========================================================================
    // Collision
    // =========================================================================
    void generateCollision() {
        if (mapImg == null) {
            for (int y = 0; y < ROWS; y++)
                for (int x = 0; x < COLS; x++)
                    walkable[y][x] = true;
            return;
        }
        int imgW = mapImg.getWidth(), imgH = mapImg.getHeight();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                int px = Math.max(0, Math.min((int)((x + 0.5) * imgW / COLS), imgW - 1));
                int py = Math.max(0, Math.min((int)((y + 0.5) * imgH / ROWS), imgH - 1));
                if (x == DOOR_X && y == DOOR_Y) { walkable[y][x] = true; continue; }
                Color c = new Color(mapImg.getRGB(px, py));
                boolean blocked = (c.getBlue() > 120 && c.getGreen() > 120)
                               || (c.getRed() < 70 && c.getGreen() < 70 && c.getBlue() < 70);
                walkable[y][x] = !blocked;
            }
        }
    }

    // =========================================================================
    // Movement
    // =========================================================================
    int px() { return gridX * TILE_W; }
    int py() { return gridY * TILE_H; }

    void move(int dx, int dy, BufferedImage dir) {
        if (dialogVisible || battleActive) return;
        int nx = gridX + dx, ny = gridY + dy;
        if (nx < 0 || ny < 0 || nx >= COLS || ny >= ROWS) return;
        if (!walkable[ny][nx]) return;
        gridX = nx; gridY = ny; currentPlayer = dir;

        if (gridX == DOOR_X && gridY == DOOR_Y) {
            if (bossDefeated) { enterDoor(); }
            else showDialog(new String[]{
                "The way is blocked.",
                "Don Malek: 'Going somewhere? We have unfinished business.'",
                "Defeat Don Malek to proceed."
            }, null);
            return;
        }

        if (gridX == BOSS_TRIGGER_X &&
    gridY == BOSS_TRIGGER_Y &&
    !bossDefeated) {
    triggerBossBattle();
    return;
}
        repaint();
    }

    // =========================================================================
    // Boss battle
    // =========================================================================
    private void triggerBossBattle() {
        battleActive    = true;
        battleStartTime = System.currentTimeMillis();
        Battle.paused   = true;

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        showDialog(new String[]{
            "Don Malek raises his glass.",
            "Don Malek: 'You shouldn\u2019t have come here.'",
            "Don Malek: 'Let\u2019s settle this properly.'"
        }, () -> {
            battle.start(frame, "src/images/G10_battleBG.png", "Don Malek");

            Timer poll = new Timer(300, null);
            poll.addActionListener(ev -> {
                if (!Battle.paused) { poll.stop(); battleActive = false; onBattleEnd(frame); }
            });
            poll.start();
        });
    }

    private void onBattleEnd(JFrame frame) {
        saveData = SaveSystem.loadGame();
        boolean won  = saveData.isDefeated("Don Malek");
        long elapsed = (System.currentTimeMillis() - battleStartTime) / 1000;

        if (won) {
            bossDefeated = true;
            List<String> battles = new ArrayList<>(saveData.battles);
            if (!battles.contains("Don Malek")) battles.add("Don Malek");
            SaveSystem.saveGame(new SaveSystem.SaveData.Builder("minibossroom")
                .position(gridY * COLS + gridX).flags(saveData.flags)
                .battles(battles).time(saveData.timeSeconds + elapsed).build());
            repaint();
            showDialog(new String[]{
                "Don Malek staggers. 'Not bad...'",
                "The door ahead has opened.",
                "Head through the door to face the final boss."
            }, () -> repaint());
        } else {
            gridX = SPAWN_X; gridY = SPAWN_Y; currentPlayer = playerDown;
            repaint();
            showDialog(new String[]{
                "You were defeated...",
                "Don Malek: 'Come back when you\u2019re stronger.'",
                "You\u2019ve been sent back to the entrance."
            }, () -> repaint());
        }
    }

    // =========================================================================
    // Door transition to BossRoom
    // =========================================================================
    void enterDoor() {
        List<String> battles = new ArrayList<>(saveData.battles);
        if (!battles.contains("Don Malek")) battles.add("Don Malek");
        SaveSystem.saveGame(new SaveSystem.SaveData.Builder("BossRoom")
            .position(0).flags(saveData.flags)
            .battles(battles).time(saveData.timeSeconds).build());

        JFrame current = (JFrame) SwingUtilities.getWindowAncestor(this);
        current.dispose();

        SwingUtilities.invokeLater(() -> {
            JFrame bossFrame = new JFrame("Boss Room \u2014 Ma\u2019am Kath");
            BossRoom bossRoom = new BossRoom();
            bossFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            bossFrame.add(bossRoom);
            bossFrame.pack();
            bossFrame.setLocationRelativeTo(null);
            bossFrame.setVisible(true);
            bossRoom.requestFocusInWindow();
        });
    }

    // =========================================================================
    // Dialog
    // =========================================================================
    private void showDialog(String[] lines, Runnable onClose) {
        dialogLines.clear();
        for (String l : lines) dialogLines.add(l);
        dialogIndex = 0; dialogOnClose = onClose; dialogVisible = true;
        repaint();
    }

    private void advanceDialog() {
        if (++dialogIndex >= dialogLines.size()) {
            dialogVisible = false; dialogLines.clear();
            Runnable cb = dialogOnClose; dialogOnClose = null;
            repaint(); if (cb != null) cb.run();
        } else { repaint(); }
    }

    private void playIntroDialog() {
        if (bossDefeated) showDialog(new String[]{
            "Don Malek has been defeated.",
            "The door to the final room is open.",
            "[Walk to the glowing door to proceed.]"
        }, null);
        else showDialog(new String[]{
            "You enter an unknown island...",
            "The smell of cigarette smoke sips through the air.",
            "Someone is waiting for you.",
            "[Arrow Keys to move. Approach the figure to start the fight.]"
        }, null);
    }

    // =========================================================================
    // Painting
    // =========================================================================
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mapImg != null) g2.drawImage(mapImg, 0, 0, WIDTH, HEIGHT, null);
        else { g2.setColor(new Color(20,15,35)); g2.fillRect(0,0,WIDTH,HEIGHT); }

        // Door
        if (bossDefeated) { g2.setColor(new Color(100,200,255,160)); g2.fillRect(DOOR_X*TILE_W,DOOR_Y*TILE_H,TILE_W,TILE_H); }
        g2.setColor(bossDefeated ? new Color(100,200,255) : new Color(120,60,60));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(DOOR_X*TILE_W, DOOR_Y*TILE_H, TILE_W, TILE_H);

        // Boss marker
        if (!bossDefeated) {
            if (bossImg != null) {
g2.drawImage(
    bossImg,
    BOSS_TILE_X * TILE_W,
    BOSS_TILE_Y * TILE_H,
    TILE_W * 3,
    TILE_H * 3,
    null
);            } else {
                g2.setColor(new Color(200,100,30,200));
                g2.fillRoundRect(BOSS_TILE_X*TILE_W+2, BOSS_TILE_Y*TILE_H+2, TILE_W-4, TILE_H-4, 6,6);
                g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,8));
                g2.drawString("DON", BOSS_TILE_X*TILE_W+2, BOSS_TILE_Y*TILE_H+TILE_H-4);
            }
            g2.setColor(new Color(200, 80, 30, 35));
g2.fillRect(BOSS_TRIGGER_X * TILE_W, BOSS_TRIGGER_Y * TILE_H, TILE_W, TILE_H);
        }

        // Player
        if (!battleActive) {
            if (currentPlayer != null) g2.drawImage(currentPlayer, px(), py(), TILE_W, TILE_H, null);
            else { g2.setColor(new Color(68,136,204)); g2.fillRect(px()+2,py()+2,TILE_W-4,TILE_H-4); }
        }

        // Dialog box
        if (dialogVisible && !dialogLines.isEmpty()) {
            int boxH=80, boxY=HEIGHT-boxH-10, pad=12;
            g2.setColor(new Color(0,0,0,210)); g2.fillRoundRect(pad,boxY,WIDTH-pad*2,boxH,10,10);
            g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(pad,boxY,WIDTH-pad*2,boxH,10,10);
            g2.setFont(new Font("Comic Sans MS",Font.BOLD,14)); g2.setColor(Color.WHITE);
            String line = dialogLines.get(dialogIndex);
            if (line.length() <= 62) { g2.drawString(line, pad+14, boxY+30); }
            else {
                int split = line.lastIndexOf(' ',62); if (split<0) split=62;
                g2.drawString(line.substring(0,split), pad+14, boxY+24);
                g2.drawString(line.substring(split+1), pad+14, boxY+46);
            }
            g2.setFont(new Font("Arial",Font.PLAIN,11)); g2.setColor(new Color(180,180,180));
            g2.drawString(dialogIndex<dialogLines.size()-1?"SPACE \u2014 next":"SPACE \u2014 close",
                          WIDTH-pad*2-90, boxY+boxH-8);
        }
    }

    // =========================================================================
    // Input
    // =========================================================================
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (dialogVisible) { if (k==KeyEvent.VK_SPACE||k==KeyEvent.VK_ENTER) advanceDialog(); return; }
        if (battleActive) return;
        if (e.getKeyCode() == KeyEvent.VK_UP) move(0, -1, playerUp);
if (e.getKeyCode() == KeyEvent.VK_DOWN) move(0, 1, playerDown);
if (e.getKeyCode() == KeyEvent.VK_LEFT) move(-1, 0, playerLeft);
if (e.getKeyCode() == KeyEvent.VK_RIGHT) move(1, 0, playerRight);
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e)    {}

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) {
        JFrame f = new JFrame("Mini Boss Room \u2014 Don Malek");
        minibossroom game = new minibossroom();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(game); f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
        game.requestFocusInWindow();
    }
    
    public static void openMinibossRoom() {
    SwingUtilities.invokeLater(() -> {
        JFrame f = new JFrame("Mini Boss Room");
        minibossroom game = new minibossroom();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(game);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        game.requestFocusInWindow();
    });
}
}


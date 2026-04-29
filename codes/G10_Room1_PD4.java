//RASONABE REDULLA VILLAROMAN


package codes;

import codes.Battle;
import codes.SaveSystem;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class G10_Room1_PD4 extends JPanel implements KeyListener {

    final int COLS   = 33;
    final int ROWS   = 20;
    final int WIDTH  = 660;
    final int HEIGHT = 660;
    final int TILE_W = WIDTH  / COLS;
    final int TILE_H = HEIGHT / ROWS;

    final int SPAWN_X        = 11;
    final int SPAWN_Y        = 14;
    final int DOOR_X         = 15;
    final int DOOR_Y         = 7;
    final int BOSS_TRIGGER_X = 15;
    final int BOSS_TRIGGER_Y = 8;
    final int BOSS_TILE_X    = 14;
    final int BOSS_TILE_Y    = 6;

    int gridX = SPAWN_X;
    int gridY = SPAWN_Y;

    private static final String PROJECT_ROOT = resolveRoot();

    private static String resolveRoot() {
        String cwd = System.getProperty("user.dir", "");
        if (new File(cwd, "docs").isDirectory()) return cwd;

        try {
            File loc = new File(
                G10_Room1_PD4.class.getProtectionDomain()
                                   .getCodeSource().getLocation().toURI());
            File dir = loc.isDirectory() ? loc : loc.getParentFile();
            for (int i = 0; i < 8 && dir != null; i++) {
                if (new File(dir, "docs").isDirectory()) return dir.getAbsolutePath();
                dir = dir.getParentFile();
            }
        } catch (URISyntaxException | SecurityException ex) {  }

        return cwd;
    }

    BufferedImage mapImg;
    BufferedImage playerUp, playerDown, playerLeft, playerRight;
    BufferedImage currentPlayer;
    BufferedImage bossImg;

    boolean[][] walkable = new boolean[ROWS][COLS];

    private final Battle battle       = new Battle();
    private boolean      battleActive  = false;
    private boolean      bossDefeated  = false;
    private String       savedUserDir  = null;

    private SaveSystem.SaveData saveData;
    private long                battleStartTime = 0;

    private final List<String> dialogLines = new ArrayList<>();
    private int      dialogIndex   = 0;
    private boolean  dialogVisible = false;
    private Runnable dialogOnClose = null;

    private boolean  transitioning  = false;
    private float    transAlpha     = 0f;
    private Timer    transTimer     = null;
    private Runnable transOnComplete = null;

    private Clip overworldClip = null;
    private Clip battleClip    = null;

    public G10_Room1_PD4() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        fixStatsFilePath();

        mapImg      = load("G10_map 2.png");
        playerUp    = load("G10_pBack.png");
        playerDown  = load("G10_pFront.png");
        playerLeft  = load("G10_pLeft.png");
        playerRight = load("G10_pRight.png");
        bossImg     = load("G10_Don Malek.png");

        currentPlayer = playerDown;
        generateCollision();
        walkable[SPAWN_Y][SPAWN_X] = true;

        loadSaveData();
        saveProgress();

        SwingUtilities.invokeLater(() -> {
            startOverworldMusic();
            playIntroDialog();
        });
        requestFocusInWindow();
    }
    
    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G10_Room1_PD4");

        if (save.timeSeconds <= 0) {
            SaveSystem.SaveData pd4Save = SaveSystem.loadGame("G9_Room2_PD6");
            SaveSystem.startTimer(pd4Save.timeSeconds);
        } else {
            SaveSystem.startTimer(save.timeSeconds);
        }
    }

    // =========================================================================
    // Save integration — save
    // =========================================================================
    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G10_Room1_PD4")
                .battles(SaveSystem.getDefeatedBosses())
        );
    }

    private Clip loadClip(String filename) {
        String[] bases = {
            PROJECT_ROOT + "/src/music/",
            PROJECT_ROOT + "/music/",
            PROJECT_ROOT + "/docs/",
            PROJECT_ROOT + "/audio/",
            PROJECT_ROOT + "/sounds/",
            PROJECT_ROOT + "/",
            System.getProperty("user.dir") + "/src/music/",
            System.getProperty("user.dir") + "/music/",
            System.getProperty("user.dir") + "/docs/",
            System.getProperty("user.dir") + "/audio/",
        };
        for (String base : bases) {
            File f = new File(base + filename);
            if (f.exists()) {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    return clip;
                } catch (Exception ex) {
                    System.out.println("[PD4] Could not load audio '" + filename + "': " + ex.getMessage());
                }
            }
        }
        System.out.println("[PD4] Audio file not found: " + filename);
        return null;
    }

    private void stopClip(Clip clip) {
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
        }
    }

    private void startOverworldMusic() {
        stopClip(battleClip);
        battleClip = null;
        stopClip(overworldClip);
        overworldClip = loadClip("DONMALEK.wav");
        if (overworldClip != null) {
            overworldClip.loop(Clip.LOOP_CONTINUOUSLY);
            overworldClip.start();
        }
    }

    private void startBattleMusic() {
        stopClip(overworldClip);
        overworldClip = null;
        stopClip(battleClip);
        battleClip = loadClip("DONMALEK-boss.wav");
        if (battleClip != null) {
            battleClip.loop(Clip.LOOP_CONTINUOUSLY);
            battleClip.start();
        }
    }

    public void stopAllMusic() {
        stopClip(overworldClip);
        stopClip(battleClip);
        overworldClip = null;
        battleClip    = null;
    }

    private void fadeOut(Runnable onComplete) {
        if (transitioning) return;
        transitioning   = true;
        transAlpha      = 0f;
        transOnComplete = onComplete;

        transTimer = new Timer(16, null);
        transTimer.addActionListener(e -> {
            transAlpha += 0.05f;
            repaint();
            if (transAlpha >= 1f) {
                transAlpha = 1f;
                transTimer.stop();
                repaint();

                Timer pause = new Timer(150, ev -> {
                    Runnable cb = transOnComplete;
                    transOnComplete = null;
                    if (cb != null) cb.run();
                });
                pause.setRepeats(false);
                pause.start();
            }
        });
        transTimer.start();
    }

    private void fadeIn() {
        transAlpha = 1f;
        transitioning = true;
        transTimer = new Timer(16, null);
        transTimer.addActionListener(e -> {
            transAlpha -= 0.05f;
            repaint();
            if (transAlpha <= 0f) {
                transAlpha    = 0f;
                transitioning = false;
                transTimer.stop();
                repaint();
            }
        });
        transTimer.start();
    }

    private void fixStatsFilePath() {
        String[] candidates = {
            PROJECT_ROOT + "/docs/battleStats.txt",
            PROJECT_ROOT + "/src/docs/battleStats.txt",
            PROJECT_ROOT + "/battleStats.txt",
            System.getProperty("user.dir") + "/docs/battleStats.txt",
        };
        File found = null;
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists() && found == null) found = f;
        }
        if (found == null) { System.out.println("[PD4] battleStats.txt not found!"); return; }
        try {
            java.lang.reflect.Field f = battle.getClass().getDeclaredField("statsFile");
            f.setAccessible(true);
            f.set(battle, found);
        } catch (Exception ex) {
            System.out.println("[PD4] statsFile reflection failed: " + ex.getMessage());
        }
    }

    private void fixBattleBackground(JFrame frame) {
        swapImageField(frame, "backgroundImage", "G10_battleBG.png");
        swapImageField(frame, "enemyBattle",     "G10_Don Malek1.png");
        swapImageField(frame, "playerBattle",    "G10_playerbattle.png");
        frame.getLayeredPane().repaint();
    }

    private void swapImageField(JFrame frame, String fieldName, String filename) {
        String[] bases = {
            PROJECT_ROOT + "/images/",
            PROJECT_ROOT + "/src/images/",
            PROJECT_ROOT + "/images/map2/",
            System.getProperty("user.dir") + "/images/",
        };
        ImageIcon icon = null;
        for (String base : bases) {
            File f = new File(base + filename);
            if (f.exists() && icon == null) icon = new ImageIcon(f.getAbsolutePath());
        }
        if (icon == null) { System.out.println("[PD4] Image not found: " + filename); return; }
        try {
            java.lang.reflect.Field field = battle.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(battle, icon);
        } catch (Exception ex) {
            System.out.println("[PD4] " + fieldName + " reflection failed: " + ex.getMessage());
        }
    }

    BufferedImage load(String name) {
        String[] bases = {
            PROJECT_ROOT + "/images/",
            PROJECT_ROOT + "/src/images/",
            System.getProperty("user.dir") + "/images/",
        };
        for (String base : bases) {
            File f = new File(base + name);
            if (f.exists()) {
                try { return ImageIO.read(f); } catch (Exception ignored) {}
            }
        }
        try {
            var is = getClass().getClassLoader().getResourceAsStream("images/" + name);
            if (is != null) return ImageIO.read(is);
        } catch (Exception ignored) {}
        System.out.println("[PD4] Could not load sprite: " + name);
        return null;
    }

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
                Color c       = new Color(mapImg.getRGB(px, py));
                boolean blocked = (c.getBlue() > 120 && c.getGreen() > 120)
                               || (c.getRed() < 70 && c.getGreen() < 70 && c.getBlue() < 70);
                walkable[y][x] = !blocked;
            }
        }
    }

    int px() { return gridX * TILE_W; }
    int py() { return gridY * TILE_H; }

    void move(int dx, int dy, BufferedImage dir) {
        if (dialogVisible || battleActive || transitioning) return;
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
        if (gridX == BOSS_TRIGGER_X && gridY == BOSS_TRIGGER_Y && !bossDefeated) {
            triggerBossBattle();
            return;
        }
        repaint();
    }

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

            startBattleMusic();

            savedUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", PROJECT_ROOT);

            battle.start(frame, "images/G10_battleBG.png", "Don Malek");
            fixBattleBackground(frame);

            Timer poll = new Timer(300, null);
            poll.addActionListener(ev -> {
                if (!Battle.paused) {
                    poll.stop();
                    battleActive = false;
                    onBattleEnd(frame);
                }
            });
            poll.start();
        });
    }

    private boolean didPlayerWin() {
        try {
            java.lang.reflect.Field f = battle.getClass().getDeclaredField("hp");
            f.setAccessible(true);
            return (int) f.get(battle) > 0;
        } catch (Exception ex) { return false; }
    }

    private void onBattleEnd(JFrame frame) {
        if (savedUserDir != null) {
            System.setProperty("user.dir", savedUserDir);
            savedUserDir = null;
        }

        startOverworldMusic();

        long    elapsed = (System.currentTimeMillis() - battleStartTime) / 1000;
        boolean won     = didPlayerWin();

        restoreFocus(frame);

        if (won) {
            bossDefeated = true;

            saveData = SaveSystem.loadGame("G10_Room1_PD4");
            List<String> battles = new ArrayList<>(saveData.battles);
            if (!battles.contains("Don Malek")) battles.add("Don Malek");

            SaveSystem.saveGame(new SaveSystem.SaveData.Builder("G10_Room1_PD4")
                .flags(saveData.flags)
                .battles(battles)
                .time(saveData.timeSeconds + elapsed)
                .build());

            saveData = SaveSystem.loadGame("G10_Room1_PD4");
            repaint();

            showDialog(new String[]{
                "Don Malek staggers back.",
                "Don Malek: 'Not bad\u2026 not bad at all.'",
                "The door ahead glows. The final room awaits.",
                "[Walk to the glowing door to face Ma\u2019am Kath.]"
            }, () -> repaint());

        } else {
            gridX = SPAWN_X;
            gridY = SPAWN_Y;
            currentPlayer = playerDown;
            walkable[SPAWN_Y][SPAWN_X] = true;
            repaint();

            showDialog(new String[]{
                "You were defeated\u2026",
                "Don Malek: 'Come back when you\u2019re stronger.'",
                "You\u2019ve been sent back to the entrance."
            }, () -> repaint());
        }
    }

    private void restoreFocus(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            frame.requestFocus();
            setFocusable(true);
            requestFocusInWindow();
        });
    }

    void enterDoor() {

        fadeOut(() -> {
            stopAllMusic();

            List<String> battles = new ArrayList<>(saveData.battles);
            if (!battles.contains("Don Malek")) battles.add("Don Malek");

            SaveSystem.saveGame(new SaveSystem.SaveData.Builder("G10_Room2_PD6")
                .flags(saveData.flags)
                .battles(battles)
                .time(saveData.timeSeconds)
                .build());

            JFrame current = (JFrame) SwingUtilities.getWindowAncestor(this);
            current.dispose();

            SwingUtilities.invokeLater(() -> {
                JFrame bossFrame = new JFrame("Boss Room \u2014 Ma\u2019am Kath");
                G10_Room2_PD6 bossRoom = new G10_Room2_PD6();
                bossFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                bossFrame.add(bossRoom);
                bossFrame.pack();
                bossFrame.setLocationRelativeTo(null);
                bossFrame.setVisible(true);
                bossRoom.requestFocusInWindow();

            });
        });
    }

    private void showDialog(String[] lines, Runnable onClose) {
        dialogLines.clear();
        for (String l : lines) dialogLines.add(l);
        dialogIndex   = 0;
        dialogOnClose = onClose;
        dialogVisible = true;
        repaint();
    }

    private void advanceDialog() {
        if (++dialogIndex >= dialogLines.size()) {
            dialogVisible = false;
            dialogLines.clear();
            Runnable cb = dialogOnClose;
            dialogOnClose = null;
            repaint();
            if (cb != null) cb.run();
        } else {
            repaint();
        }
    }

    private void playIntroDialog() {
        if (bossDefeated) showDialog(new String[]{
            "Don Malek has been defeated.",
            "The door to the final room is open.",
            "[Walk to the glowing door to proceed.]"
        }, null);
        else showDialog(new String[]{
            "You enter an unknown island\u2026",
            "The smell of cigarette smoke seeps through the air.",
            "Someone is waiting for you.",
            "[WASD to move. Approach the figure to start the fight.]"
        }, null);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mapImg != null) g2.drawImage(mapImg, 0, 0, WIDTH, HEIGHT, null);
        else { g2.setColor(new Color(20, 15, 35)); g2.fillRect(0, 0, WIDTH, HEIGHT); }

        if (bossDefeated) {
            g2.setColor(new Color(100, 200, 255, 160));
            g2.fillRect(DOOR_X * TILE_W, DOOR_Y * TILE_H, TILE_W, TILE_H);
        }
        g2.setColor(bossDefeated ? new Color(100, 200, 255) : new Color(120, 60, 60));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(DOOR_X * TILE_W, DOOR_Y * TILE_H, TILE_W, TILE_H);

        if (!bossDefeated) {
            if (bossImg != null) {
                g2.drawImage(bossImg, BOSS_TILE_X * TILE_W, BOSS_TILE_Y * TILE_H,
                             TILE_W * 3, TILE_H * 3, null);
            } else {
                g2.setColor(new Color(200, 100, 30, 200));
                g2.fillRoundRect(BOSS_TILE_X * TILE_W + 2, BOSS_TILE_Y * TILE_H + 2,
                                 TILE_W - 4, TILE_H - 4, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 8));
                g2.drawString("DON", BOSS_TILE_X * TILE_W + 2, BOSS_TILE_Y * TILE_H + TILE_H - 4);
            }
            g2.setColor(new Color(200, 80, 30, 35));
            g2.fillRect(BOSS_TRIGGER_X * TILE_W, BOSS_TRIGGER_Y * TILE_H, TILE_W, TILE_H);
        }

        if (!battleActive) {
            if (currentPlayer != null)
                g2.drawImage(currentPlayer, px(), py(), TILE_W, TILE_H, null);
            else {
                g2.setColor(new Color(68, 136, 204));
                g2.fillRect(px() + 2, py() + 2, TILE_W - 4, TILE_H - 4);
            }
        }

        if (dialogVisible && !dialogLines.isEmpty()) {
            int boxH = 80, boxY = HEIGHT - boxH - 10, pad = 12;
            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(pad, boxY, WIDTH - pad * 2, boxH, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(pad, boxY, WIDTH - pad * 2, boxH, 10, 10);
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
            g2.setColor(Color.WHITE);
            String line = dialogLines.get(dialogIndex);
            if (line.length() <= 62) {
                g2.drawString(line, pad + 14, boxY + 30);
            } else {
                int split = line.lastIndexOf(' ', 62);
                if (split < 0) split = 62;
                g2.drawString(line.substring(0, split),       pad + 14, boxY + 24);
                g2.drawString(line.substring(split + 1).trim(), pad + 14, boxY + 46);
            }
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(180, 180, 180));
            g2.drawString(dialogIndex < dialogLines.size() - 1
                    ? "SPACE \u2014 next" : "SPACE \u2014 close",
                    WIDTH - pad * 2 - 90, boxY + boxH - 8);
        }

        if (transAlpha > 0f) {
            int a = Math.min(255, (int)(transAlpha * 255));
            g2.setColor(new Color(0, 0, 0, a));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (dialogVisible) {
            if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_ENTER) advanceDialog();
            return;
        }
        if (battleActive || transitioning) return;
        if (k == KeyEvent.VK_W) move( 0, -1, playerUp);
        if (k == KeyEvent.VK_S) move( 0,  1, playerDown);
        if (k == KeyEvent.VK_A) move(-1,  0, playerLeft);
        if (k == KeyEvent.VK_D) move( 1,  0, playerRight);
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    public static void openRoom() {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Mini Boss Room \u2014 Don Malek");
            G10_Room1_PD4 room = new G10_Room1_PD4();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(room);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            room.requestFocusInWindow();
        });
    }

    public static void main(String[] args) {
        openRoom();
    }
}

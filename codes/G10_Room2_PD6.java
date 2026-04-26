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

public class G10_Room2_PD6 extends JPanel implements KeyListener {

    final int COLS   = 25;
    final int ROWS   = 12;
    final int WIDTH  = 660;
    final int HEIGHT = 660;
    final int TILE_W = WIDTH  / COLS;
    final int TILE_H = HEIGHT / ROWS;

    static final int SPAWN_X        = 8;
    static final int SPAWN_Y        = 9;
    static final int SIR_JIM_X      = 21;
    static final int SIR_JIM_Y      = 3;
    static final int KATH_DRAW_X    = 16;
    static final int KATH_DRAW_Y    = 3;
    static final int DOOR_X         = 16;
    static final int DOOR_Y         = 5;
    static final int BOSS_TRIGGER_X = 16;
    static final int BOSS_TRIGGER_Y = 5;
    static final int LEVER_X        = 2;
    static final int LEVER_Y        = 10;

    private static final int[][] BASE_MAP = {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
        { 0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0 },
        { 0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0 },
        { 0,1,0,1,1,1,1,0,1,1,0,0,0,0,0,0,1,1,1,1,0,1,0,1,0 },
        { 0,1,0,1,0,0,0,0,1,1,1,0,0,1,1,0,1,1,1,1,1,1,0,1,0 },
        { 0,1,0,1,0,0,0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,0 },
        { 0,1,0,1,1,1,1,0,1,1,1,0,1,1,0,0,1,0,0,0,0,0,0,1,0 },
        { 0,1,0,0,0,0,1,1,1,1,1,1,1,1,0,1,1,0,0,0,1,1,1,1,0 },
        { 0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0 },
        { 0,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,0,1,0 },
        { 0,1,1,0,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0 },
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    };

    int gridX = SPAWN_X;
    int gridY = SPAWN_Y;

    private static final String PROJECT_ROOT = resolveRoot();

    private static String resolveRoot() {
        String cwd = System.getProperty("user.dir", "");
        if (new File(cwd, "docs").isDirectory()) return cwd;
        try {
            File loc = new File(
                G10_Room2_PD6.class.getProtectionDomain()
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
    BufferedImage kathImg;
    BufferedImage jimImg;

    boolean[][] walkable = new boolean[ROWS][COLS];

    boolean leverPulled  = false;
    boolean bossDefeated = false;

    private float doorGlowAlpha = 0f;
    private float doorGlowDelta = 0.04f;
    private Timer doorGlowTimer = null;

    private final Battle battle          = new Battle();
    private boolean      battleActive    = false;
    private String       savedUserDir    = null;
    private long         battleStartTime = 0;

    private SaveSystem.SaveData saveData;

    private final List<String> dialogLines = new ArrayList<>();
    private int      dialogIndex   = 0;
    private boolean  dialogVisible = false;
    private Runnable dialogOnClose = null;

    private float transAlpha    = 1f;
    private boolean fadingIn    = false;
    private Timer   fadeInTimer = null;

    private Clip overworldClip = null;
    private Clip battleClip    = null;

    public G10_Room2_PD6() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        fixStatsFilePath();

        mapImg      = load("G10_map 1.png");
        playerUp    = load("G10_pBack.png");
        playerDown  = load("G10_pFront.png");
        playerLeft  = load("G10_pLeft.png");
        playerRight = load("G10_pRight.png");
        kathImg     = load("G10_maamkath.png");
        jimImg      = load("G10_sirjim.png");

        currentPlayer = playerDown;

        saveData = SaveSystem.loadGame("G10_Room2_PD6");
        if (saveData.isDefeated("Ma'am Kath")) bossDefeated = true;
        if (saveData.hasFlag("lever_pulled"))   leverPulled  = true;

        buildWalkable();
        if (leverPulled && !bossDefeated) startDoorGlow();

        SwingUtilities.invokeLater(() -> {
            startFadeIn();
            startOverworldMusic();
            playIntroDialog();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
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
                    System.out.println("[PD6] Could not load audio '" + filename + "': " + ex.getMessage());
                }
            }
        }
        System.out.println("[PD6] Audio file not found: " + filename);
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
        overworldClip = loadClip("FinalBossPhase1.wav");
        if (overworldClip != null) {
            overworldClip.loop(Clip.LOOP_CONTINUOUSLY);
            overworldClip.start();
        }
    }

    private void startBattleMusic() {
        stopClip(overworldClip);
        overworldClip = null;
        stopClip(battleClip);
        battleClip = loadClip("FinalBossPhase2.wav");
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

    private void startFadeIn() {
        transAlpha = 1f;
        fadingIn   = true;
        fadeInTimer = new Timer(16, null);
        fadeInTimer.addActionListener(e -> {
            transAlpha -= 0.04f;
            repaint();
            if (transAlpha <= 0f) {
                transAlpha = 0f;
                fadingIn   = false;
                fadeInTimer.stop();
                repaint();
            }
        });
        fadeInTimer.start();
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
        System.out.println("[PD6] Could not load: " + name);
        return null;
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
        if (found == null) { System.out.println("[PD6] battleStats.txt not found!"); return; }
        try {
            java.lang.reflect.Field f = battle.getClass().getDeclaredField("statsFile");
            f.setAccessible(true);
            f.set(battle, found);
        } catch (Exception ex) {
            System.out.println("[PD6] statsFile reflection failed: " + ex.getMessage());
        }
    }

    private void fixBattleSprites(JFrame frame) {
        swapImageField(frame, "backgroundImage", "G10_battleBG2.png");
        swapImageField(frame, "playerBattle",    "G10_playerbattle.png");
        swapImageField(frame, "enemyBattle",     "G10_maamkath.png");
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
        if (icon == null) { System.out.println("[PD6] Image not found: " + filename); return; }
        try {
            java.lang.reflect.Field field = battle.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(battle, icon);
        } catch (Exception ex) {
            System.out.println("[PD6] " + fieldName + " reflection failed: " + ex.getMessage());
        }
    }

    void buildWalkable() {
        for (int y = 0; y < ROWS; y++)
            for (int x = 0; x < COLS; x++)
                walkable[y][x] = (BASE_MAP[y][x] == 1);

        walkable[SPAWN_Y][SPAWN_X]      = true;
        walkable[LEVER_Y][LEVER_X]      = true;
        walkable[SIR_JIM_Y][SIR_JIM_X] = true;
        walkable[DOOR_Y][DOOR_X]                 = leverPulled;
        walkable[BOSS_TRIGGER_Y][BOSS_TRIGGER_X] = leverPulled;

        for (int x = 17; x <= 21; x++) {
            walkable[3][x] = true;
            walkable[4][x] = true;
        }
    }

    private void openDoor() {
        walkable[DOOR_Y][DOOR_X]                 = true;
        walkable[BOSS_TRIGGER_Y][BOSS_TRIGGER_X] = true;
    }

    private void startDoorGlow() {
        if (doorGlowTimer != null && doorGlowTimer.isRunning()) return;
        doorGlowTimer = new Timer(40, e -> {
            doorGlowAlpha += doorGlowDelta;
            if (doorGlowAlpha >= 1f) { doorGlowAlpha = 1f; doorGlowDelta = -Math.abs(doorGlowDelta); }
            if (doorGlowAlpha <= 0f) { doorGlowAlpha = 0f; doorGlowDelta =  Math.abs(doorGlowDelta); }
            repaint();
        });
        doorGlowTimer.start();
    }

    private void stopDoorGlow() {
        if (doorGlowTimer != null) { doorGlowTimer.stop(); doorGlowTimer = null; }
        doorGlowAlpha = 0f;
    }

    int px() { return gridX * TILE_W; }
    int py() { return gridY * TILE_H; }

    void move(int dx, int dy, BufferedImage dir) {
        if (dialogVisible || battleActive || fadingIn) return;
        int nx = gridX + dx, ny = gridY + dy;
        if (nx < 0 || ny < 0 || nx >= COLS || ny >= ROWS) return;
        if (!walkable[ny][nx]) return;
        gridX = nx; gridY = ny; currentPlayer = dir;

        if (gridX == LEVER_X && gridY == LEVER_Y && !leverPulled) {
            activateLever();
            return;
        }
        if (!bossDefeated && leverPulled
                && gridX == BOSS_TRIGGER_X && gridY == BOSS_TRIGGER_Y) {
            triggerBossBattle();
            return;
        }
        if (gridX == SIR_JIM_X && gridY == SIR_JIM_Y) {
            if (bossDefeated) {
                showDialog(new String[]{
                    "Sir Jim: 'You did it! I knew you could!'",
                    "Sir Jim: 'She\u2019s been holding me here for weeks\u2026'",
                    "Sir Jim: 'You have my eternal gratitude. Now let\u2019s get out of here!'"
                }, () -> launchCredits());
            } else {
                showDialog(new String[]{
                    "Sir Jim whispers: 'Shhh! Keep your voice down!'",
                    "Sir Jim: 'Ma\u2019am Kath is right there \u2014 you have to defeat her!'",
                    "Sir Jim: 'Please\u2026 I\u2019m counting on you.'"
                }, null);
            }
            return;
        }
        repaint();
    }

    private void activateLever() {
        leverPulled = true;
        openDoor();

        saveData = SaveSystem.loadGame("G10_Room2_PD6");
        List<String> flags = new ArrayList<>(saveData.flags);
        if (!flags.contains("lever_pulled")) flags.add("lever_pulled");

        SaveSystem.saveGame(new SaveSystem.SaveData.Builder("G10_Room2_PD6")
            .flags(flags)
            .battles(saveData.battles)
            .time(saveData.timeSeconds)
            .build());
        saveData = SaveSystem.loadGame("G10_Room2_PD6");

        startDoorGlow();
        repaint();

        showDialog(new String[]{
            "You pull the lever.",
            "A deep CLICK echoes through the stone walls.",
            "The crate blocking the upper-right passage has shifted!",
            "[Head north through the glowing gap in the crates to face Ma\u2019am Kath.]"
        }, null);
    }

    private void triggerBossBattle() {
        battleActive    = true;
        battleStartTime = System.currentTimeMillis();
        Battle.paused   = true;

        stopDoorGlow();

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

        showDialog(new String[]{
            "Ma\u2019am Kath watches you with quiet, terrifying authority.",
            "Ma\u2019am Kath: \u2018I have graded everything here. You are no exception.\u2019",
            "The final exam begins."
        }, () -> {

            startBattleMusic();

            savedUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", PROJECT_ROOT);

            battle.start(frame, "images/G10_battleBG2", "Ma'am Kath");
            fixBattleSprites(frame);

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
            saveData = SaveSystem.loadGame("G10_Room2_PD6");
            List<String> battles = new ArrayList<>(saveData.battles);
            if (!battles.contains("Ma'am Kath")) battles.add("Ma'am Kath");
            List<String> flags = new ArrayList<>(saveData.flags);
            if (leverPulled && !flags.contains("lever_pulled")) flags.add("lever_pulled");

            SaveSystem.saveGame(new SaveSystem.SaveData.Builder("G10_Room2_PD6")
                .flags(flags)
                .battles(battles)
                .time(saveData.timeSeconds + elapsed)
                .build());
            saveData = SaveSystem.loadGame("G10_Room2_PD6");
            repaint();
            showDialog(new String[]{
                "Ma\u2019am Kath closes her grade book slowly.",
                "Ma\u2019am Kath: \u2018\u2026Not bad. You pass.\u2019",
                "Congratulations! You have defeated the final boss!",
                "[Speak to Sir Jim in the upper-right corner.]"
            }, null);
        } else {
            startDoorGlow();
            gridX = SPAWN_X; gridY = SPAWN_Y; currentPlayer = playerDown;
            repaint();
            showDialog(new String[]{
                "Ma\u2019am Kath: \u2018You still have much to learn.\u2019",
                "You\u2019ve been sent back to the entrance.",
                "Gather your strength and try again."
            }, null);
        }
    }

    private void launchCredits() {
        transAlpha = 0f;
        fadingIn   = false;
        Timer fadeOut = new Timer(16, null);
        fadeOut.addActionListener(e -> {
            transAlpha += 0.05f;
            repaint();
            if (transAlpha >= 1f) {
                transAlpha = 1f;
                ((Timer) e.getSource()).stop();
                repaint();
                Timer pause = new Timer(200, ev -> {
                    stopAllMusic();
                    JFrame current = (JFrame) SwingUtilities.getWindowAncestor(this);
                    if (current != null) current.dispose();
                    SwingUtilities.invokeLater(() -> {
                        JFrame creditsFrame = new JFrame("TOGETHER: Project Guanine");
                        credits creditsScene = new credits();
                        creditsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        creditsFrame.setSize(660, 660);
                        creditsFrame.setLocationRelativeTo(null);
                        creditsFrame.add(creditsScene);
                        creditsFrame.setVisible(true);
                        creditsScene.requestFocusInWindow();
                    });
                });
                pause.setRepeats(false);
                pause.start();
            }
        });
        fadeOut.start();
    }

    private void restoreFocus(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            frame.requestFocus();
            setFocusable(true);
            requestFocusInWindow();
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
        if (bossDefeated) {
            showDialog(new String[]{
                "You have already defeated Ma\u2019am Kath.",
                "Sir Jim is waiting for you in the upper-right corner."
            }, null);
        } else {
            showDialog(new String[]{
                "You enter the final chamber.",
                "The air is heavy with silence.",
                leverPulled
                    ? "[The crate passage is open. Head to the upper-right to face Ma\u2019am Kath.]"
                    : "[WASD / Arrow Keys to move. Pull the lever in the bottom-left to open the crate passage.]"
            }, null);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mapImg != null) g2.drawImage(mapImg, 0, 0, WIDTH, HEIGHT, null);
        else { g2.setColor(new Color(30, 20, 20)); g2.fillRect(0, 0, WIDTH, HEIGHT); }

        if (leverPulled && !bossDefeated) {
            int gx = DOOR_X * TILE_W, gy = DOOR_Y * TILE_H;
            g2.setColor(new Color(255, 220, 60, Math.min(255, (int)(doorGlowAlpha * 80))));
            g2.fillRoundRect(gx - 6, gy - 6, TILE_W + 12, TILE_H + 12, 10, 10);
            g2.setColor(new Color(255, 200, 0, Math.min(255, (int)(doorGlowAlpha * 160))));
            g2.fillRoundRect(gx - 2, gy - 2, TILE_W + 4, TILE_H + 4, 6, 6);
            g2.setColor(new Color(255, 255, 120, Math.min(255, (int)(doorGlowAlpha * 220))));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(gx - 2, gy - 2, TILE_W + 4, TILE_H + 4, 6, 6);
        }

        if (!leverPulled) {
            int lx = LEVER_X * TILE_W + TILE_W / 2 - 4;
            int ly = LEVER_Y * TILE_H + 5;
            g2.setColor(new Color(60, 230, 60, 230));
            g2.fillOval(lx, ly, 9, 9);
            g2.setColor(new Color(0, 130, 0, 230));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(lx, ly, 9, 9);
        }

        if (!bossDefeated) {
            int kx = KATH_DRAW_X * TILE_W, ky = KATH_DRAW_Y * TILE_H;
            if (kathImg != null) {
                g2.drawImage(kathImg, kx, ky, TILE_W * 3, TILE_H * 2, null);
            } else {
                g2.setColor(new Color(200, 30, 30, 210));
                g2.fillRoundRect(kx + 2, ky + 2, TILE_W * 3 - 4, TILE_H * 2 - 4, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                g2.drawString("MA'AM KATH", kx + 6, ky + TILE_H + 6);
            }
        }

        {
            int jx = SIR_JIM_X * TILE_W, jy = SIR_JIM_Y * TILE_H;
            if (jimImg != null) {
                g2.drawImage(jimImg, jx, jy, TILE_W, TILE_H, null);
            } else {
                g2.setColor(new Color(30, 80, 200, 210));
                g2.fillRoundRect(jx + 2, jy + 2, TILE_W - 4, TILE_H - 4, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 8));
                g2.drawString("JIM", jx + 4, jy + TILE_H - 5);
            }
        }

        if (!battleActive) {
            if (currentPlayer != null) {
                g2.drawImage(currentPlayer, px(), py(), TILE_W, TILE_H, null);
            } else {
                g2.setColor(new Color(68, 136, 204));
                g2.fillRect(px() + 2, py() + 2, TILE_W - 4, TILE_H - 4);
            }
        }

        if (dialogVisible && !dialogLines.isEmpty()) {
            int pad = 12, boxH = 80, boxY = HEIGHT - boxH - 10;
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
                g2.drawString(line.substring(0, split),         pad + 14, boxY + 24);
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
        if (battleActive || fadingIn) return;
        switch (k) {
            case KeyEvent.VK_W  -> move( 0, -1, playerUp);
            case KeyEvent.VK_S  -> move( 0,  1, playerDown);
            case KeyEvent.VK_A  -> move(-1,  0, playerLeft);
            case KeyEvent.VK_D -> move( 1,  0, playerRight);
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    public static void main(String[] args) {
        JFrame f = new JFrame("Gambler\u2019s Hall");
        G10_Room2_PD6 room = new G10_Room2_PD6();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(room);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}

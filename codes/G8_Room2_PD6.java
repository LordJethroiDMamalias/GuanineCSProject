package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

// ── MapEntity ─────────────────────────────────────────────────────────────────
abstract class MapEntity {
    private int position;
    public MapEntity(int position)   { this.position = position; }
    public int  getPosition()        { return position; }
    public void setPosition(int pos) { this.position = pos; }
    public abstract void onInteract(Dialog dialog, JLayeredPane lp, int w, int h);
}

// ── BushEvent ─────────────────────────────────────────────────────────────────
class BushEvent extends MapEntity {
    public BushEvent(int position) { super(position); }

    @Override
    public void onInteract(Dialog dialog, JLayeredPane lp, int w, int h) {
        dialog.show(lp,
            new String[]{ "[You searched the wires... nothing but cords.]" },
            null, null, w, h);
    }
}

// ── Monster ───────────────────────────────────────────────────────────────────
class Monster extends MapEntity {
    private ImageIcon icon;

    public Monster(int position, int tw, int th) {
        super(position);
        icon = loadSafeIcon("images/G8_Gigglebot3000.png", tw, th);
    }

    private ImageIcon loadSafeIcon(String path, int w, int h) {
        try {
            ImageIcon tempIcon = new ImageIcon(path);
            if (tempIcon.getIconWidth() == -1) {
                System.err.println("Warning: Monster Image not found -> " + path);
                if (path.contains("G8_")) {
                    String fallback = path.replace("G8_", "");
                    tempIcon = new ImageIcon(fallback);
                }
            }
            if (tempIcon.getIconWidth() == -1) return null;
            return new ImageIcon(tempIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    public ImageIcon getIcon() { return icon; }

    public void onCatch(Dialog dialog, JLayeredPane lp, int w, int h,
                        Timer monsterTimer, Timer gameTimer,
                        Runnable onRestart) {
        if (monsterTimer != null && monsterTimer.isRunning()) monsterTimer.stop();
        if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();
        
        dialog.show(lp,
            new String[]{
                "CAUGHT!",
                "The Gigglebot was too fast for you...",
                "GAME OVER — restarting room."
            },
            null, null, w, h,
            onRestart);
    }

    @Override
    public void onInteract(Dialog dialog, JLayeredPane lp, int w, int h) {}
}

// ── G8_Room2_PD6 ──────────────────────────────────────────────────────────────
public class G8_Room2_PD6 implements KeyListener {

    // ── UI ────────────────────────────────────────────────────────────────────
    JFrame       frame;
    JLayeredPane layeredPane;
    G8_Dialog       dialog = new G8_Dialog();
    Battle       battle = new Battle();

    JLabel[] characterLabels;
    JLabel[] monsterLabels; 
    int mapWidth = 11, mapHeight = 11;
    int frameWidth = 660, frameHeight = 660;
    public int[] mapLayout;
    public int   characterPosition;

    int walkFrame = 0;
    int direction = 1;   // 0=up 1=down 2=left 3=right

    ImageIcon pUp1, pUp2, pDown1, pDown2, pLeft1, pLeft2, pRight1, pRight2, BGimage;

    Timer   monsterTimer, gameTimer;
    Monster enemy;
    int     survivalTime = 30;
    JLabel  timerLabel;

    boolean battleTriggered = false; 

    // =========================================================================
    // Constructor
    // =========================================================================
    public G8_Room2_PD6() {
        frame = new JFrame("PD6 - Gigglebot Battle");
        int tw = frameWidth  / mapWidth;
        int th = frameHeight / mapHeight;

        BGimage  = loadSafeIcon("images/G8_PD6BG.png",    frameWidth, frameHeight);
        pDown1   = loadSafeIcon("images/G8_down1.png",    tw, th);
        pDown2   = loadSafeIcon("images/G8_down2.png",    tw, th);
        pUp1     = loadSafeIcon("images/G8_up1.png",      tw, th);
        pUp2     = loadSafeIcon("images/G8_up2.png",      tw, th);
        pLeft1   = loadSafeIcon("images/G8_left1.png",    tw, th);
        pLeft2   = loadSafeIcon("images/G8_left2.png",    tw, th);
        pRight1  = loadSafeIcon("images/G8_right1.png",   tw, th);
        pRight2  = loadSafeIcon("images/G8_right2.png",   tw, th);

        mapLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,1,1,1,1,1,1,0,0,
            0,1,1,1,1,1,1,1,1,1,0,
            0,1,1,0,0,1,2,0,1,1,0,
            0,1,1,1,1,1,1,2,0,1,0,
            0,1,0,2,1,1,0,0,1,1,0,
            0,1,0,0,0,1,1,1,1,1,0,
            0,1,1,1,1,1,1,1,0,3,0,
            0,1,0,0,1,1,0,0,1,1,0,
            0,1,1,1,1,3,1,1,1,1,0,
            0,0,0,0,0,0,0,0,0,0,0
        };
 
        characterLabels   = new JLabel[mapWidth * mapHeight];
        monsterLabels     = new JLabel[mapWidth * mapHeight];
        characterPosition = 60;
        
        for (int i = 0; i < characterLabels.length; i++) {
            characterLabels[i] = new JLabel();
            characterLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            if (i == characterPosition)
                characterLabels[i].setIcon(pDown1);
                
            monsterLabels[i] = new JLabel();
            monsterLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private ImageIcon loadSafeIcon(String path, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getIconWidth() == -1) {
                System.err.println("Warning: Image not found -> " + path);
                if (path.contains("G8_")) {
                    String fallback = path.replace("G8_", "");
                    icon = new ImageIcon(fallback);
                }
            }
            if (icon.getIconWidth() == -1) return null; 
            return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    public void setFrame() {
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

        JPanel charGrid = new JPanel(new GridLayout(mapHeight, mapWidth));
        charGrid.setBounds(0, 0, frameWidth, frameHeight);
        charGrid.setOpaque(false);
        for (JLabel lbl : characterLabels) charGrid.add(lbl);

        JPanel monsterGrid = new JPanel(new GridLayout(mapHeight, mapWidth));
        monsterGrid.setBounds(0, 0, frameWidth, frameHeight);
        monsterGrid.setOpaque(false);
        for (JLabel lbl : monsterLabels) monsterGrid.add(lbl);

        JLabel background = new JLabel(BGimage);
        background.setBounds(0, 0, frameWidth, frameHeight);

        layeredPane.add(background,  JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(charGrid,    JLayeredPane.PALETTE_LAYER);
        layeredPane.add(monsterGrid, JLayeredPane.MODAL_LAYER);

        frame.add(layeredPane);
        frame.addKeyListener(this);
        dialog.addKey(frame);

        timerLabel = new JLabel("Survive: " + survivalTime + "s");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.WHITE);
        JPanel glass = (JPanel) frame.getGlassPane();
        glass.setVisible(true);
        glass.setLayout(new FlowLayout(FlowLayout.CENTER));
        glass.add(timerLabel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        startGameTimer();
        startMonsterChase();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            if (Battle.paused)      return;
            if (dialog.isVisible()) return;

            int move = 0;
            int key  = e.getKeyCode();

            if      (key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) { direction = 0; move = -mapWidth; }
            else if (key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) { direction = 1; move =  mapWidth; }
            else if (key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) { direction = 2; move = -1; }
            else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { direction = 3; move =  1; }

            if (key == KeyEvent.VK_SPACE) {
                int interactPos = switch (direction) {
                    case 0 -> characterPosition - mapWidth;
                    case 1 -> characterPosition + mapWidth;
                    case 2 -> characterPosition - 1;
                    case 3 -> characterPosition + 1;
                    default -> -1;
                };

                // FIX: Check if standing directly ON Tile 3, or facing it
                boolean onBossTile = (mapLayout[characterPosition] == 3);
                boolean facingBossTile = (interactPos >= 0 && interactPos < mapLayout.length && mapLayout[interactPos] == 3);

                // Boss trigger logic
                if ((onBossTile || facingBossTile) && !battleTriggered) {
                    int triggerPos = onBossTile ? characterPosition : interactPos;
                    battleTriggered = true;
                    if (gameTimer != null) gameTimer.stop();
                    if (monsterTimer != null) monsterTimer.stop();

                    dialog.show(layeredPane,
                        new String[]{
                            "GIGGLEBOT3000: A SYSTEMIC FLAW DETECTED.",
                            "GIGGLEBOT3000: Solve or perish!"
                        },
                        null, null, mapWidth, mapHeight,
                        () -> {
                            battle.start(frame, "images/G8_PD6BG.png", "GIGGLEBOT3000");
                            waitForBattleEnd(() -> {
                                if (!SaveSystem.isDefeated("GIGGLEBOT3000"))
                                    SaveSystem.markDefeated("GIGGLEBOT3000");
                                saveProgress();
                                mapLayout[triggerPos] = 1; // Clear the specific boss tile
                                showVictoryCutscene();
                            });
                        });
                    return; // Stop here so it doesn't trigger "nothing found"
                }

                // Normal interactions
                if (interactPos < 0 || interactPos >= mapLayout.length) return;
                int tileType = mapLayout[interactPos];
                
                // Allow interaction if player is standing on the tile after battle
                if (onBossTile && battleTriggered) {
                    tileType = 3;
                }

                if (tileType == 2) {
                    new BushEvent(interactPos).onInteract(dialog, layeredPane, mapWidth, mapHeight);

                } else if (tileType == 3) {
                    String line = SaveSystem.isDefeated("GIGGLEBOT3000")
                            ? "(Just scorch marks remain. GIGGLEBOT3000 is gone.)"
                            : "(Something still lurks here...)";
                    dialog.show(layeredPane, new String[]{ line }, null, null, mapWidth, mapHeight);

                } else if (tileType != 0 && tileType != 1) {
                    dialog.show(layeredPane,
                        new String[]{ "[You searched, but found nothing.]" },
                        null, null, mapWidth, mapHeight);
                }
            }

            if (move != 0) {
                int nextPos = characterPosition + move;
                if (nextPos >= 0 && nextPos < mapLayout.length) {
                    boolean isWrapping = (Math.abs(move) == 1
                            && (nextPos / mapWidth != characterPosition / mapWidth));
                    if (!isWrapping && mapLayout[nextPos] != 0) {
                        characterLabels[characterPosition].setIcon(null);
                        characterPosition = nextPos;
                        walkFrame = (walkFrame + 1) % 2;  
                    } else {
                        walkFrame = 0;
                    }
                }

                ImageIcon current = switch (direction) {
                    case 0 -> (walkFrame == 0) ? pUp1    : pUp2;
                    case 1 -> (walkFrame == 0) ? pDown1  : pDown2;
                    case 2 -> (walkFrame == 0) ? pLeft1  : pLeft2;
                    case 3 -> (walkFrame == 0) ? pRight1 : pRight2;
                    default -> pDown1;
                };
                if (current == null) current = pDown1;
                characterLabels[characterPosition].setIcon(current);
            }

        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("Movement/Interaction Error: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("General Input Error: " + ex.getMessage());
        }
    }

    @Override public void keyTyped(KeyEvent e)    {}
    @Override public void keyReleased(KeyEvent e) {}

    public void startMonsterChase() {
        enemy = new Monster(12, frameWidth / mapWidth, frameHeight / mapHeight);
        monsterLabels[12].setIcon(enemy.getIcon()); // Spawn instantly
        
        monsterTimer = new Timer(800, e -> moveMonster());
        monsterTimer.start();
    }

    private void moveMonster() {
        if (dialog.isVisible() || Battle.paused || enemy == null) return;

        int mPos = enemy.getPosition();
        int nextPos = mPos;

        if      (mPos % mapWidth < characterPosition % mapWidth) nextPos++;
        else if (mPos % mapWidth > characterPosition % mapWidth) nextPos--;
        else if (mPos / mapWidth < characterPosition / mapWidth) nextPos += mapWidth;
        else if (mPos / mapWidth > characterPosition / mapWidth) nextPos -= mapWidth;

        if (nextPos != mPos && nextPos >= 0 && nextPos < mapLayout.length && mapLayout[nextPos] != 0) {
            monsterLabels[mPos].setIcon(null);
            enemy.setPosition(nextPos);
            monsterLabels[nextPos].setIcon(enemy.getIcon());
            layeredPane.repaint();
        }

        // FIX: Freeze prevention & double-trigger prevention
        if (enemy != null && enemy.getPosition() == characterPosition) {
            if (monsterTimer != null) monsterTimer.stop(); 
            if (gameTimer != null) gameTimer.stop();
            
            Monster caughtEnemy = enemy;
            enemy = null; // Guarantee this block only runs ONCE

            SwingUtilities.invokeLater(() -> {
                caughtEnemy.onCatch(dialog, layeredPane, mapWidth, mapHeight, monsterTimer, gameTimer, () -> {
                    SwingUtilities.invokeLater(() -> {
                        frame.dispose();
                        G8_Room2_PD6 fresh = new G8_Room2_PD6();
                        fresh.setFrame();
                    });
                });
            });
        }
    }

    public void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            if (dialog.isVisible() || Battle.paused) return;
            survivalTime--;
            timerLabel.setText("Survive: " + survivalTime + "s");
            if (survivalTime <= 0) {
                gameTimer.stop();
                monsterTimer.stop();
                showSurvivedCutscene();
            }
        });
        gameTimer.start();
    }

    private void showSurvivedCutscene() {
        dialog.show(layeredPane,
            new String[]{
                "You survived the onslaught!",
                "But something grabs you from behind...",
                "GIGGLEBOT3000: SPECIMEN ACQUIRED. TRANSPORTING TO LAB.",
                "Everything goes dark as you are dragged through a metal corridor.",
                "You wake up... somewhere different. The lab."
            },
            null, null, mapWidth, mapHeight,
            () -> { timerLabel.setText(""); });
    }

    private void showVictoryCutscene() {
        dialog.show(layeredPane,
            new String[]{
                "GIGGLEBOT3000 sparks and collapses.",
                "'S-SYSTEM... FAILURE... FLAW... UNACCEPTABLE...'",
                "Its chassis hits the floor with a heavy clang.",
                "Silence. Finally.",
                "You catch your breath. There has to be a way out of this lab.",
                "A door at the far end slides open...",
                "[END OF PD6 — next area loading...]"
            },
            null, null, mapWidth, mapHeight,
            () -> {
                JOptionPane.showMessageDialog(frame,
                    "PD6 complete! Next PD will be wired in here once ready.");
                System.exit(0);
            });
    }

    private void waitForBattleEnd(Runnable onEnd) {
        javax.swing.Timer poller = new javax.swing.Timer(200, null);
        poller.addActionListener(ev -> {
            if (!Battle.paused) {
                poller.stop();
                onEnd.run();
            }
        });
        poller.start();
    }

    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G8_Room2_PD6");
        SaveSystem.startTimer(save.timeSeconds);
        if (save.isDefeated("GIGGLEBOT3000")) {
            battleTriggered = true;
            mapLayout[79] = 1;  
            mapLayout[95] = 1;  
        }
        System.out.println("[G8_Room2] Loaded — BattleDone:" + battleTriggered
                + "  Time:" + save.formattedTime());
    }

    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G8_Room2_PD6")
                .battles(SaveSystem.getDefeatedBosses())
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            G8_Room2_PD6 room = new G8_Room2_PD6();
            room.loadSaveData();
            room.setFrame();
        });
    }
}
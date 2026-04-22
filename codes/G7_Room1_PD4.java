package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class G7_Room1_PD4 extends JPanel implements KeyListener {

    final int TILE = 40;

    Image G7_player, G7_wall, G7_npc, G7_enemy, G7_floor;

    String[] maze = {
            "111111111111",
            "1000000000E1",
            "101111111101",
            "101000001101",
            "101011101101",
            "1N0010000101",
            "111111111111"
    };

    int rows = maze.length;
    int cols = maze[0].length();

    int playerRow = 1;
    int playerCol = 1;

    boolean talkedToNPC  = false;
    boolean hasKey       = false;
    boolean doorUnlocked = false;
    boolean bossDefeated = false;

    int playerHP = 100;

    static final int HP_BAR_MAX_WIDTH = 200;

    Battle battle = new Battle();

    JFrame frame;

    public G7_Room1_PD4() {
        setPreferredSize(new Dimension(cols * TILE, rows * TILE + 70));
        setFocusable(true);
        addKeyListener(this);

        G7_player = new ImageIcon(getClass().getResource("/Images/player.png")).getImage();
        G7_wall   = new ImageIcon(getClass().getResource("/Images/wall.png")).getImage();
        G7_npc    = new ImageIcon(getClass().getResource("/Images/npc.png")).getImage();
        G7_enemy  = new ImageIcon(getClass().getResource("/Images/enemy.png")).getImage();
        G7_floor  = new ImageIcon(getClass().getResource("/Images/floor.png")).getImage();

        loadSaveData();
    }

    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G7_Room1_PD4");
        SaveSystem.startTimer(save.timeSeconds);

        talkedToNPC  = save.hasFlag("talked_npc");
        hasKey       = save.hasFlag("has_key");
        doorUnlocked = save.hasFlag("door_unlocked");
        bossDefeated = save.hasFlag("boss_defeated");

        System.out.println("[G7 Room1] Loaded - talkedToNPC: " + talkedToNPC
                + " hasKey: " + hasKey
                + " doorUnlocked: " + doorUnlocked
                + " bossDefeated: " + bossDefeated
                + " time: " + save.formattedTime());
    }

    private void saveProgress() {
        SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("G7_Room1_PD4")
                        .flag(talkedToNPC  ? "talked_npc"    : null)
                        .flag(hasKey       ? "has_key"       : null)
                        .flag(doorUnlocked ? "door_unlocked" : null)
                        .flag(bossDefeated ? "boss_defeated" : null)
                        .battles(SaveSystem.getDefeatedBosses())
        );
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char tile = maze[r].charAt(c);

                if (tile == '1') {
                    g.drawImage(G7_wall,  c * TILE, r * TILE, TILE, TILE, null);
                } else {
                    g.drawImage(G7_floor, c * TILE, r * TILE, TILE, TILE, null);
                }
                if (tile == 'N') g.drawImage(G7_npc,   c * TILE, r * TILE, TILE, TILE, null);
                if (tile == 'E') g.drawImage(G7_enemy, c * TILE, r * TILE, TILE, TILE, null);
            }
        }

        g.drawImage(G7_player, playerCol * TILE, playerRow * TILE, TILE, TILE, null);

        // ── HUD ──────────────────────────────────────────────────────────────
        int barY = rows * TILE + 10;

        // Black border
        g.setColor(Color.BLACK);
        g.drawRect(20, barY, HP_BAR_MAX_WIDTH, 20);

        // Red background
        g.setColor(Color.RED);
        g.fillRect(21, barY + 1, HP_BAR_MAX_WIDTH - 1, 19);

        // Green fill — clamped so it never overflows the border
        int greenW = Math.max(0, Math.min(playerHP * 2, HP_BAR_MAX_WIDTH - 1));
        g.setColor(Color.GREEN);
        g.fillRect(21, barY + 1, greenW, 19);

        g.setColor(Color.BLACK);
        g.drawString("Player HP: " + playerHP,              20, rows * TILE + 50);
        g.drawString("Key: "  + (hasKey       ? "YES" : "NO"),  140, rows * TILE + 50);
        g.drawString("Door: " + (doorUnlocked ? "UNLOCKED" : "LOCKED"), 240, rows * TILE + 50);
    }

    void movePlayer(int dr, int dc) {
        if (Battle.paused) return;  // block input while battle overlay is open

        int newRow = playerRow + dr;
        int newCol = playerCol + dc;

        if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) return;

        char tile = maze[newRow].charAt(newCol);

        if (tile == '1') return;

        // Block on 'E' if door not yet unlocked
        if (tile == 'E' && !doorUnlocked) {
            JOptionPane.showMessageDialog(this,
                    "The door is locked.\nTalk to the NPC first.");
            return;
        }

        playerRow = newRow;
        playerCol = newCol;

        // NPC interaction
        if (tile == 'N' && !talkedToNPC) {
            talkedToNPC  = true;
            hasKey       = true;
            doorUnlocked = true;
            playerHP    += 20;

            JOptionPane.showMessageDialog(this,
                    "NPC: Take this help (+20 HP)\n" +
                    "You received the key.\n" +
                    "The door is now unlocked!");

            saveProgress();
        }

        // Enemy interaction — warn then battle, no room transition
        if (tile == 'E' && doorUnlocked && !bossDefeated) {
            JOptionPane.showMessageDialog(this,
                    "???: You dare enter my domain?\n" +
                    "RYAN: I will destroy you!");
            triggerBossBattle();
            return;
        }

        repaint();
    }

    void triggerBossBattle() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame f)) return;

        battle.hp    = playerHP;
        battle.maxHp = playerHP;

        battle.start(f, "TRY THIS", "RYAN");

        // Poll until Battle.paused goes false (battle ended)
        Timer checkEnd = new Timer(500, null);
        checkEnd.addActionListener(e -> {
            if (!Battle.paused) {
                checkEnd.stop();
                playerHP = battle.hp;

                if (playerHP <= 0) {
                    JOptionPane.showMessageDialog(this, "You were defeated by RYAN...");
                    System.exit(0);
                } else {
                    bossDefeated = true;
                    saveProgress();
                    JOptionPane.showMessageDialog(this,
                            "You defeated RYAN!\nYou got the money 💰");
                    repaint();
                }

                requestFocusInWindow();
            }
        });
        checkEnd.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (Battle.paused) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP,    KeyEvent.VK_W -> movePlayer(-1, 0);
            case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> movePlayer(1,  0);
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> movePlayer(0, -1);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> movePlayer(0,  1);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Room 1 - G7");
        G7_Room1_PD4 panel = new G7_Room1_PD4();

        panel.frame = frame;

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

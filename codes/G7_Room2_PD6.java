package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class G7_Room2_PD6 extends JPanel implements KeyListener {

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

    boolean talkedToNPC      = false;
    boolean firstEncounterDone = false;  // true after first step on 'E'
    boolean bossDefeated     = false;

    int playerHP = 100;

    Battle battle = new Battle();

    JFrame frame;

    public G7_Room2_PD6() {
        setPreferredSize(new Dimension(cols * TILE, rows * TILE + 70));
        setFocusable(true);
        addKeyListener(this);

        G7_player = new ImageIcon(getClass().getResource("/Images/player.png")).getImage();
        G7_wall   = new ImageIcon(getClass().getResource("/Images/wall.png")).getImage();
        G7_npc    = new ImageIcon(getClass().getResource("/Images/npc.png")).getImage();
        G7_enemy  = new ImageIcon(getClass().getResource("/Images/enemy.png")).getImage();
        G7_floor  = new ImageIcon(getClass().getResource("/Images/floor.png")).getImage();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                char tile = maze[r].charAt(c);

                if (tile == '1') {
                    g.drawImage(G7_wall, c * TILE, r * TILE, TILE, TILE, null);
                } else {
                    g.drawImage(G7_floor, c * TILE, r * TILE, TILE, TILE, null);
                }

                if (tile == 'N') {
                    g.drawImage(G7_npc, c * TILE, r * TILE, TILE, TILE, null);
                }

                if (tile == 'E') {
                    g.drawImage(G7_enemy, c * TILE, r * TILE, TILE, TILE, null);
                }
            }
        }

        g.drawImage(G7_player, playerCol * TILE, playerRow * TILE, TILE, TILE, null);

        // HP bar — player
        g.setColor(Color.RED);
        g.fillRect(20, rows * TILE + 10, 200, 20);
        g.setColor(Color.GREEN);
        g.fillRect(20, rows * TILE + 10, Math.max(0, playerHP * 2), 20);
        g.setColor(Color.BLACK);
        g.drawRect(20, rows * TILE + 10, 200, 20);
        g.drawString("Player HP: " + playerHP, 20, rows * TILE + 45);
    }

    void movePlayer(int dr, int dc) {
        int newRow = playerRow + dr;
        int newCol = playerCol + dc;

        if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) return;
        if (maze[newRow].charAt(newCol) == '1') return;

        char destination = maze[newRow].charAt(newCol);

        playerRow = newRow;
        playerCol = newCol;

        // NPC boost — unchanged
        if (destination == 'N' && !talkedToNPC) {
            talkedToNPC = true;
            playerHP += 30;
            JOptionPane.showMessageDialog(this,
                    "NPC: RYAN is dangerous...\n" +
                    "Take this blessing (+30 HP)\n" +
                    "Your HP: " + playerHP);
            repaint();
        }

        // Enemy tile — two-stage interaction
        if (destination == 'E') {
            if (!firstEncounterDone) {
                firstEncounterDone = true;
                JOptionPane.showMessageDialog(this,
                        "A dark presence looms...\nRYAN is watching you.");
            } else if (!bossDefeated) {
                triggerBossBattle();
                return;
            }
        }

        repaint();
    }

    void triggerBossBattle() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame f)) return;

        // Hand off current player HP into the battle system
        battle.hp    = playerHP;
        battle.maxHp = playerHP;

        battle.start(f, "TRY THIS", "RYAN");

        // Poll every 500ms until Battle.paused goes false (battle ended)
        Timer checkEnd = new Timer(500, null);
        checkEnd.addActionListener(e -> {
            if (!Battle.paused) {
                checkEnd.stop();
                playerHP = battle.hp;  // write HP back after battle ends

                if (playerHP <= 0) {
                    JOptionPane.showMessageDialog(this, "You were defeated...");
                    System.exit(0);
                } else {
                    bossDefeated = true;
                    JOptionPane.showMessageDialog(this,
                            "You defeated RYAN!\nYou got the money 💰");
                    repaint();
                }

                requestFocusInWindow();  // restore key input after battle UI closes
            }
        });
        checkEnd.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (Battle.paused) return;  // block movement while battle is active
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> movePlayer(-1, 0);
            case KeyEvent.VK_DOWN  -> movePlayer(1, 0);
            case KeyEvent.VK_LEFT  -> movePlayer(0, -1);
            case KeyEvent.VK_RIGHT -> movePlayer(0, 1);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Room 2 - G7");
        G7_Room2_PD6 panel = new G7_Room2_PD6();

        panel.frame = frame;

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

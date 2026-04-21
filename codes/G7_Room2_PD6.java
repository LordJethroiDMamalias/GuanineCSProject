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

    boolean talkedToNPC = false;
    boolean gameFinished = false;

    int playerHP = 100;
    int bossHP = 100;

    public G7_Room1_PD4() {
        setPreferredSize(new Dimension(cols * TILE, rows * TILE + 70));
        setFocusable(true);
        addKeyListener(this);

        // SPRITES (RENAMED VARIABLES)
        G7_player = new ImageIcon(getClass().getResource("/assets/player.png")).getImage();
        G7_wall   = new ImageIcon(getClass().getResource("/assets/wall.png")).getImage();
        G7_npc    = new ImageIcon(getClass().getResource("/assets/npc.png")).getImage();
        G7_enemy  = new ImageIcon(getClass().getResource("/assets/enemy.png")).getImage();
        G7_floor  = new ImageIcon(getClass().getResource("/assets/floor.png")).getImage();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // MAZE
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

        // PLAYER
        g.drawImage(G7_player, playerCol * TILE, playerRow * TILE, TILE, TILE, null);

        // PLAYER HP BAR
        g.setColor(Color.RED);
        g.fillRect(20, rows * TILE + 10, 200, 20);

        g.setColor(Color.GREEN);
        g.fillRect(20, rows * TILE + 10, Math.max(0, playerHP * 2), 20);

        g.setColor(Color.BLACK);
        g.drawRect(20, rows * TILE + 10, 200, 20);
        g.drawString("Player HP: " + playerHP, 20, rows * TILE + 45);

        // BOSS HP BAR
        g.setColor(Color.RED);
        g.fillRect(260, rows * TILE + 10, 200, 20);

        g.setColor(Color.GREEN);
        g.fillRect(260, rows * TILE + 10, Math.max(0, bossHP * 2), 20);

        g.setColor(Color.BLACK);
        g.drawRect(260, rows * TILE + 10, 200, 20);
        g.drawString("RYAN HP: " + bossHP, 260, rows * TILE + 45);
    }

    void movePlayer(int dr, int dc) {
        int newRow = playerRow + dr;
        int newCol = playerCol + dc;

        char destination = maze[newRow].charAt(newCol);

        if (destination == '1') return;

        playerRow = newRow;
        playerCol = newCol;

        // NPC BOOST
        if (destination == 'N' && !talkedToNPC) {
            talkedToNPC = true;
            playerHP += 30;

            JOptionPane.showMessageDialog(this,
                    "NPC: RYAN is dangerous...\n" +
                    "Take this blessing (+30 HP)\n" +
                    "Your HP: " + playerHP);

            repaint();
        }

        // ENEMY
        if (destination == 'E' && !gameFinished) {
            startBattle();
        }

        repaint();
    }

    void startBattle() {
        bossHP = 100;

        JOptionPane.showMessageDialog(this,
                "😈 RYAN Appears!\nBattle Start!");

        while (playerHP > 0 && bossHP > 0) {

            String q = JOptionPane.showInputDialog(this,
                    "Your HP: " + playerHP + " | RYAN HP: " + bossHP +
                    "\n\nRYAN:\nFind the MEAN of 2, 4, 6");

            if ("4".equals(q)) {
                bossHP -= 30;
                playerHP += 10;
                JOptionPane.showMessageDialog(this, "Correct! 30 dmg +10 HP!");
            } else {
                playerHP -= 40;
                JOptionPane.showMessageDialog(this, "Wrong! RYAN hits you (-40 HP)");
            }

            repaint(); pause();
            if (bossHP <= 0 || playerHP <= 0) break;

            q = JOptionPane.showInputDialog(this,
                    "Your HP: " + playerHP + " | RYAN HP: " + bossHP +
                    "\n\nRYAN:\nFind the MEDIAN of 3, 7, 9");

            if ("7".equals(q)) {
                bossHP -= 30;
                playerHP += 10;
                JOptionPane.showMessageDialog(this, "Correct! 30 dmg +10 HP!");
            } else {
                playerHP -= 40;
                JOptionPane.showMessageDialog(this, "Wrong! RYAN hits you (-40 HP)");
            }

            repaint(); pause();
            if (bossHP <= 0 || playerHP <= 0) break;

            q = JOptionPane.showInputDialog(this,
                    "Your HP: " + playerHP + " | RYAN HP: " + bossHP +
                    "\n\nRYAN:\nFind the MODE of 1, 2, 2, 3");

            if ("2".equals(q)) {
                bossHP -= 30;
                playerHP += 10;
                JOptionPane.showMessageDialog(this, "Correct! 30 dmg +10 HP!");
            } else {
                playerHP -= 40;
                JOptionPane.showMessageDialog(this, "Wrong! RYAN hits you (-40 HP)");
            }

            repaint(); pause();
        }

        if (playerHP <= 0) {
            JOptionPane.showMessageDialog(this,
                    "💀 You were defeated by RYAN...\nGame Over.");
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(this,
                    "🎉 You defeated RYAN!\nYou got the money 💰");
            gameFinished = true;
        }
    }

    void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> movePlayer(-1, 0);
            case KeyEvent.VK_DOWN -> movePlayer(1, 0);
            case KeyEvent.VK_LEFT -> movePlayer(0, -1);
            case KeyEvent.VK_RIGHT -> movePlayer(0, 1);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Maze RPG: RYAN Boss Fight");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new G7_Room1_PD4());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

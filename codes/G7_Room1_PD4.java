import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleMazeGame extends JPanel implements KeyListener {

    final int TILE = 40;

    Image playerImg, wallImg, npcImg, enemyImg, floorImg;

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
    int enemyHP = 100;

    public SimpleMazeGame() {

        setPreferredSize(new Dimension(cols * TILE, rows * TILE + 70));
        setFocusable(true);
        addKeyListener(this);

        playerImg = new ImageIcon(getClass().getResource("/assets/player.png")).getImage();
        wallImg   = new ImageIcon(getClass().getResource("/assets/wall.png")).getImage();
        npcImg    = new ImageIcon(getClass().getResource("/assets/npc.png")).getImage();
        enemyImg  = new ImageIcon(getClass().getResource("/assets/enemy.png")).getImage();
        floorImg  = new ImageIcon(getClass().getResource("/assets/floor.png")).getImage();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                char tile = maze[r].charAt(c);

                if (tile == '1') {
                    g.drawImage(wallImg, c * TILE, r * TILE, TILE, TILE, null);
                } else {
                    g.drawImage(floorImg, c * TILE, r * TILE, TILE, TILE, null);
                }

                if (tile == 'N') {
                    g.drawImage(npcImg, c * TILE, r * TILE, TILE, TILE, null);
                }

                if (tile == 'E') {
                    g.drawImage(enemyImg, c * TILE, r * TILE, TILE, TILE, null);
                }
            }
        }

        g.drawImage(playerImg, playerCol * TILE, playerRow * TILE, TILE, TILE, null);

        g.setColor(Color.BLACK);
        g.drawString("Player HP: " + playerHP, 20, rows * TILE + 25);
        g.drawString("Enemy HP: " + enemyHP, 160, rows * TILE + 25);
    }

    void movePlayer(int dr, int dc) {

        if (gameFinished) return;

        int newRow = playerRow + dr;
        int newCol = playerCol + dc;

        if (maze[newRow].charAt(newCol) == '1') return;

        playerRow = newRow;
        playerCol = newCol;

        char tile = maze[newRow].charAt(newCol);

        if (tile == 'N' && !talkedToNPC) {
            talkedToNPC = true;
            playerHP += 20;

            JOptionPane.showMessageDialog(this,
                    "NPC: Take this help (+20 HP)\nPlayer HP: " + playerHP);
        }

        if (tile == 'E' && !gameFinished) {
            startBattle();
        }

        repaint();
    }

    void startBattle() {

        enemyHP = 100;

        JOptionPane.showMessageDialog(this,
                "Enemy Appears! Battle Start!");

        while (playerHP > 0 && enemyHP > 0) {

            String q = JOptionPane.showInputDialog(this,
                    "What is 3 + 3?");

            if ("6".equals(q)) {
                enemyHP -= 30;
                playerHP += 10;

                JOptionPane.showMessageDialog(this,
                        "Correct! Enemy -30 HP");
            } else {
                playerHP -= 30;

                JOptionPane.showMessageDialog(this,
                        "Wrong! You take damage!");
            }

            repaint();

            if (enemyHP <= 0 || playerHP <= 0) break;
        }

        if (playerHP <= 0) {
            JOptionPane.showMessageDialog(this, "You Lost!");
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(this, "You Win!");
            gameFinished = true;
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
        JFrame frame = new JFrame("Simple Maze RPG");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SimpleMazeGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

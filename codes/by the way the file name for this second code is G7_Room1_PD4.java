import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class G7_Room1_PD4 extends JPanel implements KeyListener {

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

    public G7_Room1_PD4() {
        setPreferredSize(new Dimension(cols * TILE, rows * TILE));
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
    }

    void movePlayer(int dr, int dc) {
        int newRow = playerRow + dr;
        int newCol = playerCol + dc;

        char destination = maze[newRow].charAt(newCol);

        if (destination == '1') return;

        playerRow = newRow;
        playerCol = newCol;

        if (destination == 'N' && !talkedToNPC) {
            talkedToNPC = true;
            JOptionPane.showMessageDialog(this,
                    "NPC: Beware of the tree monster ahead...\nAnswer wisely if you want the money.");
        }

        if (destination == 'E' && !gameFinished) {
            startGamble();
        }

        repaint();
    }

    void startGamble() {
        String q1 = JOptionPane.showInputDialog(this, "Tree Monster:\nWhat is 5 + 3?");
        if (!"8".equals(q1)) fail();

        String q2 = JOptionPane.showInputDialog(this, "Tree Monster:\nWhat is the capital of France?");
        if (!"Paris".equalsIgnoreCase(q2)) fail();

        String q3 = JOptionPane.showInputDialog(this, "Tree Monster:\nJava keyword to inherit a class?");
        if (!"extends".equalsIgnoreCase(q3)) fail();

        gameFinished = true;
        JOptionPane.showMessageDialog(this,
                "You won the gamble!\nYou received the money 💰");
    }

    void fail() {
        JOptionPane.showMessageDialog(this,
                "Wrong answer!\nThe monster keeps the money.");
        System.exit(0);
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
        JFrame frame = new JFrame("Maze Gamble Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new G7_Room1_PD4());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class G7_Room2_PD6 extends JPanel implements KeyListener, ActionListener {

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

    Player player;
    Enemy enemy;

    boolean up, down, left, right;
    boolean talkedToNPC = false;
    boolean gameFinished = false;
    boolean gameWon = false;

    Timer timer;

    public G7_Room2_PD6() {

        setPreferredSize(new Dimension(cols * TILE, rows * TILE));
        setFocusable(true);
        addKeyListener(this);

        playerImg = new ImageIcon(getClass().getResource("/assets/player.png")).getImage();
        wallImg   = new ImageIcon(getClass().getResource("/assets/wall.png")).getImage();
        npcImg    = new ImageIcon(getClass().getResource("/assets/npc.png")).getImage();
        enemyImg  = new ImageIcon(getClass().getResource("/assets/enemy.png")).getImage();
        floorImg  = new ImageIcon(getClass().getResource("/assets/floor.png")).getImage();

        player = new Player(TILE, TILE, TILE, playerImg, 4);
        enemy = new Enemy(10 * TILE, 1 * TILE, TILE, enemyImg, 0);

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
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
                    enemy.draw(g);
                }
            }
        }

        player.draw(g);

        if (gameWon) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("YOU WIN!", 120, 150);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameFinished) movePlayer();
        repaint();
    }

    void movePlayer() {

        int currentX = player.getX();
        int currentY = player.getY();

        if (up) {
            int newY = currentY - 4;
            if (!isWall(currentX, newY))
                player.setPosition(currentX, newY);
        }

        if (down) {
            int newY = currentY + 4;
            if (!isWall(currentX, newY))
                player.setPosition(currentX, newY);
        }

        if (left) {
            int newX = currentX - 4;
            if (!isWall(newX, currentY))
                player.setPosition(newX, currentY);
        }

        if (right) {
            int newX = currentX + 4;
            if (!isWall(newX, currentY))
                player.setPosition(newX, currentY);
        }

        checkInteraction();
    }

    boolean isWall(int x, int y) {

        int leftCol = x / TILE;
        int rightCol = (x + TILE - 1) / TILE;
        int topRow = y / TILE;
        int bottomRow = (y + TILE - 1) / TILE;

        return maze[topRow].charAt(leftCol) == '1' ||
               maze[topRow].charAt(rightCol) == '1' ||
               maze[bottomRow].charAt(leftCol) == '1' ||
               maze[bottomRow].charAt(rightCol) == '1';
    }

    void checkInteraction() {

        int col = player.getX() / TILE;
        int row = player.getY() / TILE;
        char tile = maze[row].charAt(col);

        if (tile == 'N' && !talkedToNPC) {
            talkedToNPC = true;
            JOptionPane.showMessageDialog(this,
                    "NPC: Defeat the Tree Monster to escape!");
        }

        if (tile == 'E' && !gameFinished) {
            startBattle();
        }
    }

    void startBattle() {

        try {
            String q1 = JOptionPane.showInputDialog(this, "5 + 3?");
            if (q1 == null || !q1.trim().equals("8")) {
                fail("Wrong answer to Q1");
                return;
            }

            String q2 = JOptionPane.showInputDialog(this, "Capital of France?");
            if (q2 == null || !q2.trim().equalsIgnoreCase("Paris")) {
                fail("Wrong answer to Q2");
                return;
            }

            String q3 = JOptionPane.showInputDialog(this, "Java keyword for inheritance?");
            if (q3 == null || !q3.trim().equalsIgnoreCase("extends")) {
                fail("Wrong answer to Q3");
                return;
            }

            gameFinished = true;
            gameWon = true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input. Please enter valid answers.");
        }
    }

    void fail(String message) {
        JOptionPane.showMessageDialog(this, "Game Over!\n" + message);
        System.exit(0);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP) up = true;
        else if (key == KeyEvent.VK_DOWN) down = true;
        else if (key == KeyEvent.VK_LEFT) left = true;
        else if (key == KeyEvent.VK_RIGHT) right = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) up = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = false;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
    }

    @Override public void keyTyped(KeyEvent e) {}
}

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

        String q1 = JOptionPane.showInputDialog(this, "5 + 3?");
        if (!"8".equals(q1)) fail();

        String q2 = JOptionPane.showInputDialog(this, "Capital of France?");
        if (!"Paris".equalsIgnoreCase(q2)) fail();

        String q3 = JOptionPane.showInputDialog(this, "Java keyword for inheritance?");
        if (!"extends".equalsIgnoreCase(q3)) fail();

        gameFinished = true;
        gameWon = true;
    }

    void fail() {
        JOptionPane.showMessageDialog(this, "Game Over!");
        System.exit(0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) up = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = true;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) up = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = false;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Maze Adventure");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new G7_Room2_PD6());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class GameObject {
    protected int x, y;
    protected int size;
    protected Image image;

    public GameObject(int x, int y, int size, Image image) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.image = image;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, size, size, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Character extends GameObject {

    protected int speed;

    public Character(int x, int y, int size, Image image, int speed) {
        super(x, y, size, image);
        this.speed = speed;
    }

    public void move(int dx, int dy) {
        x += dx * speed;
        y += dy * speed;
    }

    public void move(int dx, int dy, int customSpeed) {
        x += dx * customSpeed;
        y += dy * customSpeed;
    }
}

class Player extends Character {

    public Player(int x, int y, int size, Image image, int speed) {
        super(x, y, size, image, speed);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        g.setColor(Color.YELLOW);
        g.drawRect(x, y, size, size);
    }
}

class Enemy extends Character {

    public Enemy(int x, int y, int size, Image image, int speed) {
        super(x, y, size, image, speed);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        g.setColor(Color.RED);
        g.drawRect(x, y, size, size);
    }
}

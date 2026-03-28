/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package codes;

/**
 *
 * @author malcholm
 */
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.*;

/**
 * MazeGame.java
 *
 * Clean, single-file medium-level maze game (Option 3).
 * - Uses maze[row][col]
 * - Player moves with arrow keys
 * - Collect items; after 10 items you can shoot (SPACE)
 * - Bullets kill enemies
 * - Enemies move randomly toward player occasionally
 *
 * Put optional sprites in same folder:
 *  - player.png
 *  - enemy.png
 *  - item.png
 *  - bullet.png
 *
 * Compile:
 *  javac MazeGame.java
 *  java MazeGame
 */
public class G10_Room1_PD4 extends JPanel implements ActionListener, KeyListener {

    // grid
    private static final int CELL = 32;
    private static final int ROWS = 15;
    private static final int COLS = 20;

    // game state
    private int playerRow = 1;
    private int playerCol = 1;
    private int health = 5;
    private int score = 0;
    private boolean paused = false;
    private boolean gameOver = false;
    private boolean gameWon = false;

    // shooting
    private final List<Bullet> bullets = new ArrayList<>();
    private boolean canShoot = false;
    private int itemsCollected = 0;
    private int facing = KeyEvent.VK_RIGHT; // last direction; used when shooting

    // items and enemies
    private final Set<Item> items = new HashSet<>();
    private final List<Enemy> enemies = new ArrayList<>();

    // sprites (optional)
    private final Image playerImg;
    private final Image enemyImg;
    private final Image itemImg;
    private final Image bulletImg;

    // timer (use javax.swing.Timer explicitly)
    private final javax.swing.Timer timer;

    // medium maze (0=open, 1=wall)
    private final int[][] maze = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,0,1,1,1,1,0,1,0,1,1,1,1,0,1},
            {1,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,1},
            {1,0,1,0,1,1,0,1,0,1,1,1,0,1,1,1,0,1,0,1},
            {1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,1},
            {1,1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,0,1,0,1},
            {1,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,1},
            {1,0,1,1,1,1,0,1,0,1,1,1,0,1,1,1,0,1,0,1},
            {1,0,1,0,0,0,0,1,0,0,0,1,0,1,0,0,0,1,0,1},
            {1,0,1,0,1,1,0,1,1,1,0,1,0,1,0,1,1,1,0,1},
            {1,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1},
            {1,0,1,0,1,1,1,1,0,1,1,1,0,1,0,1,1,1,1,1},
            {1,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    public G10_Room1_PD4() {
        setPreferredSize(new Dimension(COLS * CELL, ROWS * CELL));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // load images (optional)
        playerImg = loadImage("G10_player.png");
        enemyImg = loadImage("G10_enemy.png");
        itemImg = loadImage("G10_item.png");
        bulletImg = loadImage("G10_bullet.png");

        // place items and enemies
        spawnRandomItems(20);
        spawnEnemies();

        // Swing timer (explicit)
        timer = new javax.swing.Timer(100, this);
        timer.start();
    }

    private Image loadImage(String name) {
        try {
            ImageIcon ic = new ImageIcon(name);
            return ic.getImage();
        } catch (Exception e) {
            return null;
        }
    }

    // spawn items (items store row,col)
    private void spawnRandomItems(int count) {
        Random rnd = new Random();
        while (items.size() < count) {
            int r = rnd.nextInt(ROWS);
            int c = rnd.nextInt(COLS);
            if (maze[r][c] == 0 && !(r == playerRow && c == playerCol)) {
                items.add(new Item(r, c));
            }
        }
    }

    private void spawnEnemies() {
        enemies.clear();
        // place a few enemies at safe open locations
        enemies.add(new Enemy(7, 15));  // row, col
        enemies.add(new Enemy(10, 5));
        enemies.add(new Enemy(12, 13));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw grid
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (maze[r][c] == 1) g.setColor(new Color(60,60,60));
                else g.setColor(new Color(20,20,20));
                g.fillRect(c * CELL, r * CELL, CELL, CELL);
            }
        }

        // draw items
        for (Item it : items) {
            if (itemImg != null) g.drawImage(itemImg, it.col * CELL + 6, it.row * CELL + 6, CELL - 12, CELL - 12, this);
            else {
                g.setColor(Color.YELLOW);
                g.fillOval(it.col * CELL + 8, it.row * CELL + 8, CELL - 16, CELL - 16);
            }
        }

        // draw bullets
        g.setColor(Color.CYAN);
        for (Bullet b : bullets) {
            if (bulletImg != null) g.drawImage(bulletImg, b.col * CELL + 10, b.row * CELL + 10, 12, 12, this);
            else g.fillOval(b.col * CELL + 10, b.row * CELL + 10, 12, 12);
        }

        // draw enemies
        for (Enemy en : enemies) {
            if (enemyImg != null) g.drawImage(enemyImg, en.col * CELL, en.row * CELL, CELL, CELL, this);
            else {
                g.setColor(Color.RED);
                g.fillOval(en.col * CELL + 4, en.row * CELL + 4, CELL - 8, CELL - 8);
            }
        }

        // draw player
        if (playerImg != null) g.drawImage(playerImg, playerCol * CELL, playerRow * CELL, CELL, CELL, this);
        else {
            g.setColor(Color.BLUE);
            g.fillOval(playerCol * CELL + 4, playerRow * CELL + 4, CELL - 8, CELL - 8);
        }

        // HUD
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 16);
        g.drawString("HP: " + health, 100, 16);
        g.drawString("Items left: " + items.size(), 160, 16);
        g.drawString("Shoot (SPACE): " + (canShoot ? "READY" : "LOCKED"), 300, 16);

        if (paused) {
            g.setFont(g.getFont().deriveFont(Font.BOLD, 32f));
            g.setColor(Color.WHITE);
            g.drawString("PAUSED", getWidth()/2 - 60, getHeight()/2);
        }

        if (gameOver) {
            g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
            g.setColor(Color.RED);
            g.drawString("YOU DIED", getWidth()/2 - 60, getHeight()/2 - 20);
        }

        if (gameWon) {
            g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
            g.setColor(Color.GREEN);
            g.drawString("YOU WIN!", getWidth()/2 - 60, getHeight()/2 - 20);
        }
    }

    // main loop
    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused || gameOver || gameWon) return;

        // enemies act
        for (Enemy en : new ArrayList<>(enemies)) {
            en.actTowards(playerRow, playerCol, maze);
        }

        // bullets update
        updateBullets();

        // collisions & pickups
        checkItemPickup();
        checkEnemyPlayerCollision();

        // win condition: items empty and enemies dead
        if (items.isEmpty() && enemies.isEmpty()) {
            gameWon = true;
            timer.stop();
        }

        repaint();
    }

    private void updateBullets() {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();

            // move
            b.row += b.dRow;
            b.col += b.dCol;

            // out of bounds or hit wall
            if (b.row < 0 || b.row >= ROWS || b.col < 0 || b.col >= COLS || maze[b.row][b.col] == 1) {
                it.remove();
                continue;
            }

            // hit enemy?
            Iterator<Enemy> enIt = enemies.iterator();
            boolean removedBullet = false;
            while (enIt.hasNext()) {
                Enemy en = enIt.next();
                if (en.row == b.row && en.col == b.col) {
                    enIt.remove();
                    it.remove();
                    score += 10;
                    removedBullet = true;
                    break;
                }
            }
            if (removedBullet) continue;
        }
    }

    private void checkItemPickup() {
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item itx = it.next();
            if (itx.row == playerRow && itx.col == playerCol) {
                it.remove();
                score += 5;
                itemsCollected++;
                if (itemsCollected >= 10) canShoot = true;
                if (health < 5) health++; // small heal on pickup
            }
        }
    }

    private void checkEnemyPlayerCollision() {
        for (Enemy en : enemies) {
            if (en.row == playerRow && en.col == playerCol) {
                // damage and knockback attempt
                health--;
                score = Math.max(0, score - 1);
                // simple knockback: try opposite of previous move not tracked -> try random adjacent safe tile
                for (int[] d : new int[][]{{0,1},{0,-1},{1,0},{-1,0}}) {
                    int nr = playerRow + d[0], nc = playerCol + d[1];
                    if (nr >=0 && nr < ROWS && nc >=0 && nc < COLS && maze[nr][nc] == 0) {
                        playerRow = nr; playerCol = nc;
                        break;
                    }
                }
                if (health <= 0) {
                    gameOver = true;
                    timer.stop();
                    break;
                }
            }
        }
    }

    // input
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || gameWon) return;

        int k = e.getKeyCode();

        if (k == KeyEvent.VK_P) {
            paused = !paused;
            return;
        }

        int newRow = playerRow;
        int newCol = playerCol;

        if (k == KeyEvent.VK_UP) { newRow--; facing = KeyEvent.VK_UP; }
        if (k == KeyEvent.VK_DOWN) { newRow++; facing = KeyEvent.VK_DOWN; }
        if (k == KeyEvent.VK_LEFT) { newCol--; facing = KeyEvent.VK_LEFT; }
        if (k == KeyEvent.VK_RIGHT) { newCol++; facing = KeyEvent.VK_RIGHT; }

        // move if open
        if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS && maze[newRow][newCol] == 0) {
            playerRow = newRow;
            playerCol = newCol;
        }

        // shoot
        if (k == KeyEvent.VK_SPACE && canShoot) {
            // bullet spawns in front of player
            int dr = 0, dc = 0;
            if (facing == KeyEvent.VK_UP) dr = -1;
            if (facing == KeyEvent.VK_DOWN) dr = 1;
            if (facing == KeyEvent.VK_LEFT) dc = -1;
            if (facing == KeyEvent.VK_RIGHT) dc = 1;
            if (dr != 0 || dc != 0) {
                bullets.add(new Bullet(playerRow + dr, playerCol + dc, dr, dc));
            }
        }

        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // ------------------ small supporting classes --------------------

    private static class Item {
        int row, col;
        Item(int row, int col) { this.row = row; this.col = col; }

        // equality & hash so Set works
        @Override public boolean equals(Object o) {
            if (!(o instanceof Item)) return false;
            Item i = (Item)o;
            return i.row == row && i.col == col;
        }
        @Override public int hashCode() { return row * 31 + col; }
    }

    private static class Bullet {
        int row, col;
        int dRow, dCol;
        Bullet(int row, int col, int dRow, int dCol) { this.row = row; this.col = col; this.dRow = dRow; this.dCol = dCol; }
    }

    private class Enemy {
        int row, col;
        Random rnd = new Random();

        Enemy(int row, int col) { this.row = row; this.col = col; }

        // act: sometimes attempt to move toward the player, sometimes random
        void actTowards(int prow, int pcol, int[][] maze) {
            // 60% chance try to move toward player, 40% random/no-op
            if (rnd.nextDouble() < 0.4) return;

            int dr = Integer.compare(prow, row);
            int dc = Integer.compare(pcol, col);

            // try horizontal then vertical, then diagonals, then random neighbor
            int[][] tries = {
                    {row + dr, col},
                    {row, col + dc},
                    {row + dr, col + dc},
                    {row+1, col}, {row-1, col}, {row, col+1}, {row, col-1}
            };

            for (int[] t : tries) {
                int rr = t[0], cc = t[1];
                if (rr < 0 || rr >= ROWS || cc < 0 || cc >= COLS) continue;
                if (maze[rr][cc] == 0) {
                    row = rr; col = cc;
                    return;
                }
            }
        }
    }

    // ------------------ main --------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Maze Game - Medium (Clean)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            G10_Room1_PD4 panel = new G10_Room1_PD4();
            f.add(panel);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}


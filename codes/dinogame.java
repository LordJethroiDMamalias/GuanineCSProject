/*Sir this game idea is from the google dino game and we got this code from chatgpt. We just replaced the plain color bg to our assets. */

package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.IOException;

public class dinogame extends JPanel implements ActionListener, KeyListener {

    static final int WIDTH = 800;
    static final int HEIGHT = 300;

    private final Timer timer = new Timer(16, this);

    // Runner
    private final int dinoWidth = 60;
    private final int dinoHeight = 60;
    private double dinoX = 80;
    private double dinoY = HEIGHT - dinoHeight - 30;
    private double velocityY = 0;

    // 🔹 Jump tuning
    private final double gravity = 1;        // lower gravity = smoother float
    private final double jumpVelocity = -18;  // stronger jump
    private boolean onGround = true;

    // Obstacles
    private final ArrayList<Rectangle> obstacles = new ArrayList<>();
    private final Random rand = new Random();
    private int ticksSinceLastObstacle = 0;
    private int obstacleSpawnInterval = 90;
    private double gameSpeed = 5.0;

    // Score
    private int score = 0;
    private int highScore = 0;

    // Game states
    private boolean running = true;
    private boolean gameOver = false;
    private boolean started = false;
    private boolean gameWin = false;
    private final int WIN_SCORE = 1000;

    // Images
    private BufferedImage runnerImg;
    private BufferedImage hurdleImg;
    private BufferedImage trackImg;

    // Track scrolling
    private int trackX1 = 0;
    private int trackX2 = WIDTH;

    public dinogame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        spawnObstacle();
        timer.start();
    }

    private void loadImages() {
        try {
            runnerImg = ImageIO.read(new java.io.File("images/G6_runner.png"));
            hurdleImg = ImageIO.read(new java.io.File("images/G6_hurdle.png"));
            trackImg  = ImageIO.read(new java.io.File("images/G6_track.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Track background
        g2.drawImage(trackImg, trackX1, 0, WIDTH, HEIGHT, null);
        g2.drawImage(trackImg, trackX2, 0, WIDTH, HEIGHT, null);

        // Runner
        g2.drawImage(runnerImg, (int)dinoX, (int)dinoY, dinoWidth, dinoHeight, null);

        // Hurdles
        for (Rectangle ob : obstacles) {
            g2.drawImage(hurdleImg, ob.x, ob.y, ob.width, ob.height, null);
        }

        // Score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Score: " + score, 20, 25);
        g2.drawString("High: " + highScore, 20, 45);

        // Start screen
        if (!started) {
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            g2.drawString("Press SPACE to Start (Reach 1000 to Exit)", WIDTH/2 - 120, HEIGHT/2);
        }

        // Game Over
        if (gameOver) {
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            g2.drawString("GAME OVER", WIDTH/2 - 100, HEIGHT/2);
            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            g2.drawString("Press R to Restart", WIDTH/2 - 90, HEIGHT/2 + 30);
        }

        // Win
        if (gameWin) {
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            g2.drawString("YOU WIN!", WIDTH/2 - 80, HEIGHT/2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;

        if (started && !gameOver && !gameWin) {

            // Scroll track
            trackX1 -= gameSpeed;
            trackX2 -= gameSpeed;
            if (trackX1 + WIDTH <= 0) trackX1 = trackX2 + WIDTH;
            if (trackX2 + WIDTH <= 0) trackX2 = trackX1 + WIDTH;

            // Runner physics
            velocityY += gravity;
            dinoY += velocityY;
            int groundY = HEIGHT - dinoHeight - 30;

            if (dinoY >= groundY) {
                dinoY = groundY;
                velocityY = 0;
                onGround = true;
            } else onGround = false;

            // Spawn obstacles
            ticksSinceLastObstacle++;
            if (ticksSinceLastObstacle > obstacleSpawnInterval) {
                spawnObstacle();
                ticksSinceLastObstacle = 0;
                obstacleSpawnInterval = 70 + rand.nextInt(80);
            }

            // Move obstacles + collision
            Iterator<Rectangle> it = obstacles.iterator();
            while (it.hasNext()) {
                Rectangle ob = it.next();
                ob.x -= (int)gameSpeed;

                if (ob.x + ob.width < 0) it.remove();

                // 🔹 Smaller collision box for fairer jumps
                Rectangle playerHitbox = new Rectangle(
                        (int)dinoX + 8,
                        (int)dinoY + 5,
                        dinoWidth - 16,
                        dinoHeight - 10
                );

                Rectangle obstacleHitbox = new Rectangle(
                        ob.x + 8,
                        ob.y + 5,
                        ob.width - 16,
                        ob.height - 10
                );

                if (playerHitbox.intersects(obstacleHitbox)) {
                    gameOver = true;
                    running = false;
                    timer.stop();
                    if (score > highScore) highScore = score;
                }
            }

            // Difficulty scaling
            if (score % 300 == 0 && score != 0) gameSpeed += 0.3;

            // Win condition
            if (score >= WIN_SCORE) {
                running = false;       
                SwingUtilities.invokeLater(() -> {
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w != null) w.dispose();
                });

                return; 
            }


            score++;
        }

        repaint();
    }

    private void spawnObstacle() {
        int size = 45 + rand.nextInt(20);
        obstacles.add(new Rectangle(WIDTH + 40, HEIGHT - size - 30, size, size));
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (!started && k == KeyEvent.VK_SPACE) started = true;

        if ((k == KeyEvent.VK_SPACE || k == KeyEvent.VK_UP) && onGround && started && !gameOver) {
            velocityY = jumpVelocity;
        }

        if (k == KeyEvent.VK_R && gameOver) resetGame();
    }

    private void resetGame() {
        dinoY = HEIGHT - dinoHeight - 30;
        velocityY = 0;
        onGround = true;
        obstacles.clear();
        score = 0;
        gameSpeed = 5.0;
        ticksSinceLastObstacle = 0;
        gameOver = false;
        gameWin = false;
        started = false;
        running = true;
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Track Runner");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new dinogame());
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

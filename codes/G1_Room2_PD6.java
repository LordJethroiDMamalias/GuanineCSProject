package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class G1_Room2_PD6 extends JPanel implements KeyListener {

    // ── SaveSystem ──
    private static SaveSystem.SaveData saved;

    // ── State ──
    private boolean isPaused        = false;
    private boolean bossMessageShown = false;
    private boolean hasWon          = false;

    private final Battle battle = new Battle();

    // ── Images ──
    private Image kirbo;
    private Image shooter;
    private Image redProjectile;
    private Image pea;
    private Image wallpaper;
    private Image lawnmowe;
    private Image boss1, boss2, boss3, boss4;

    // ── Walk animation frames ──
    private final Image[] walkUp    = new Image[4];
    private final Image[] walkDown  = new Image[4];
    private final Image[] walkLeft  = new Image[4];
    private final Image[] walkRight = new Image[4];
    private int kirbyDirection  = 1; // 0=up 1=down 2=left 3=right
    private int kirbyWalkFrame  = 0;

    // ── Grid ──
    private final int colSize  = 16;
    private final int gridSize = 12;

    // ── Enemy shooters ──
    private final int kirboX = 1,  kirboY = 1;
    private final int kirboX2 = 1, kirboY2 = 5;
    private final int kirboX3 = 1, kirboY3 = 7;
    private final int kirboX4 = 1, kirboY4 = 9;
    private final int kirboX5 = 5, kirboY5 = 0;
    private final int kirboX7 = 9, kirboY7 = 0;
    private final int kirboX10 = 3, kirboY10 = 0;
    private final int kirboX11 = 11, kirboY11 = 0;

    // ── Player ──
    private int kirbyX = 0;
    private int kirbyY = 11;

    // ── Projectiles ──
    private final List<Projectile> projectiles = new ArrayList<>();

    // ── Lawn mower solid tiles (col, row) ──
    private final List<Point> solidTiles = List.of(
            new Point(0, 1),
            new Point(0, 3),
            new Point(0, 5),
            new Point(0, 7),
            new Point(0, 9)
    );

    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @param startMusic pass {@code true} when launching Room2 directly (standalone),
     *                   {@code false} when transitioning from Room1 so music keeps playing.
     */
    public G1_Room2_PD6(boolean startMusic) {
        if (startMusic) {
            G1_Room1_PD4.startMusicIfNeeded(); // starts only if not already running
        }
        // If startMusic == false the shared clip from Room1 is already looping — do nothing.

        saved = SaveSystem.loadGame("G1_Room2_PD6");
        SaveSystem.startTimer(saved.timeSeconds);
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G1_Room2_PD6")
                .battles(SaveSystem.getDefeatedBosses())
        );

        setSize(660, 660);

        int cellWidth  = 660 / colSize;
        int cellHeight = 660 / gridSize;

        // Walk frames
        for (int i = 0; i < 4; i++) {
            walkUp[i]    = new ImageIcon("images/up_"    + (i+1) + ".png").getImage()
                               .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
            walkDown[i]  = new ImageIcon("images/down_"  + (i+1) + ".png").getImage()
                               .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
            walkLeft[i]  = new ImageIcon("images/left_"  + (i+1) + ".png").getImage()
                               .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
            walkRight[i] = new ImageIcon("images/right_" + (i+1) + ".png").getImage()
                               .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
        }

        // Other images
        kirbo         = new ImageIcon("images/G1_peashooter_right.png").getImage();
        redProjectile = new ImageIcon("images/G1_basketball.png").getImage();
        shooter       = new ImageIcon("images/G1_bestpeashooter.jpg").getImage();
        pea           = new ImageIcon("images/G1_pea.png").getImage();
        wallpaper     = new ImageIcon("images/G1_viness.png").getImage();
        lawnmowe      = new ImageIcon("images/G1_lawnmowe.jpg").getImage();
        boss1         = new ImageIcon("images/G1_boss1.jpg").getImage();
        boss2         = new ImageIcon("images/G1_boss2.jpg").getImage();
        boss3         = new ImageIcon("images/G1_boss3.jpg").getImage();
        boss4         = new ImageIcon("images/G1_boss4.jpg").getImage();

        setFocusable(true);
        addKeyListener(this);

        // Grab focus once the window is shown
        new Timer(10, e -> {
            if (!hasWon) requestFocusInWindow();
            ((Timer) e.getSource()).stop();
        }).start();

        // Game-loop timer (~60 fps)
        new Timer(16, e -> {
            updateProjectiles();
            repaint();
        }).start();

        // Enemy fire timer
        new Timer(300 + (int)(Math.random() * 400), e -> fireProjectile()).start();
    }

    /** Convenience: launched standalone (or from Room1 that already manages music). */
    public G1_Room2_PD6() {
        this(false); // music was already started by Room1
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G1_Room2_PD6")
                .battles(SaveSystem.getDefeatedBosses())
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    private class Projectile {
        double x, y, dx, dy;
        Image  img;

        Projectile(double x, double y, double dx, double dy, Image img) {
            this.x = x; this.y = y;
            this.dx = dx; this.dy = dy;
            this.img = img;
        }

        void update() { x += dx; y += dy; }
    }

    private void fireProjectile() {
        if (hasWon) return;

        int cw = getWidth()  / colSize;
        int ch = getHeight() / gridSize;

        // Horizontal shooters (left side, fire right)
        projectiles.add(new Projectile(kirboX  * cw + cw / 2.0, kirboY  * ch + ch / 2.0, Math.random() + 2, 0, pea));
        projectiles.add(new Projectile(kirboX2 * cw + cw / 2.0, kirboY2 * ch + ch / 2.0, Math.random() + 2, 0, pea));
        projectiles.add(new Projectile(kirboX3 * cw + cw / 2.0, kirboY3 * ch + ch / 2.0, Math.random() + 2, 0, pea));
        projectiles.add(new Projectile(kirboX4 * cw + cw / 2.0, kirboY4 * ch + ch / 2.0, Math.random() + 2, 0, pea));

        // Top shooters (fire down)
        projectiles.add(new Projectile(kirboX5  * cw + cw / 2.0, kirboY5  * ch + ch / 2.0, 0, Math.random() + 3, redProjectile));
        projectiles.add(new Projectile(kirboX7  * cw + cw / 2.0, kirboY7  * ch + ch / 2.0, 0, Math.random() + 3, redProjectile));
        projectiles.add(new Projectile(kirboX10 * cw + cw / 2.0, kirboY10 * ch + ch / 2.0, 0, Math.random() + 3, redProjectile));
        projectiles.add(new Projectile(kirboX11 * cw + cw / 2.0, kirboY11 * ch + ch / 2.0, 0, Math.random() + 3, redProjectile));
    }

    private boolean isColliding(int x1, int y1, int w1, int h1,
                                double x2, double y2, int size2) {
        return new Rectangle(x1, y1, w1, h1).intersects(
               new Rectangle((int) x2, (int) y2, size2, size2));
    }

    private void updateProjectiles() {
        if (hasWon) return;

        int cw = getWidth()  / colSize;
        int ch = getHeight() / gridSize;

        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (!isPaused) p.update();

            // Hit player → reset
            if (isColliding(kirbyX * cw, kirbyY * ch, cw, ch, p.x, p.y, 20)) {
                kirbyX = 0;
                kirbyY = 11;
                it.remove();
                continue;
            }

            // Off-screen
            if (p.x > getWidth() || p.y > getHeight()) it.remove();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cw = getWidth()  / colSize;
        int ch = getHeight() / gridSize;

        // Wallpaper
        for (int row = 0; row < gridSize; row++)
            for (int col = 0; col < colSize; col++)
                g.drawImage(wallpaper, col * cw, row * ch, cw, ch, null);

        // Enemy shooters
        g.drawImage(kirbo,   kirboX   * cw, kirboY   * ch, cw, ch, null);
        g.drawImage(kirbo,   kirboX2  * cw, kirboY2  * ch, cw, ch, null);
        g.drawImage(kirbo,   kirboX3  * cw, kirboY3  * ch, cw, ch, null);
        g.drawImage(kirbo,   kirboX4  * cw, kirboY4  * ch, cw, ch, null);
        g.drawImage(shooter, kirboX5  * cw, kirboY5  * ch, cw, ch, null);
        g.drawImage(shooter, kirboX7  * cw, kirboY7  * ch, cw, ch, null);
        g.drawImage(shooter, kirboX10 * cw, kirboY10 * ch, cw, ch, null);
        g.drawImage(shooter, kirboX11 * cw, kirboY11 * ch, cw, ch, null);

        // Animated player
        Image kirbyImage = switch (kirbyDirection) {
            case 0  -> walkUp[kirbyWalkFrame];
            case 2  -> walkLeft[kirbyWalkFrame];
            case 3  -> walkRight[kirbyWalkFrame];
            default -> walkDown[kirbyWalkFrame];
        };
        g.drawImage(kirbyImage, kirbyX * cw, kirbyY * ch, cw, ch, null);

        // Projectiles
        for (Projectile p : projectiles)
            g.drawImage(p.img, (int) p.x, (int) p.y, 20, 20, null);

        // Lawn mowers
        for (Point p : solidTiles)
            g.drawImage(lawnmowe, p.x * cw, p.y * ch + 5, cw, ch, null);

        // Boss tiles (2×2 at col 7-8, row 2-3)
        g.drawImage(boss1, 7 * cw, 2 * ch, cw, ch, null);
        g.drawImage(boss2, 8 * cw, 2 * ch, cw, ch, null);
        g.drawImage(boss3, 7 * cw, 3 * ch, cw, ch, null);
        g.drawImage(boss4, 8 * cw, 3 * ch, cw, ch, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    /** Polls until the battle screen is closed (Battle.paused == false). */
    private void waitForBattleEnd(Runnable onEnd) {
        Timer[] holder = {null};
        holder[0] = new Timer(200, e -> {
            if (!Battle.paused) {
                holder[0].stop();
                onEnd.run();
            }
        });
        holder[0].start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Toggle pause
        if (key == KeyEvent.VK_P) {
            isPaused = !isPaused;
            repaint();
            return;
        }

        if (hasWon) return;

        int oldX  = kirbyX;
        int oldY  = kirbyY;
        int nextX = kirbyX;
        int nextY = kirbyY;

        switch (key) {
            case KeyEvent.VK_W    -> nextY--;
            case KeyEvent.VK_S  -> nextY++;
            case KeyEvent.VK_A  -> nextX--;
            case KeyEvent.VK_D -> nextX++;
        }

        // Bounds check
        if (nextX < 0 || nextX >= colSize || nextY < 0 || nextY >= gridSize)
            return;

        // Solid tile (lawnmower) check
        boolean blocked = false;
        for (Point p : solidTiles)
            if (p.x == nextX && p.y == nextY) { blocked = true; break; }

        if (!blocked) { kirbyX = nextX; kirbyY = nextY; }

        // Animation
        if (kirbyX != oldX || kirbyY != oldY) {
            if      (kirbyY < oldY) kirbyDirection = 0;
            else if (kirbyY > oldY) kirbyDirection = 1;
            else if (kirbyX < oldX) kirbyDirection = 2;
            else if (kirbyX > oldX) kirbyDirection = 3;
            kirbyWalkFrame = (kirbyWalkFrame + 1) % 4;
        }

        // ── Reach boss area → trigger battle ──
        if ((kirbyX == 8 || kirbyX == 7) && kirbyY == 4) {
            JOptionPane.showMessageDialog(this, "Now starting battle...");

            JFrame top = (JFrame) SwingUtilities.getWindowAncestor(this);
            G1_Room1_PD4.stopMusic();
            battle.start(top, "G1_viness.png", "IVy");

            waitForBattleEnd(() -> {
                if (battle.didPlayerWin()) {
                    if (!SaveSystem.isDefeated("IVy")) {
                        SaveSystem.markDefeated("IVy");
                        hasWon = true;
                    }
                } else {
                    fullReset();
                }
                saveProgress();

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.revalidate();
                frame.repaint();
                
                if (hasWon == true) {
                   frame.dispose();
                   SwingUtilities.invokeLater(() -> new G2_Room1_PD4().setFrame());
                }
            });
        }

        // Tip prompt at far right
        if (kirbyX == 15 && kirbyY == 10)
            JOptionPane.showMessageDialog(this, "Press P to stop projectiles");

        // Intro message
        if ((kirbyX == 0 && kirbyY == 10 || kirbyX == 1 && kirbyY == 11)
                && !bossMessageShown) {
            JOptionPane.showMessageDialog(this,
                    "Now... make your way through the defenses to fight the IVy");
            bossMessageShown = true;
        }

        repaint();
    }
    
    private void fullReset() {
        // Stop music (optional but recommended)
        G1_Room1_PD4.stopMusic();

        // Close current window
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.dispose();

        // Restart the whole app fresh
        SwingUtilities.invokeLater(() -> {
            main(new String[]{}); // relaunch app
        });
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        G1_Room2_PD6 panel = new G1_Room2_PD6(true); // standalone: start music
        frame.setContentPane(panel);
        frame.setSize(660, 660);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }
}
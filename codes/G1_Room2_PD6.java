package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sound.sampled.*;
import java.io.File;

public class G1_Room2_PD6 extends JPanel implements KeyListener {
    private Clip clip;
    
    private boolean isPaused = false;
    private boolean bossMessageShown = false;
    private Battle battle = new Battle();
    private static SaveSystem.SaveData saved;

    // Kirby animation frames (pixil‑style)
    private final Image[] walkUp    = new Image[4];
    private final Image[] walkDown  = new Image[4];
    private final Image[] walkLeft  = new Image[4];
    private final Image[] walkRight = new Image[4];

    private Image kirbo;
    private Image shooter;
    private Image redProjectile;
    private Image pea;
    private Image peashooter_left;
    private Image wallpaper;
    private Image lawnmowe;
    private Image boss1;
    private Image boss2;
    private Image boss3;
    private Image boss4;

    private final int colSize = 16;
    private final int gridSize = 12;

    private int kirboX = 1;
    private int kirboY = 1;
    private int kirboX2 = 1;
    private int kirboY2 = 5;
    private int kirboX3 = 1;
    private int kirboY3 = 7;
    private int kirboX4 = 1;
    private int kirboY4 = 9;
    private int kirboX5 = 5;
    private int kirboY5 = 0;
    private int kirboX7 = 9;
    private int kirboY7 = 0;
    private int kirboX10 = 3;
    private int kirboY10 = 0;
    private int kirboX11 = 11;
    private int kirboY11 = 0;

    private int kirbyX = 0;
    private int kirbyY = 11;

    private int kirbyDirection = 1; // 0=up, 1=down, 2=left, 3=right
    private int kirbyWalkFrame = 0; // 0→1→2→3

    private boolean hasWon = false;

    private final List<Projectile> projectiles = new ArrayList<>();

    // Lawn mower tiles (solid tiles Kirby can't walk on)
    private final List<Point> solidTiles = List.of(
            new Point(0, 1),
            new Point(0, 3),
            new Point(0, 5),
            new Point(0, 7),
            new Point(0, 9)
            
           
    );

    public G1_Room2_PD6() {
        playMusic();
       saved = SaveSystem.loadGame("PD6");
    SaveSystem.startTimer(saved.timeSeconds);
        
     
  
        setSize(660, 660);

        int cellWidth = 660 / colSize;
        int cellHeight = 660 / gridSize;

        for (int i = 0; i < 4; i++) {
            walkUp[i]    = new ImageIcon("pixil-up_" + (i + 1) + ".png").getImage()
                                         .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
            walkDown[i]  = new ImageIcon("pixil-down_" + (i + 1) + ".png").getImage()
                                         .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
            walkLeft[i]  = new ImageIcon("pixil-left_" + (i + 1) + ".png").getImage()
                                         .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
            walkRight[i] = new ImageIcon("pixil-right_" + (i + 1) + ".png").getImage()
                                         .getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH);
        }
        
        

        kirbo = new ImageIcon("images/G1_peashooter_right.png").getImage();
redProjectile = new ImageIcon("images/G1_basketball.png").getImage();
shooter = new ImageIcon("images/G1_bestpeashooter.jpg").getImage();
pea = new ImageIcon("images/G1_pea.png").getImage();
wallpaper = new ImageIcon("images/G1_viness.png").getImage();
peashooter_left = new ImageIcon("images/G1_peashooter_left.png").getImage();
lawnmowe = new ImageIcon("images/G1_lawnmowe.jpg").getImage();

boss1 = new ImageIcon("images/G1_boss1.jpg").getImage();
boss2 = new ImageIcon("images/G1_boss2.jpg").getImage();
boss3 = new ImageIcon("images/G1_boss3.jpg").getImage();
boss4 = new ImageIcon("images/G1_boss4.jpg").getImage();

        setFocusable(true);
        addKeyListener(this);

        // Ensure panel gets focus after window shows
        Timer focusTimer = new Timer(10, e -> {
            if (!hasWon) {
                requestFocusInWindow();
            }
        });
        focusTimer.setRepeats(false);
        focusTimer.start();

        Timer timer = new Timer(16, e -> {
            updateProjectiles();
            repaint();
        });
        timer.start();

        Timer fireTimer = new Timer((int) (Math.random() * 1000), e -> fireProjectile());
        fireTimer.start();
    }
    
    public void playMusic() {
    try {
        File file = new File("music/IVy.wav");

        if (!file.exists()) {
            System.out.println("Music file not found: " + file.getAbsolutePath());
            return;
        }

        AudioInputStream audio = AudioSystem.getAudioInputStream(file);

        clip = AudioSystem.getClip();
        clip.open(audio);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    private void saveProgress() {
    SaveSystem.saveGame(
        new SaveSystem.SaveData.Builder("PD6")
            .battles(SaveSystem.getDefeatedBosses())
    );
}

    private class Projectile {
        double x, y, dx, dy;
        Image img;

        public Projectile(double x, double y, double dx, double dy, Image img) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.img = img;
        }

        public void update() {
            x += dx;
            y += dy;
        }
    }

    private void fireProjectile() {
        if (hasWon) return;

        int cellWidth = getWidth() / colSize;
        int cellHeight = getHeight() / gridSize;

        double px  = kirboX  * cellWidth + cellWidth / 2.0;
        double py  = kirboY  * cellHeight + cellHeight / 2.0;
        double px2 = kirboX2 * cellWidth + cellWidth / 2.0;
        double py2 = kirboY2 * cellHeight + cellHeight / 2.0;
        double px3 = kirboX3 * cellWidth + cellWidth / 2.0;
        double py3 = kirboY3 * cellHeight + cellHeight / 2.0;
        double px4 = kirboX4 * cellWidth + cellWidth / 2.0;
        double py4 = kirboY4 * cellHeight + cellHeight / 2.0;
        double px5 = kirboX5 * cellWidth + cellWidth / 2.0;
        double py5 = kirboY5 * cellHeight + cellHeight / 2.0;

        double px7 = kirboX7 * cellWidth + cellWidth / 2.0;
        double py7 = kirboY7 * cellHeight + cellHeight / 2.0;

        double px10 = kirboX10 * cellWidth + cellWidth / 2.0;
        double py10 = kirboY10 * cellHeight + cellHeight / 2.0;
        double px11 = kirboX11 * cellWidth + cellWidth / 2.0;
        double py11 = kirboY11 * cellHeight + cellHeight / 2.0;

        projectiles.add(new Projectile(px + 10, py - 20, Math.random() + 2, 0, pea));
        projectiles.add(new Projectile(px2 + 10, py2 - 20, Math.random() + 2, 0, pea));
        projectiles.add(new Projectile(px3 + 10, py3 - 20, Math.random() + 2, 0, pea));
        projectiles.add(new Projectile(px4 + 10, py4 - 20, Math.random() + 2, 0, pea));
        projectiles.add(new Projectile(px5 - 10, py5 + 20, 0, Math.random() + 3, redProjectile));

        projectiles.add(new Projectile(px7 - 10, py7 + 20, 0, Math.random() + 3, redProjectile));

        projectiles.add(new Projectile(px10 - 10, py10 + 20, 0, Math.random() + 3, redProjectile));
        projectiles.add(new Projectile(px11 - 10, py11 + 20, 0, Math.random() + 3, redProjectile));
    }

    private boolean isColliding(int x1, int y1, int width1, int height1,
                                double x2, double y2, int size2) {
        Rectangle rect1 = new Rectangle(x1, y1, width1, height1);
        Rectangle rect2 = new Rectangle((int) x2, (int) y2, size2, size2);
        return rect1.intersects(rect2);
    }

    private void updateProjectiles() {
        if (hasWon) return;

        int cellWidth = getWidth() / colSize;
        int cellHeight = getHeight() / gridSize;

        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();

            if (!isPaused) {
                p.update();
            }

            if (isColliding(kirbyX * cellWidth, kirbyY * cellHeight,
                    cellWidth, cellHeight, p.x, p.y, 20)) {
                kirbyX = 0;
                kirbyY = 11;
                it.remove();
                continue;
            }

            if (p.x > getWidth() || p.y > getHeight()) {
                it.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellWidth = getWidth() / colSize;
        int cellHeight = getHeight() / gridSize;

        // Wallpaper
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < colSize; col++) {
                g.drawImage(wallpaper, col * cellWidth, row * cellHeight, cellWidth, cellHeight, null);
            }
        }

        // kirbo / shooters (no animation)
        g.drawImage(kirbo, kirboX * cellWidth, kirboY * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(kirbo, kirboX2 * cellWidth, kirboY2 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(kirbo, kirboX3 * cellWidth, kirboY3 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(kirbo, kirboX4 * cellWidth, kirboY4 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(shooter, kirboX5 * cellWidth, kirboY5 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(shooter, kirboX7 * cellWidth, kirboY7 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(shooter, kirboX10 * cellWidth, kirboY10 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(shooter, kirboX11 * cellWidth, kirboY11 * cellHeight, cellWidth, cellHeight, null);

        // Animated Kirby (pixil‑style frames)
        Image kirbyImage = switch (kirbyDirection) {
            case 0 -> walkUp[kirbyWalkFrame];
            case 1 -> walkDown[kirbyWalkFrame];
            case 2 -> walkLeft[kirbyWalkFrame];
            case 3 -> walkRight[kirbyWalkFrame];
            default -> walkDown[kirbyWalkFrame];
        };

        g.drawImage(kirbyImage,
                kirbyX * cellWidth,
                kirbyY * cellHeight,
                cellWidth,
                cellHeight, null);

        // Projectiles
        for (Projectile p : projectiles) {
            g.drawImage(p.img, (int) p.x, (int) p.y, 20, 20, null);
        }

        // Lawnmowers (solid tiles)
        for (Point p : solidTiles) {
            g.drawImage(lawnmowe, p.x * cellWidth, p.y * cellHeight + 5, cellWidth, cellHeight, null);
        }

        // Boss tiles
        g.drawImage(boss1, 7 * cellWidth, 2 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(boss2, 8 * cellWidth, 2 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(boss3, 7 * cellWidth, 3 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(boss4, 8 * cellWidth, 3 * cellHeight, cellWidth, cellHeight, null);
    }

private void waitForBattleEnd(Runnable onEnd) {
    Timer[] holder = {null};
    holder[0] = new Timer(200, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!Battle.paused) {
                holder[0].stop();
                onEnd.run();
            }
        }
    });
    holder[0].start();
}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            isPaused = !isPaused;
            repaint();
            return;
        }

        if (hasWon) return;

        int oldX = kirbyX;
        int oldY = kirbyY;

        int nextX = kirbyX;
        int nextY = kirbyY;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> nextY = kirbyY - 1;
            case KeyEvent.VK_DOWN  -> nextY = kirbyY + 1;
            case KeyEvent.VK_LEFT  -> nextX = kirbyX - 1;
            case KeyEvent.VK_RIGHT -> nextX = kirbyX + 1;
        }

        
        if (e.getKeyCode() == KeyEvent.VK_S) {
    saveProgress();
    JOptionPane.showMessageDialog(this, "Game saved!");
    return;
}
        
        // Check bounds
        if (nextX < 0 || nextX >= colSize || nextY < 0 || nextY >= gridSize) {
            return; // out of map, no move
        }

        // Check solid tiles (lawnmowers)
        boolean blocked = false;
        for (Point p : solidTiles) {
            if (p.x == nextX && p.y == nextY) {
                blocked = true;
                break;
            }
        }

        if (!blocked) {
            kirbyX = nextX;
            kirbyY = nextY;
        }

        // Update animation if actually moved
        if (kirbyX != oldX || kirbyY != oldY) {
            if (kirbyY < oldY)      kirbyDirection = 0; // up
            else if (kirbyY > oldY) kirbyDirection = 1; // down
            else if (kirbyX < oldX) kirbyDirection = 2; // left
            else if (kirbyX > oldX) kirbyDirection = 3; // right

            kirbyWalkFrame = (kirbyWalkFrame + 1) % 4; // 0→1→2→3
        }

if ((kirbyX == 8 && kirbyY == 4) || (kirbyX == 7 && kirbyY == 4)) {
    hasWon = true;
    JOptionPane.showMessageDialog(this, "You Win! Now starting battle...");

    JFrame top = (JFrame) SwingUtilities.getWindowAncestor(this);
    battle.start(top, "viness.png", "IVy");   // battle opens

    waitForBattleEnd(() -> {
        // Mark IVy as permanently defeated (only once)
        if (!SaveSystem.isDefeated("IVy")) {
            SaveSystem.markDefeated("IVy");
        }

        // Save the defeat to saveFile.txt
        saveProgress();

        // Then switch map or reload
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.setContentPane(new G1_Room2_PD6());   // or new G2_Room2_PD6()
        frame.revalidate();
        frame.repaint();
    });
}
        
        if (kirbyX == 15 && kirbyY == 10) {
            JOptionPane.showMessageDialog(this, "press P to stop projectiles");
        }
        if (((kirbyX == 0 && kirbyY == 10) || (kirbyX == 1 && kirbyY == 11))
            && !bossMessageShown) {
            JOptionPane.showMessageDialog(this, "Now... make your way through the defenses to fight the IVy");
            bossMessageShown = true;
        }

        if (kirboX == 15 && kirboY == 11) {
            JFrame top = (JFrame) SwingUtilities.getWindowAncestor(this);
            top.setContentPane(new G1_Room2_PD6());
            top.revalidate();
            top.repaint();
        }

        repaint();
    }
 

    


    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Kirbo Shoots Projectiles");
        G1_Room2_PD6 panel = new G1_Room2_PD6();

        frame.setContentPane(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
        });
    }
}
    


    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Kirbo Shoots Projectiles");
        G1_Room2_PD6 panel = new G1_Room2_PD6();

        frame.setContentPane(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            panel.requestFocusInWindow();
        });
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// these are not our final sprites

public class Test extends JPanel implements KeyListener {
private boolean isPaused = false;

    private final Image kirbo;
    private final Image shooter;
    private final Image redProjectile;
    private final Image movableKirbo;
    private final Image pea;
    private final Image peashooter_left;
    private final Image wallpaper;
    private final Image lawnmowe;

    private final Image boss1;
    private final Image boss2;
    private final Image boss3;
    private final Image boss4;

    private final int colSize = 16;
    private final int gridSize = 12;
private boolean bossMessageShown = false;

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

    private int movableX = 0;
    private int movableY = 11;

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

    public Test() {
        kirbo = new ImageIcon("peashooter_right.png").getImage();
        redProjectile = new ImageIcon("basketball.png").getImage();
        shooter = new ImageIcon("bestpeashooter.jpg").getImage();
        movableKirbo = new ImageIcon("ogkirby.png").getImage();
        pea = new ImageIcon("pea.png").getImage();
        wallpaper = new ImageIcon("vines.png").getImage();
        peashooter_left = new ImageIcon("peashooter_left.png").getImage();
        lawnmowe = new ImageIcon("lawnmowe.jpg").getImage();

        boss1 = new ImageIcon("boss1.jpg").getImage();
        boss2 = new ImageIcon("boss2.jpg").getImage();
        boss3 = new ImageIcon("boss3.jpg").getImage();
        boss4 = new ImageIcon("boss4.jpg").getImage();

        setFocusable(true);
        addKeyListener(this);

        Timer timer = new Timer(16, e -> {
            updateProjectiles();
            repaint();
        });
        timer.start();

        Timer fireTimer = new Timer((int) (Math.random() * 900), e -> fireProjectile());
        fireTimer.start();
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

        double px = kirboX * cellWidth + cellWidth / 2.0;
        double py = kirboY * cellHeight + cellHeight / 2.0;

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
        
        
        double px10 = kirboX10 * cellHeight + cellHeight / 2.0;
        double py10 = kirboY10 * cellHeight + cellHeight / 2.0;
        double px11 = kirboX11 * cellWidth + cellHeight / 2.0;
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

        // 🔹 Only move if not paused
        if (!isPaused) {
            p.update();
        }

        // 🔹 ALWAYS check collision (even if paused)
        if (isColliding(movableX * cellWidth, movableY * cellHeight,
                cellWidth, cellHeight, p.x, p.y, 20)) {

            movableX = 0;
            movableY = 11;
            it.remove();
            continue;
        }

        // Remove if off screen
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

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < colSize; col++) {
                g.drawImage(wallpaper, col * cellWidth, row * cellHeight, cellWidth, cellHeight, null);
            }
        }

        // Draw static characters
        g.drawImage(kirbo, kirboX * cellWidth, kirboY * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(kirbo, kirboX2 * cellWidth, kirboY2 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(kirbo, kirboX3 * cellWidth, kirboY3 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(kirbo, kirboX4 * cellWidth, kirboY4 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(shooter, kirboX5 * cellWidth, kirboY5 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(shooter, kirboX7 * cellWidth, kirboY7 * cellHeight, cellWidth, cellHeight, null);
   
       
        g.drawImage(shooter, kirboX10 * cellWidth, kirboY10 * cellHeight, cellWidth, cellHeight, null);
        g.drawImage(shooter, kirboX11 * cellWidth, kirboY11 * cellHeight, cellWidth, cellHeight, null);

        // Draw movable Kirby
        g.drawImage(movableKirbo, movableX * cellWidth, movableY * cellHeight, cellWidth, cellHeight, null);

        // Draw projectiles
        for (Projectile p : projectiles) {
            g.drawImage(p.img, (int) p.x, (int) p.y, 20, 20, null);
        }

        // Draw lawnmowers (solid tiles)
        for (Point p : solidTiles) {
            g.drawImage(lawnmowe, p.x * cellWidth, p.y * cellHeight+5, cellWidth, cellHeight, null);
        }

        g.drawImage(boss1, 7 * cellWidth, 2 * cellHeight, cellWidth, cellHeight, null);
g.drawImage(boss2, 8 * cellWidth, 2 * cellHeight, cellWidth, cellHeight, null);
g.drawImage(boss3, 7 * cellWidth, 3 * cellHeight, cellWidth, cellHeight, null);
g.drawImage(boss4, 8 * cellWidth, 3 * cellHeight, cellWidth, cellHeight, null);

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
    isPaused = !isPaused; // toggle pause
    repaint();
    return;
}

        if (!hasWon) {
           int nextY = movableY;
           int nextX = movableX;
           

            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP -> nextY = movableY - 1;
                case KeyEvent.VK_DOWN -> nextY = movableY + 1;
                case KeyEvent.VK_LEFT -> nextX = movableX - 1;
                case KeyEvent.VK_RIGHT -> nextX = movableX + 1;
                
                
            }
// Check boundaries
if (nextX >= 0 && nextX < colSize &&
    nextY >= 0 && nextY < gridSize) {

    // Check solid tiles (lawnmowers)
    boolean blocked = false;
    for (Point p : solidTiles) {
        if (p.x == nextX && p.y == nextY) {
            blocked = true;
            break;
        }
    }

    if (!blocked) {
        movableX = nextX;
        movableY = nextY;
    }
}

            

            

            // Win condition
            if ((movableX == 8 && movableY == 4) || (movableX == 7 && movableY == 4)) {
                hasWon = true;
                JOptionPane.showMessageDialog(this, "You Win!");
            }
            if ((movableX == 15 && movableY == 10)) {
                
                JOptionPane.showMessageDialog(this, "press P to stop projectiles");
            }
            if (((movableX == 0 && movableY == 10) || (movableX == 1 && movableY == 11))
        && !bossMessageShown) {

    JOptionPane.showMessageDialog(this, "Go in front of the final boss");
    bossMessageShown = true;
}

            

            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Kirbo Shoots Projectiles");
        Test panel = new Test();
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

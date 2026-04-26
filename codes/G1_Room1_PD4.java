package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;
import java.io.File;

public class G1_Room1_PD4 extends JFrame {
    private Clip clip;

    // ─────────────── SaveSystem hook ───────────────
    private static SaveSystem.SaveData saved;

    private void switchMap() {
        setContentPane(new G1_Room2_PD6());
        revalidate();
        repaint();
    }
    
    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new G1_Room1_PD4();
    });
}
    
    public void playMusic() {
    try {
        File file = new File("music/IVy.wav");

        AudioInputStream audio = AudioSystem.getAudioInputStream(file);

        clip = AudioSystem.getClip();
        clip.open(audio);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void stopMusic() {
    if (clip != null) {
        clip.stop();
        clip.close();
    }
}

    public G1_Room1_PD4() {
        playMusic();
        // Load save FIRST
        saved = SaveSystem.loadGame("PD4");
        SaveSystem.startTimer(saved.timeSeconds);

        setTitle("Group One");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanel panel = new GamePanel();

        setContentPane(panel);

        setSize(660, 660);
        setLocationRelativeTo(null);

        setVisible(true);

        SwingUtilities.invokeLater(() -> panel.repaint());
    }

    

    // Inside the same file, nested class
    public class GamePanel extends JPanel implements KeyListener {

        // ─────────────── Save integration helpers ───────────────
        private void saveProgress() {
            SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("PD4")
                    .battles(SaveSystem.getDefeatedBosses())
            );
        }

        // ─────────────── Original HUD / timing stuff ───────────────
        private String hudMessage = "";
        private int hudTimer = 0;

        private void showHUD(String msg) {
            hudMessage = msg;
            hudTimer = 90; // ~1.5 seconds at 60 FPS
        }

        private int promptCooldown = 0;

        private final int gridSize = 12;
        private final int colSize = 16;

        private final Image Vine;
        private final Image blue1;
        private final Image blue2;
        private final Image blue3;
        private final Image blue4;
        private final Image pro1;
        private final Image pro2;
        private final Image pro3;
        private final Image pro4;
        private final Image AGH;
        private final Image Dead;
        
        int hi = 0;

        // --- pixil animations (4 frames per direction) ---
        private Image[] walkUp    = new Image[4];
        private Image[] walkDown  = new Image[4];
        private Image[] walkLeft  = new Image[4];
        private Image[] walkRight = new Image[4];
        private int walkFrame = 0;
        private int lastDirection = 1; // 0=up, 1=down, 2=left, 3=right

        private int pixilX = 8;
        private int pixilY = 11;

        private final boolean[][] solidTiles = new boolean[gridSize][colSize];

        public GamePanel() {
            setFocusable(true);
            addKeyListener(this);

            Vine  = new ImageIcon("images/G1_vines.png").getImage();
AGH   = new ImageIcon("images/G1_AGH.png").getImage();
blue1 = new ImageIcon("images/G1_blue1.png").getImage();
blue2 = new ImageIcon("images/G1_blue2.png").getImage();
blue3 = new ImageIcon("images/G1_blue3.png").getImage();
blue4 = new ImageIcon("images/G1_blue4.png").getImage();
pro1  = new ImageIcon("images/G1_pro1.png").getImage();
pro2  = new ImageIcon("images/G1_pro2.png").getImage();
pro3  = new ImageIcon("images/G1_pro3.png").getImage();
pro4  = new ImageIcon("images/G1_pro4.png").getImage();
Dead  = new ImageIcon("images/G1_deads.png").getImage();

            int w = 660 / colSize;
            int h = 660 / gridSize;

            for (int i = 0; i < 4; i++) {
                walkUp[i]    = new ImageIcon("up_" + (i+1) + ".png").getImage()
                                         .getScaledInstance(w, h, Image.SCALE_SMOOTH);
                walkDown[i]  = new ImageIcon("down_" + (i+1) + ".png").getImage()
                                         .getScaledInstance(w, h, Image.SCALE_SMOOTH);
                walkLeft[i]  = new ImageIcon("left_" + (i+1) + ".png").getImage()
                                         .getScaledInstance(w, h, Image.SCALE_SMOOTH);
                walkRight[i] = new ImageIcon("right_" + (i+1) + ".png").getImage()
                                         .getScaledInstance(w, h, Image.SCALE_SMOOTH);
            }

            // -------- SOLID TILES (UNCHANGED LOGIC) --------
            solidTiles[1][2] = true;
            solidTiles[1][3] = true;
            solidTiles[1][4] = true;
            solidTiles[1][6] = true;
            solidTiles[1][7] = true;
            solidTiles[1][8] = true;
            solidTiles[1][9] = true;
            solidTiles[1][10] = true;

            solidTiles[2][2] = true;
            solidTiles[2][10] = true;

            solidTiles[3][2] = true;
            solidTiles[3][10] = true;
            solidTiles[3][11] = true;
            solidTiles[3][12] = true;
            solidTiles[3][13] = true;
            solidTiles[3][14] = true;
            solidTiles[3][15] = true;

            solidTiles[4][2] = true;
            solidTiles[4][15] = true;

            solidTiles[5][0] = true;
            solidTiles[5][1] = true;
            solidTiles[5][2] = true;
            solidTiles[5][15] = true;

            solidTiles[6][0] = true;
            solidTiles[6][2] = true;

            solidTiles[7][2] = true;
            solidTiles[7][3] = true;
            solidTiles[7][4] = true;
            solidTiles[7][5] = true;
            solidTiles[7][6] = true;
            solidTiles[7][7] = true;
            solidTiles[7][15] = true;

            solidTiles[8][0] = true;
            solidTiles[8][15] = true;

            solidTiles[9][0] = true;
            solidTiles[9][2] = true;
            solidTiles[9][3] = true;
            solidTiles[9][4] = true;
            solidTiles[9][15] = true;

            solidTiles[10][0] = true;
            solidTiles[10][1] = true;
            solidTiles[10][2] = true;
            solidTiles[10][4] = true;
            solidTiles[10][6] = true;
            solidTiles[10][7] = true;
            solidTiles[10][8] = true;
            solidTiles[10][9] = true;
            solidTiles[10][10] = true;
            solidTiles[10][11] = true;
            solidTiles[10][12] = true;
            solidTiles[10][13] = true;
            solidTiles[10][15] = true;

            solidTiles[11][4] = true;
            solidTiles[11][6] = true;
            solidTiles[11][13] = true;
            solidTiles[11][14] = true;
            solidTiles[11][15] = true;

            solidTiles[11][5] = false;
            solidTiles[7][0] = false;
            solidTiles[6][15] = false;
            solidTiles[1][5] = false;

            System.out.println("GAME PANEL EXISTS");
        }

        private Image load(String path) {
            try {
                return new ImageIcon(getClass().getResource(path)).getImage();
            } catch (Exception e) {
                System.out.println("Missing image: " + path);
                return new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
            }
        }

        private void showPrompt(String msg) {
            if (promptCooldown == 0) {
                hudMessage = msg;
                hudTimer = 60;
                promptCooldown = 30;
            }
        }
        private void switchMap() {
    stopMusic(); // 🔥 important
    setContentPane(new G1_Room2_PD6());
    revalidate();
    repaint();
}

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int cellWidth = getWidth() / colSize;
            int cellHeight = getHeight() / gridSize;

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < colSize; col++) {
                    Image img = Vine;

                    if (row == 5 && col == 10) img = blue1;
                    else if (row == 5 && col == 11) img = blue2;
                    else if (row == 6 && col == 10) img = blue3;
                    else if (row == 6 && col == 11) img = blue4;
                    
                    else if (row == 5 && col == 4) img = blue1;
                    else if (row == 5 && col == 5) img = blue2;
                    else if (row == 6 && col == 4) img = blue3;
                    else if (row == 6 && col == 5) img = blue4;
                    
                    else if (row == 2 && col == 7) img = blue1;
                    else if (row == 2 && col == 8) img = blue2;
                    else if (row == 3 && col == 7) img = blue3;
                    else if (row == 3 && col == 8) img = blue4;

                    else if (row == 5 && col == 7) img = pro1;
                    else if (row == 5 && col == 8) img = pro2;
                    else if (row == 6 && col == 7) img = pro3;
                    else if (row == 6 && col == 8) img = pro4;

                    else if (row == 1 && col == 5)
                        img = solidTiles[1][5] ? AGH : Dead;

                    if (row == 6 && col == 15)
                        img = solidTiles[6][15] ? AGH : Dead;

                    if (row == 7 && col == 0)
                        img = solidTiles[7][0] ? AGH : Dead;

                    if (row == 11 && col == 5)
                        img = solidTiles[11][5] ? AGH : Dead;

                    g.drawImage(img, col * cellWidth, row * cellHeight,
                            cellWidth, cellHeight, null);
                }
            }

            // draw pixil
            Image pixilImage;

            switch (lastDirection) {
                case 0: pixilImage = walkUp[walkFrame];   break;
                case 1: pixilImage = walkDown[walkFrame]; break;
                case 2: pixilImage = walkLeft[walkFrame]; break;
                case 3: pixilImage = walkRight[walkFrame]; break;
                default: pixilImage = null;
            }

            if (pixilImage != null) {
                g.drawImage(pixilImage,
                        pixilX * cellWidth,
                        pixilY * cellHeight,
                        cellWidth,
                        cellHeight, null);
            }

            if (hudTimer > 0) {
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRoundRect(10, 10, 300, 40, 10, 10);

                g.setColor(Color.WHITE);
                g.drawString(hudMessage, 20, 35);

                hudTimer--;
                if (promptCooldown > 0) promptCooldown--;
            }
            
            if(pixilY  == 11 && hi == 0){
            showPrompt("Find a path to face the boss");
            hi = 1;
            }

            

            if (!solidTiles[6][15] &&
                    ((pixilX == 15 && pixilY == 7) || (pixilX == 14 && pixilY == 6))) {
                showPrompt("Press W to break vines");
            }

            if (!solidTiles[1][5] &&
                    ((pixilX == 6 && pixilY == 1) || (pixilX == 4 && pixilY == 1))) {
                showPrompt("Press A to break vines");
            }

            if (!solidTiles[7][0] &&
                    ((pixilX == 0 && pixilY == 8) || (pixilX == 0 && pixilY == 6))) {
                showPrompt("Press Q to break vines");
            }

            if (!solidTiles[11][5] &&
                    ((pixilX == 4 && pixilY == 11) || (pixilX == 6 && pixilY == 11))) {
                showPrompt("Press D to break vines");
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            int oldX = pixilX;
            int oldY = pixilY;

            // ---------------- MOVEMENT ----------------
            if (key == KeyEvent.VK_UP)    pixilY--;
            if (key == KeyEvent.VK_DOWN)  pixilY++;
            if (key == KeyEvent.VK_LEFT)  pixilX--;
            if (key == KeyEvent.VK_RIGHT) pixilX++;

            if (pixilX < 0) pixilX = 0;
            if (pixilY < 0) pixilY = 0;
            if (pixilX >= colSize)  pixilX = colSize - 1;
            if (pixilY >= gridSize) pixilY = gridSize - 1;

            if (!solidTiles[pixilY][pixilX]) {
                pixilX = oldX;
                pixilY = oldY;
            }

            // ---------------- UPDATE ANIMATION FRAME ----------------
            if (pixilX != oldX || pixilY != oldY) {
                if (pixilY < oldY)      lastDirection = 0;
                else if (pixilY > oldY) lastDirection = 1;
                else if (pixilX < oldX) lastDirection = 2;
                else if (pixilX > oldX) lastDirection = 3;

                walkFrame = (walkFrame + 1) % 4;
            }

            // ---------------- BREAK VINES (ONE TIME ONLY) ----------------

            if (key == KeyEvent.VK_W) {
                if (!solidTiles[6][15] &&
                        ((pixilX == 15 && pixilY == 7) || (pixilX == 15 && pixilY == 5))) {

                    solidTiles[6][15] = true;
                    showHUD("Vines broken!");
                    saveProgress();
                }
            }

            if (key == KeyEvent.VK_A) {
                if (!solidTiles[1][5] &&
                        ((pixilX == 6 && pixilY == 1) || (pixilX == 4 && pixilY == 1))) {

                    solidTiles[1][5] = true;
                    showHUD("Vines broken!");
                    saveProgress();
                }
            }

            if (key == KeyEvent.VK_Q) {
                if (!solidTiles[7][0] &&
                        ((pixilX == 0 && pixilY == 8) || (pixilX == 0 && pixilY == 6))) {

                    solidTiles[7][0] = true;
                    showHUD("Vines broken!");
                    saveProgress();
                }
            }

            if (key == KeyEvent.VK_D) {
                if (!solidTiles[11][5] &&
                        ((pixilX == 4 && pixilY == 11) || (pixilX == 6 && pixilY == 11))) {

                    solidTiles[11][5] = true;
                    showHUD("Vines broken!");
                    saveProgress();
                }
            }
            
            

            // ---------------- MAIN MAP SWITCH (G1_Room1_PD4 → G1_Room2_PD6) ----------------
            if (pixilX == 7 && pixilY == 7) {
                SwingUtilities.invokeLater(() -> {
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    if (topFrame instanceof G1_Room1_PD4) {
                        ((G1_Room1_PD4) topFrame).switchMap();
                    }
                });
            }

            repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {}
        @Override
        public void keyTyped(KeyEvent e) {}
    }
}

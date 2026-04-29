package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;
import java.io.File;

public class G1_Room1_PD4 {

    static Clip sharedClip;
    private static SaveSystem.SaveData saved;
    JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new G1_Room1_PD4().setFrame());
    }

    public static void startMusicIfNeeded() {
        if (sharedClip != null && sharedClip.isRunning()) return;
        try {
            File file = new File("music/Ivy.wav");
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            sharedClip = AudioSystem.getClip();
            sharedClip.open(audio);
            sharedClip.loop(Clip.LOOP_CONTINUOUSLY);
            sharedClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        if (sharedClip != null) {
            sharedClip.stop();
            sharedClip.close();
            sharedClip = null;
        }
    }

    void switchMap() {
        frame.setContentPane(new G1_Room2_PD6());
        frame.revalidate();
        frame.repaint();
    }

    public void setFrame() {
        startMusicIfNeeded();

        saved = SaveSystem.loadGame("G1_Room1_PD4");
        SaveSystem.startTimer(saved.timeSeconds);
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G1_Room1_PD4")
                .battles(SaveSystem.getDefeatedBosses())
        );

        frame = new JFrame("Group One");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanel panel = new GamePanel();
        frame.setContentPane(panel);
        frame.setSize(660, 660);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        SwingUtilities.invokeLater(panel::repaint);
    }

    // ═══════════════════════════════════════════════════════════
    public class GamePanel extends JPanel implements KeyListener {

        private String hudMessage = "";
        private int hudTimer      = 0;
        private int promptCooldown = 0;

        private void showHUD(String msg) {
            hudMessage = msg;
            hudTimer   = 90;
        }

        private void showPrompt(String msg) {
            if (promptCooldown == 0) {
                hudMessage     = msg;
                hudTimer       = 60;
                promptCooldown = 30;
            }
        }

        public void saveProgress() {
            SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("G1_Room1_PD4")
                    .battles(SaveSystem.getDefeatedBosses())
            );
        }

        private final int gridSize = 16;
        private final int colSize  = 16;

        private final Image Vine, blue1, blue2, blue3, blue4;
        private final Image pro1,  pro2,  pro3,  pro4;
        private final Image AGH,  Dead;

        private final Image[] walkUp    = new Image[4];
        private final Image[] walkDown  = new Image[4];
        private final Image[] walkLeft  = new Image[4];
        private final Image[] walkRight = new Image[4];
        private int walkFrame     = 0;
        private int lastDirection = 1;

        private int pixilX = 8;
        private int pixilY = 13;

        private final boolean[][] solidTiles = new boolean[gridSize][colSize];

        private int hi = 0;

        // ─────────────────────────────────────────────────────────
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

            // FIX: Load the original image via ImageIcon (which blocks until fully loaded),
            //      then scale directly inside drawImage inside toBufferedImage.
            //      This eliminates the lazy ToolkitImage pipeline entirely.
            //      getScaledInstance() is NOT called here anymore.
            for (int i = 0; i < 4; i++) {
                walkUp[i]    = toBufferedImage(new ImageIcon("images/up_"    + (i+1) + ".png").getImage(), w, h);
                walkDown[i]  = toBufferedImage(new ImageIcon("images/down_"  + (i+1) + ".png").getImage(), w, h);
                walkLeft[i]  = toBufferedImage(new ImageIcon("images/left_"  + (i+1) + ".png").getImage(), w, h);
                walkRight[i] = toBufferedImage(new ImageIcon("images/right_" + (i+1) + ".png").getImage(), w, h);
            }

            initSolidTiles();
            System.out.println("GAME PANEL EXISTS");
        }

        // FIX: Takes the raw (already-loaded) Image from ImageIcon and draws it
        //      scaled directly into a fresh BufferedImage using RenderingHints
        //      for quality. No getScaledInstance() = no lazy decoding = no blank sprites.
        private BufferedImage toBufferedImage(Image img, int w, int h) {
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, w, h, null);
            g2.dispose();
            return bi;
        }

        private void initSolidTiles() {
            solidTiles[3][2]  = true;
            solidTiles[3][3]  = true;
            solidTiles[3][4]  = true;
            solidTiles[3][6]  = true;
            solidTiles[3][7]  = true;
            solidTiles[3][8]  = true;
            solidTiles[3][9]  = true;
            solidTiles[3][10] = true;

            solidTiles[4][2]  = true;
            solidTiles[4][10] = true;

            solidTiles[5][2]  = true;
            solidTiles[5][10] = true;
            solidTiles[5][11] = true;
            solidTiles[5][12] = true;
            solidTiles[5][13] = true;
            solidTiles[5][14] = true;
            solidTiles[5][15] = true;

            solidTiles[6][2]  = true;
            solidTiles[6][15] = true;

            solidTiles[7][0]  = true;
            solidTiles[7][1]  = true;
            solidTiles[7][2]  = true;
            solidTiles[7][15] = true;

            solidTiles[8][0]  = true;
            solidTiles[8][2]  = true;

            solidTiles[9][2]  = true;
            solidTiles[9][3]  = true;
            solidTiles[9][4]  = true;
            solidTiles[9][5]  = true;
            solidTiles[9][6]  = true;
            solidTiles[9][7]  = true;
            solidTiles[9][15] = true;

            solidTiles[10][0]  = true;
            solidTiles[10][15] = true;

            solidTiles[11][0]  = true;
            solidTiles[11][2]  = true;
            solidTiles[11][3]  = true;
            solidTiles[11][4]  = true;
            solidTiles[11][15] = true;

            solidTiles[12][0]  = true;
            solidTiles[12][1]  = true;
            solidTiles[12][2]  = true;
            solidTiles[12][4]  = true;
            solidTiles[12][6]  = true;
            solidTiles[12][7]  = true;
            solidTiles[12][8]  = true;
            solidTiles[12][9]  = true;
            solidTiles[12][10] = true;
            solidTiles[12][11] = true;
            solidTiles[12][12] = true;
            solidTiles[12][13] = true;
            solidTiles[12][15] = true;

            solidTiles[13][4]  = true;
            solidTiles[13][6]  = true;
            solidTiles[13][13] = true;
            solidTiles[13][14] = true;
            solidTiles[13][15] = true;

            solidTiles[13][5] = false;
            solidTiles[9][0]  = false;
            solidTiles[8][15] = false;
            solidTiles[3][5]  = false;

            solidTiles[13][8] = true;
        }

        // ─────────────────────────────────────────────────────────
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int cellWidth  = getWidth()  / colSize;
            int cellHeight = getHeight() / gridSize;

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < colSize; col++) {
                    Image img = Vine;

                    if      (row == 7 && col == 10) img = blue1;
                    else if (row == 7 && col == 11) img = blue2;
                    else if (row == 8 && col == 10) img = blue3;
                    else if (row == 8 && col == 11) img = blue4;
                    else if (row == 7 && col ==  4) img = blue1;
                    else if (row == 7 && col ==  5) img = blue2;
                    else if (row == 8 && col ==  4) img = blue3;
                    else if (row == 8 && col ==  5) img = blue4;
                    else if (row == 4 && col ==  7) img = blue1;
                    else if (row == 4 && col ==  8) img = blue2;
                    else if (row == 5 && col ==  7) img = blue3;
                    else if (row == 5 && col ==  8) img = blue4;
                    else if (row == 7 && col ==  7) img = pro1;
                    else if (row == 7 && col ==  8) img = pro2;
                    else if (row == 8 && col ==  7) img = pro3;
                    else if (row == 8 && col ==  8) img = pro4;

                    if (row == 3 && col == 5)
                        img = solidTiles[3][5] ? Dead : AGH;

                    if (row == 8 && col == 15)
                        img = solidTiles[8][15] ? Dead : AGH;

                    if (row == 9 && col == 0)
                        img = solidTiles[9][0] ? Dead : AGH;

                    if (row == 13 && col == 5)
                        img = solidTiles[13][5] ? Dead : AGH;

                    g.drawImage(img, col * cellWidth, row * cellHeight,
                            cellWidth, cellHeight, null);
                }
            }

            Image pixilImage = switch (lastDirection) {
                case 0  -> walkUp[walkFrame];
                case 2  -> walkLeft[walkFrame];
                case 3  -> walkRight[walkFrame];
                default -> walkDown[walkFrame];
            };

            if (pixilImage != null)
                g.drawImage(pixilImage,
                        pixilX * cellWidth, pixilY * cellHeight,
                        cellWidth, cellHeight, null);

            if (hudTimer > 0) {
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRoundRect(10, 10, 340, 40, 10, 10);
                g.setColor(Color.WHITE);
                g.setFont(g.getFont().deriveFont(14f));
                g.drawString(hudMessage, 20, 35);
                hudTimer--;
                if (promptCooldown > 0) promptCooldown--;
            }

            if (pixilY == 13 && hi == 0) {
                showPrompt("Find a path to face the boss");
                hi = 1;
            }

            if (!solidTiles[8][15] &&
                    (pixilX == 15 && (pixilY == 9 || pixilY == 7)))
                showPrompt("Press SPACE to break vines");

            if (!solidTiles[3][5] &&
                    pixilY == 3 && (pixilX == 6 || pixilX == 4))
                showPrompt("Press SPACE to break vines");

            if (!solidTiles[9][0] &&
                    pixilX == 0 && (pixilY == 10 || pixilY == 8))
                showPrompt("Press SPACE to break vines");

            if (!solidTiles[13][5] &&
                    pixilY == 13 && (pixilX == 4 || pixilX == 6))
                showPrompt("Press SPACE to break vines");
        }

        // ─────────────────────────────────────────────────────────
        @Override
        public void keyPressed(KeyEvent e) {
            int key  = e.getKeyCode();
            int oldX = pixilX;
            int oldY = pixilY;

            int nextX = pixilX;
            int nextY = pixilY;

            if (key == KeyEvent.VK_W) nextY--;
            if (key == KeyEvent.VK_S) nextY++;
            if (key == KeyEvent.VK_A) nextX--;
            if (key == KeyEvent.VK_D) nextX++;

            nextX = Math.max(0, Math.min(colSize  - 1, nextX));
            nextY = Math.max(0, Math.min(gridSize - 1, nextY));

            if (solidTiles[nextY][nextX]) {
                pixilX = nextX;
                pixilY = nextY;
            }

            if (pixilX != oldX || pixilY != oldY) {
                if      (pixilY < oldY) lastDirection = 0;
                else if (pixilY > oldY) lastDirection = 1;
                else if (pixilX < oldX) lastDirection = 2;
                else if (pixilX > oldX) lastDirection = 3;
                walkFrame = (walkFrame + 1) % 4;
            }

            if (key == KeyEvent.VK_SPACE
                    && !solidTiles[8][15]
                    && pixilX == 15 && (pixilY == 9 || pixilY == 7)) {
                solidTiles[8][15] = true;
                showHUD("Vines broken!");
                saveProgress();
            }

            if (key == KeyEvent.VK_SPACE
                    && !solidTiles[3][5]
                    && pixilY == 3 && (pixilX == 6 || pixilX == 4)) {
                solidTiles[3][5] = true;
                showHUD("Vines broken!");
                saveProgress();
            }

            if (key == KeyEvent.VK_SPACE
                    && !solidTiles[9][0]
                    && pixilX == 0 && (pixilY == 10 || pixilY == 8)) {
                solidTiles[9][0] = true;
                showHUD("Vines broken!");
                saveProgress();
            }

            if (key == KeyEvent.VK_SPACE
                    && !solidTiles[13][5]
                    && pixilY == 13 && (pixilX == 4 || pixilX == 6)) {
                solidTiles[13][5] = true;
                showHUD("Vines broken!");
                saveProgress();
            }

            if (pixilX == 7 && pixilY == 9) {
                SwingUtilities.invokeLater(G1_Room1_PD4.this::switchMap);
            }

            repaint();
        }

        @Override public void keyReleased(KeyEvent e) {}
        @Override public void keyTyped(KeyEvent e) {}
    }
}
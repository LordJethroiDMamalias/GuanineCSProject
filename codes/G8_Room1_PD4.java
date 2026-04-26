package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;
import java.io.File;

public class G8_Room1_PD4 implements KeyListener {

    JFrame frame;
    JLayeredPane layeredPane;
    G8_Dialog dialog = new G8_Dialog(); 

    JLabel[] tiles;
    JLabel[] characterLabels; 

    int mapWidth = 11;
    int mapHeight = 11;
    int frameWidth = 660;
    int frameHeight = 660;
    int[] mapLayout;
    int characterPosition;

    ImageIcon grass1, Fence, TRFence, TLFence, SFence, BRFence, BLFence;
    ImageIcon pUp1, pDown1, pLeft1, pRight1;
    ImageIcon grassRed, bombIcon;

    boolean[] isRedTile;
    boolean[] hasBomb;
    ScheduledExecutorService bombScheduler;

    // --- MUSIC SYSTEM (Shared with PD6) ---
    public static Clip bgmClip;
    public static String currentSong = "";

    public static void playMusic(String location) {
        if (currentSong.equals(location)) return; // Keep playing if it's the same song
        try {
            stopMusic();
            File musicPath = new File(location);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioInput);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
                currentSong = location;
            }
        } catch(Exception e) { System.out.println("Music Error: " + e); }
    }

    public static void stopMusic() {
        if (bgmClip != null && bgmClip.isOpen()) {
            bgmClip.stop();
            bgmClip.close();
        }
        currentSong = "";
    }
    // ---------------------------------------

    public G8_Room1_PD4() {
        frame = new JFrame("Bomber - Room 1");
        int cellW = frameWidth / mapWidth;
        int cellH = frameHeight / mapHeight;

        isRedTile = new boolean[mapWidth * mapHeight];
        hasBomb = new boolean[mapWidth * mapHeight];
        characterLabels = new JLabel[mapWidth * mapHeight];
        tiles = new JLabel[mapWidth * mapHeight];

        grass1 = loadAndScale("images/G8_grass1.png", cellW, cellH);
        grassRed = loadAndScale("images/G8_grass1red.png", cellW, cellH);
        bombIcon = loadAndScale("images/G8_bomb.png", cellW, cellH);
        Fence = loadAndScale("images/G8_TFence.png", cellW, cellH);
        TRFence = loadAndScale("images/G8_TRFence.png", cellW, cellH);
        TLFence = loadAndScale("images/G8_TLFence.png", cellW, cellH);
        SFence = loadAndScale("images/G8_SFence.png", cellW, cellH);
        BRFence = loadAndScale("images/G8_BRFence.png", cellW, cellH);
        BLFence = loadAndScale("images/G8_BLFence.png", cellW, cellH);
        
        pDown1 = loadAndScale("images/G8_down1.png", cellW, cellH);
        pUp1 = loadAndScale("images/G8_up1.png", cellW, cellH);
        pLeft1 = loadAndScale("images/G8_left1.png", cellW, cellH);
        pRight1 = loadAndScale("images/G8_right1.png", cellW, cellH);

        setupMap();
    }

    private ImageIcon loadAndScale(String path, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getIconWidth() == -1) {
                if (path.contains("G8_")) icon = new ImageIcon(path.replace("G8_", ""));
                else icon = new ImageIcon(path.replace("images/", "images/G8_"));
            }
            if (icon.getIconWidth() == -1) return null;
            return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    private void setupMap() {
        mapLayout = new int[]{
            4,1,1,1,1,1,1,1,1,1,2,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            3,0,0,0,0,0,0,0,0,0,3,
            5,1,1,1,1,1,1,1,1,1,6
        };

        for (int i = 0; i < mapLayout.length; i++) {
            characterLabels[i] = new JLabel();
            characterLabels[i].setHorizontalAlignment(JLabel.CENTER);
            switch (mapLayout[i]) {
                case 0 -> tiles[i] = new JLabel(grass1);
                case 1 -> tiles[i] = new JLabel(Fence);
                case 2 -> tiles[i] = new JLabel(TRFence);
                case 3 -> tiles[i] = new JLabel(SFence);
                case 4 -> tiles[i] = new JLabel(TLFence);
                case 5 -> tiles[i] = new JLabel(BLFence);
                case 6 -> tiles[i] = new JLabel(BRFence);
                default -> tiles[i] = new JLabel();
            }
        }
        characterPosition = 60; 
        characterLabels[characterPosition].setIcon(pDown1);
    }

    public void setFrame() {
        // Start Exploration Music
        playMusic("music/GIGGLEBOT3000.wav");

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

        JPanel tilePanel = new JPanel(new GridLayout(mapHeight, mapWidth));
        tilePanel.setBounds(0, 0, frameWidth, frameHeight);
        for (JLabel t : tiles) tilePanel.add(t);
    
        JPanel charPanel = new JPanel(new GridLayout(mapHeight, mapWidth));
        charPanel.setBounds(0, 0, frameWidth, frameHeight);
        charPanel.setOpaque(false);
        for (JLabel c : characterLabels) charPanel.add(c);

        layeredPane.add(tilePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(charPanel, JLayeredPane.PALETTE_LAYER);

        frame.add(layeredPane);
        frame.setFocusable(true);
        frame.addKeyListener(this);
        dialog.addKey(frame); 
        
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        String[] intro = {"Welcome!", "Survive the bombs for 30 seconds!", "Press SPACE to start!"};
        dialog.show(layeredPane, intro, null, null, mapWidth, mapHeight, () -> {
            startBombLogic();
            startWinTimer();
        });

        SwingUtilities.invokeLater(() -> frame.requestFocusInWindow());
    }

    private void moveCharacter(int target, ImageIcon icon) {
        if (dialog.isVisible()) return; 
        if (target < 0 || target >= mapWidth * mapHeight || mapLayout[target] != 0) return; 

        characterLabels[characterPosition].setIcon(null);
        characterLabels[target].setIcon(icon);
        characterPosition = target;

        if (hasBomb[characterPosition]) {
            if (bombScheduler != null) bombScheduler.shutdownNow();
            dialog.show(layeredPane, new String[]{"BOOM!", "Game Over!"}, null, new Runnable[]{() -> System.exit(0)}, mapWidth, mapHeight, null);
        }
    }

    private void startBombLogic() {
        bombScheduler = Executors.newScheduledThreadPool(4);
        bombScheduler.scheduleAtFixedRate(() -> {
            if (dialog.isVisible()) return;
            for(int wave = 0; wave < 3; wave++) {
                java.util.List<Integer> valid = new java.util.ArrayList<>();
                for (int i = 0; i < mapLayout.length; i++) 
                    if (mapLayout[i] == 0 && !isRedTile[i] && !hasBomb[i]) valid.add(i);
                
                if (valid.isEmpty()) continue;
                int idx = valid.get((int)(Math.random() * valid.size()));
                isRedTile[idx] = true;
                SwingUtilities.invokeLater(() -> tiles[idx].setIcon(grassRed));

                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(1500);
                        SwingUtilities.invokeLater(() -> {
                            isRedTile[idx] = false;
                            hasBomb[idx] = true;
                            tiles[idx].setIcon(bombIcon);
                            if (characterPosition == idx) moveCharacter(idx, (ImageIcon)characterLabels[idx].getIcon());
                        });
                        Thread.sleep(1000);
                        SwingUtilities.invokeLater(() -> {
                            hasBomb[idx] = false;
                            tiles[idx].setIcon(grass1);
                        });
                    } catch (Exception e) {}
                });
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void startWinTimer() {
        javax.swing.Timer timer = new javax.swing.Timer(30000, e -> {
            if (bombScheduler != null) bombScheduler.shutdownNow();

            dialog.show(layeredPane, 
                new String[]{"VICTORY!",
                    "Looks like you survived the bombs, Heh, Lets see you battle me.",
                    "Teleporting to my lab*"
                }, 
                null, null, mapWidth, mapHeight, 
                () -> { 
                    frame.dispose(); 
                    G8_Room2_PD6 nextRoom = new G8_Room2_PD6();
                    nextRoom.setFrame();
                }
            );
        });
        timer.setRepeats(false);
        timer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int row = characterPosition / mapWidth;
        int col = characterPosition % mapWidth;
        if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && col < mapWidth - 1) moveCharacter(characterPosition + 1, pRight1);
        else if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && col > 0) moveCharacter(characterPosition - 1, pLeft1);
        else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && row < mapHeight - 1) moveCharacter(characterPosition + mapWidth, pDown1);
        else if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && row > 0) moveCharacter(characterPosition - mapWidth, pUp1);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new G8_Room1_PD4().setFrame());
    }
}

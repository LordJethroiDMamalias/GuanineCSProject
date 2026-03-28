/*
Created by: 
John Felippe D. Samonte
Angelika Margaret Dumogho
James Ryan Rios
10-Guanine

Using the assistance of Gemini AI

*/

package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

public class G8_Room1_PD4 implements KeyListener {
    
    JFrame frame;
    JLayeredPane layeredPane; // Required for Dialog compatibility
    Dialog dialog = new Dialog(); 
    
    JLabel[] tiles;
    JLabel[] characterLabels; // Renamed for clarity
    
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
    
    public G8_Room1_PD4() {
        try {
            frame = new JFrame("Bomber - Room 1");
            int tileSizeW = frameWidth / mapWidth;
            int tileSizeH = frameHeight / mapHeight;

            isRedTile = new boolean[mapWidth * mapHeight];
            hasBomb = new boolean[mapWidth * mapHeight];
            
            // Safe Image Loading
            grass1 = loadIcon("Images/grass1.png", tileSizeW, tileSizeH);
            grassRed = loadIcon("Images/grass1red.png", tileSizeW, tileSizeH);
            bombIcon = loadIcon("Images/bomb.png", tileSizeW, tileSizeH);
            Fence = loadIcon("Images/TFence.png", tileSizeW, tileSizeH);
            TRFence = loadIcon("Images/TRFence.png", tileSizeW, tileSizeH);
            TLFence = loadIcon("Images/TLFence.png", tileSizeW, tileSizeH);
            SFence = loadIcon("Images/SFence.png", tileSizeW, tileSizeH);
            BRFence = loadIcon("Images/BRFence.png", tileSizeW, tileSizeH);
            BLFence = loadIcon("Images/BLFence.png", tileSizeW, tileSizeH);
            pDown1 = loadIcon("Images/down1.png", tileSizeW, tileSizeH);
            pUp1 = loadIcon("Images/up1.png", tileSizeW, tileSizeH);
            pLeft1 = loadIcon("Images/left1.png", tileSizeW, tileSizeH);
            pRight1 = loadIcon("Images/right1.png", tileSizeW, tileSizeH);

            setupMap();
            startBombLogic();

        } catch (Exception e) {
            System.err.println("Critical failure in Constructor: " + e.getMessage());
        }
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("Missing asset: " + path);
            return null; 
        }
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

        tiles = new JLabel[mapWidth * mapHeight];
        characterLabels = new JLabel[mapWidth * mapHeight];
        characterPosition = 48; // Center approx

        for (int i = 0; i < tiles.length; i++) {
            characterLabels[i] = new JLabel();
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
        characterLabels[characterPosition].setIcon(pDown1);
    }

    public void setFrame() {
        try {
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
            dialog.addKey(frame); // Dialog integration
            frame.addKeyListener(this);

            frame.setSize(frameWidth, frameHeight);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Intro Dialog
            String[] intro = {"Welcome to the Bomber Room!", "Watch the tiles. If they turn red, a bomb is coming!", "Move with Arrow Keys."};
            dialog.show(layeredPane, intro, null, null, mapWidth, mapHeight);

        } catch (Exception e) {
            System.err.println("UI SetFrame Error: " + e.getMessage());
        }
    }

    private void moveCharacter(int target, ImageIcon icon) {
        try {
            if (dialog.isVisible()) return; // Block move during dialog
            
            if (target < 0 || target >= mapWidth * mapHeight) return;
            if (mapLayout[target] != 0) return;

            characterLabels[characterPosition].setIcon(null);
            characterLabels[target].setIcon(icon);
            characterPosition = target;

            if (hasBomb[characterPosition]) {
                String[] deathText = {"BOOM!", "You stepped on a bomb...", "Game Over."};
                dialog.show(layeredPane, deathText, null, new Runnable[]{() -> System.exit(0)}, mapWidth, mapHeight);
            }
        } catch (Exception e) {
            System.err.println("Movement error: " + e.getMessage());
        }
    }

    private void startBombLogic() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (dialog.isVisible()) return;

                int batchSize = 3; // Reduced batch size for better gameplay
                int[] selectedIndices = new int[batchSize];
                int count = 0;

                while (count < batchSize) {
                    int randIndex = (int) (Math.random() * mapLayout.length);
                    if (mapLayout[randIndex] == 0 && !isRedTile[randIndex]) {
                        isRedTile[randIndex] = true;
                        selectedIndices[count] = randIndex;
                        count++;
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    for (int idx : selectedIndices) tiles[idx].setIcon(grassRed);
                });

                // Detonate after 2 seconds
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(2000);
                        SwingUtilities.invokeLater(() -> {
                            for (int idx : selectedIndices) {
                                hasBomb[idx] = true;
                                tiles[idx].setIcon(bombIcon);
                                // Check if player is on the bomb at the moment of explosion
                                if (characterPosition == idx) {
                                    moveCharacter(idx, pDown1); 
                                }
                            }
                        });
                        
                        Thread.sleep(1000);
                        SwingUtilities.invokeLater(() -> {
                            for (int idx : selectedIndices) {
                                isRedTile[idx] = false;
                                hasBomb[idx] = false;
                                tiles[idx].setIcon(grass1);
                            }
                        });
                    } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                });
            } catch (Exception e) {
                System.err.println("Bomb Logic Error: " + e.getMessage());
            }
        }, 3, 4, TimeUnit.SECONDS);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (dialog.isVisible()) return;

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_RIGHT && (characterPosition % mapWidth) < mapWidth - 1) 
            moveCharacter(characterPosition + 1, pRight1);
        else if (key == KeyEvent.VK_LEFT && (characterPosition % mapWidth) > 0) 
            moveCharacter(characterPosition - 1, pLeft1);
        else if (key == KeyEvent.VK_DOWN) 
            moveCharacter(characterPosition + mapWidth, pDown1);
        else if (key == KeyEvent.VK_UP) 
            moveCharacter(characterPosition - mapWidth, pUp1);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new G8_Room1_PD4().setFrame());
    }
}
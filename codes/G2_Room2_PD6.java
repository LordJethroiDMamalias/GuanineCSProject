package codes;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.FileNotFoundException;

// Made by: Francis Gabriel Dangoy, Ashey Bhel Galleto, Jared Linnus Cantor
// 10-Guanine

public class G2_Room2_PD6 implements KeyListener {

    JFrame       frame;
    JLayeredPane layers;
    ImageIcon    backgroundImage;
    ImageIcon    MakeupNPC;

    ImageIcon[] pUp    = new ImageIcon[4];
    ImageIcon[] pDown  = new ImageIcon[4];
    ImageIcon[] pLeft  = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];

    JLabel   backgroundLabel;
    JLabel[] characterLabels;

    JLabel[] tiles, character;
    int[]    mapLayout, characterPlace;
    int      mapWidth = 16, mapHeight = 16;
    int      frameWidth = 660, frameHeight = 660;
    int      characterPosition, characterMode;
    int      walkFrame = 0;
    int      direction = 1;

    int    itemsCollected = 0;
    Dialog dialog         = new Dialog();
    Battle battle         = new Battle();

    boolean foundLipstick   = false;
    boolean foundBrush      = false;
    boolean foundPalette    = false;
    boolean battleTriggered = false;   // true once the boss fight has started

    // =========================================================================
    // Constructor
    // =========================================================================
    public G2_Room2_PD6() {
        try {
            frame = new JFrame("Living Room");
            int tw = frameWidth  / mapWidth;
            int th = frameHeight / mapHeight;

            backgroundImage = new ImageIcon("images/G2_LivingRoomMap.png");
            if (backgroundImage.getImageLoadStatus() == MediaTracker.ERRORED)
                throw new FileNotFoundException(
                        "Background image not found at images/G2_LivingRoomMap.png");
            backgroundImage = new ImageIcon(backgroundImage.getImage()
                    .getScaledInstance((frameWidth / mapWidth) * 16,
                                       (frameHeight / mapHeight) * 16,
                                       Image.SCALE_DEFAULT));

            MakeupNPC = new ImageIcon("images/G2_MakeupNPC.png");
            if (MakeupNPC.getImageLoadStatus() == MediaTracker.ERRORED)
                throw new FileNotFoundException(
                        "NPC image not found at images/G2_MakeupNPC.png");
            MakeupNPC = new ImageIcon(MakeupNPC.getImage()
                    .getScaledInstance(frameWidth / mapWidth,
                                       frameHeight / mapHeight,
                                       Image.SCALE_DEFAULT));

            for (int i = 0; i < 4; i++) {
                pUp[i]    = scale("images/up_"    + (i + 1) + ".png", tw, th);
                pDown[i]  = scale("images/down_"  + (i + 1) + ".png", tw, th);
                pLeft[i]  = scale("images/left_"  + (i + 1) + ".png", tw, th);
                pRight[i] = scale("images/right_" + (i + 1) + ".png", tw, th);
            }

            backgroundLabel = new JLabel(backgroundImage);

            characterPlace = new int[]{
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            };

            characterLabels = new JLabel[mapWidth * mapHeight];
            character       = new JLabel[mapWidth * mapHeight];
            for (int i = 0; i < characterLabels.length; i++) {
                characterLabels[i] = new JLabel();
                characterLabels[i].setHorizontalAlignment(JLabel.CENTER);
                if (characterPlace[i] == 1)
                    characterLabels[i].setIcon(MakeupNPC);
            }

            characterPosition = 94;
            characterLabels[characterPosition].setIcon(pDown[0]);

            mapLayout = new int[]{
                3,3,3,3,3,3,3,6,6,6,3,3,3,3,3,3,
                3,3,3,3,3,0,3,6,6,6,3,3,3,3,3,3,
                3,3,3,3,3,0,3,6,6,6,3,3,3,3,3,3,
                3,3,3,3,3,0,3,6,6,6,7,7,7,7,0,0,
                3,3,3,3,3,0,3,0,0,7,7,7,7,7,0,0,
                3,3,3,3,3,0,3,0,0,7,7,7,7,7,0,0,
                0,0,0,0,3,0,3,4,0,0,5,5,5,0,0,0,
                0,0,0,0,0,0,3,4,0,0,5,5,5,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                2,2,2,2,2,2,2,0,0,0,1,1,1,0,0,0,
                2,2,2,2,2,2,2,0,0,0,1,1,1,0,0,0,
                2,2,2,2,2,2,2,0,0,0,1,1,1,0,0,0,
            };

            tiles = new JLabel[mapHeight * mapWidth];
            for (int i = 0; i < tiles.length; i++) {
                if (mapLayout[i] == 0)
                    tiles[i] = new JLabel();
            }

        } catch (FileNotFoundException e) {
            System.err.println("Critical Error: Missing Asset - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during initialization: " + e.getMessage());
        } finally {
            System.out.println("Game initialization sequence completed.");
        }

        // Load save data after all fields are ready
        loadSaveData();
    }

    // =========================================================================
    // Save integration — load
    // =========================================================================
    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G2_Room2_PD6");

        SaveSystem.startTimer(save.timeSeconds);

        foundLipstick = save.hasFlag("item_lipstick");
        foundBrush    = save.hasFlag("item_brush");
        foundPalette  = save.hasFlag("item_palette");

        // Recalculate collected count from flags (single source of truth)
        itemsCollected = 0;
        if (foundLipstick) itemsCollected++;
        if (foundBrush)    itemsCollected++;
        if (foundPalette)  itemsCollected++;

        // If Rainity was permanently defeated, skip the battle trigger permanently
        if (save.isDefeated("Rainity")) battleTriggered = true;

        System.out.println("[Room2] Loaded — Items:" + itemsCollected
                + "/3  BattleDone:" + battleTriggered
                + "  Time:" + save.formattedTime());
    }

    // =========================================================================
    // Save integration — save
    // =========================================================================
    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G2_Room2_PD6")
                .flag(foundLipstick ? "item_lipstick" : null)
                .flag(foundBrush    ? "item_brush"    : null)
                .flag(foundPalette  ? "item_palette"  : null)
                .battles(SaveSystem.getDefeatedBosses())   // always up-to-date list
        );
    }

    // =========================================================================
    // scale() helper
    // =========================================================================
    private ImageIcon scale(String path, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == MediaTracker.ERRORED)
                return new ImageIcon(new java.awt.image.BufferedImage(
                        w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB));
            return new ImageIcon(icon.getImage()
                    .getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================================
    // setFrame()
    // =========================================================================
    public void setFrame() {
        try {
            layers = new JLayeredPane();
            layers.setLayout(new GraphPaperLayout(new Dimension(mapWidth, mapHeight)));
            frame.setContentPane(layers);

            int x = 0, y = 0;
            for (int i = 0; i < characterLabels.length; i++) {
                layers.add(characterLabels[i], new Rectangle(x, y, 1, 1), 0);
                x++;
                if (x % mapWidth == 0) { x = 0; y++; }
            }

            layers.add(backgroundLabel, new Rectangle(0, 0, mapWidth, mapHeight));

            frame.setSize(frameWidth, frameHeight);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.addKeyListener(this);
            dialog.addKey(frame);

        } catch (NullPointerException e) {
            System.err.println("Error: Components not fully initialized before setting frame.");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            if (Battle.paused)      return;
            if (dialog.isVisible()) return;

            int move = 0;
            int key  = e.getKeyCode();

            if      (key == KeyEvent.VK_UP    || key == KeyEvent.VK_W) { direction = 0; move = -mapWidth; }
            else if (key == KeyEvent.VK_DOWN  || key == KeyEvent.VK_S) { direction = 1; move =  mapWidth; }
            else if (key == KeyEvent.VK_LEFT  || key == KeyEvent.VK_A) { direction = 2; move = -1; }
            else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { direction = 3; move =  1; }

            if (key == KeyEvent.VK_SPACE) {
                int interactPos = switch (direction) {
                    case 0 -> characterPosition - mapWidth;
                    case 1 -> characterPosition + mapWidth;
                    case 2 -> characterPosition - 1;
                    case 3 -> characterPosition + 1;
                    default -> -1;
                };

                if (interactPos < 0 || interactPos >= mapLayout.length)
                    throw new ArrayIndexOutOfBoundsException(
                            "Interaction attempted outside of map bounds.");

                // ── NPC interaction ────────────────────────────────────────────
                if (characterPlace[interactPos] == 1) {

                    if (itemsCollected < 3) {
                        dialog.show(layers,
                            new String[]{
                                "NPC: Oh no! I've lost my makeup kit in this messy room!",
                                "Can you help me find my Lipstick, Brush, and Palette?"
                            },
                            new String[]{"Sure!", "Maybe later"},
                            new Runnable[]{
                                () -> dialog.show(layers,
                                        new String[]{
                                            "NPC: Thank you!",
                                            "I think they are hidden in the furniture around here."
                                        },
                                        null, null, mapWidth, mapHeight),
                                null
                            },
                            mapWidth, mapHeight);

                    } else if (!battleTriggered) {
                        battleTriggered = true;

                        dialog.show(layers,
                            new String[]{
                                "NPC: You found them all! Thank you so much!",
                                "Wait... something feels wrong.",
                                "NPC: That's not my makeup kit at all.",
                                "It was a TRAP! Prepare yourself!"
                            },
                            null,
                            null,
                            mapWidth, mapHeight,
                            () -> {
                                // Fires the moment dialogue closes
                                battle.start(frame,
                                        "images/G2_LivingRoomMap.png",
                                        "Rainity");
                                waitForBattleEnd(() -> {
                                    if (!SaveSystem.isDefeated("Rainity")) {
                                        SaveSystem.markDefeated("Rainity");
                                    }
                                    saveProgress();
                                });
                            });

                    } else {
                        String npcLine = SaveSystem.isDefeated("Rainity")
                                ? "(The NPC smiles warmly, relieved.)"
                                : "(The NPC trembles. Something is still very wrong here.)";
                        dialog.show(layers,
                            new String[]{ npcLine },
                            null, null, mapWidth, mapHeight);
                    }
                } else {
                    int tileType = mapLayout[interactPos];

                    if (tileType == 6) {
                        if (!foundLipstick) {
                            foundLipstick = true;
                            itemsCollected++;
                            saveProgress();
                            dialog.show(layers,
                                new String[]{
                                    "[You searched the Bookshelf...]",
                                    "You found the Lipstick!"
                                },
                                null, null, mapWidth, mapHeight);
                        } else {
                            dialog.show(layers,
                                new String[]{"[Just some dusty books here.]"},
                                null, null, mapWidth, mapHeight);
                        }

                    } else if (tileType == 4) {
                        if (!foundBrush) {
                            foundBrush = true;
                            itemsCollected++;
                            saveProgress();
                            dialog.show(layers,
                                new String[]{
                                    "[You opened the Drawer...]",
                                    "You found the Brush!"
                                },
                                null, null, mapWidth, mapHeight);
                        } else {
                            dialog.show(layers,
                                new String[]{"[The drawer is empty.]"},
                                null, null, mapWidth, mapHeight);
                        }

                    } else if (tileType == 1) {
                        if (!foundPalette) {
                            foundPalette = true;
                            itemsCollected++;
                            saveProgress();
                            dialog.show(layers,
                                new String[]{
                                    "[You looked behind the TV...]",
                                    "You found the Palette!"
                                },
                                null, null, mapWidth, mapHeight);
                        } else {
                            dialog.show(layers,
                                new String[]{"[The TV is showing a makeup tutorial.]"},
                                null, null, mapWidth, mapHeight);
                        }

                    } else if (tileType != 0) {
                        dialog.show(layers,
                            new String[]{"[You searched, but found nothing.]"},
                            null, null, mapWidth, mapHeight);
                    }
                }
            }

            // ── Movement ───────────────────────────────────────────────────────
            if (move != 0) {
                int nextPos = characterPosition + move;
                if (nextPos >= 0 && nextPos < mapLayout.length) {
                    boolean isWrapping = (Math.abs(move) == 1
                            && (nextPos / mapWidth != characterPosition / mapWidth));
                    if (!isWrapping && mapLayout[nextPos] == 0
                            && characterPlace[nextPos] == 0) {
                        characterLabels[characterPosition].setIcon(null);
                        characterPosition = nextPos;
                        walkFrame = (walkFrame + 1) % 4;
                    } else {
                        walkFrame = 0;
                    }
                }

                ImageIcon current = switch (direction) {
                    case 0 -> pUp[walkFrame];
                    case 1 -> pDown[walkFrame];
                    case 2 -> pLeft[walkFrame];
                    case 3 -> pRight[walkFrame];
                    default -> pDown[0];
                };

                if (current == null)
                    throw new NullPointerException("Animation frame icon is null.");
                characterLabels[characterPosition].setIcon(current);
            }

        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("Movement/Interaction Error: " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.err.println("Display Error: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("General Input Error: " + ex.getMessage());
        }
    }

    @Override public void keyTyped(KeyEvent e)    {}
    @Override public void keyReleased(KeyEvent e) {}

    private void waitForBattleEnd(Runnable onEnd) {
        javax.swing.Timer poller = new javax.swing.Timer(200, null);
        poller.addActionListener(ev -> {
            if (!Battle.paused) {          // battle overlay is gone
                poller.stop();
                onEnd.run();
            }
        });
        poller.start();
    }

    public static void main(String[] args) {
        try {
            G2_Room2_PD6 x = new G2_Room2_PD6();
            x.setFrame();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "The application failed to start: " + e.getMessage());
        }
    }
}

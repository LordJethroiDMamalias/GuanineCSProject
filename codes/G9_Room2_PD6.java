package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// grp: ancla, badayos, sepe

public class G9_Room2_PD6 implements KeyListener {
    // --- UI Components ---
    JFrame frame;
    JLayeredPane layeredPane;
    JLabel background;
    JLabel cutscene;
    JPanel blackOverlay;
    JPanel charPanel; 
    JLabel[] gridSlots;

    // --- Assets ---
    ImageIcon pStand, pFrontW, pFrontW1, pFrontW2, pBack, pBack1, pBack2, pLeft, pLeft1, pLeft2, pRight, pRight1, pRight2;
    ImageIcon grandma, grandpa;

    // --- Settings & State ---
    int mapW = 11;
    int mapH = 11;
    int frW = 660, frH = 660;
    int cutW = 540, cutH = 540;
    int characterPosition = 66; 
    int grandmaPos = 92;
    int grandpaPos = 78;
    int binPos1 = 102; 
    int binPos2 = 103; 
    int animFrame = 0;
    String direction = "down";
    
    // --- Encapsulated Trash System ---
    int[] inv = new int[10]; 
    int itemsHeld = 0;      
    int[] mapTrash = {13, 37, 48, 75, 79, 85, 108, 104};
    GameEvent trashEvent = new G9_trashThrown();
    
    public int totalTrash = mapTrash.length;
    private int trashCleared = 0;
    public boolean rewardGiven = false;
    public boolean[] cleared; 
    boolean finalDialog = false;
    boolean objComplete = false;
    boolean miniDefeat = false;
    boolean talkedGrandma = false;
    boolean isFirstTimeM = true;
    boolean isFirstTimeI = true;
    int[] mapLayout = new int[121]; 
    int wrongKeyCount = 0;
    Dialog dialog = new Dialog();
    Battle battle = new Battle();        
    private boolean battleTriggered = false;
    
    // --- Getter and Setter for trashCleared ---
    public int getTrashCleared() {
        return trashCleared;
    }

    public void incrementTrashCleared() {
        this.trashCleared++;
        saveProgress();
    }

    public G9_Room2_PD6() {
        frame = new JFrame("PD6");
        layeredPane = new JLayeredPane();
        cleared = new boolean[121]; 
        
        ImageIcon mapImg = new ImageIcon("images/G9_map2_updated.png");
        background = new JLabel(new ImageIcon(mapImg.getImage().getScaledInstance(frW, frH, Image.SCALE_DEFAULT)));
        ImageIcon panelImg = new ImageIcon("images/G9_cutscn1.png");
        cutscene = new JLabel(new ImageIcon(panelImg.getImage().getScaledInstance(cutW, cutH, Image.SCALE_DEFAULT)));
        
        if (mapImg.getImageLoadStatus() == MediaTracker.COMPLETE) {
             mapImg = new ImageIcon(mapImg.getImage().getScaledInstance(frW, frH, Image.SCALE_SMOOTH));
        }
        background = new JLabel(mapImg);

        int tw = frW / mapW;
        int th = frH / mapH;
        int cw = (int)(tw * 0.6); 
        int ch = (int)(th * 0.6);

        pStand = scale("images/G9_pFront.png", cw, ch);
        pFrontW = scale("images/G9_pFront.png", cw, ch);
        pFrontW1 = scale("images/G9_pFrontLW.png", cw, ch);
        pFrontW2 = scale("images/G9_pFrontRW.png", cw, ch);
        pBack = scale("images/G9_pBack.png", cw, ch);
        pBack1 = scale("images/G9_pBack1.png", cw, ch);
        pBack2 = scale("images/G9_pBack2.png", cw, ch);
        pLeft = scale("images/G9_pLeft.png", cw, ch);
        pLeft1 = scale("images/G9_pLeft1.png", cw, ch);
        pLeft2 = scale("images/G9_pLeft2.png", cw, ch);
        pRight = scale("images/G9_pRight.png", cw, ch);
        pRight1 = scale("images/G9_pRight1.png", cw, ch);
        pRight2 = scale("images/G9_pRight2.png", cw, ch);
        
        grandma = scale("images/G9_grammy.png", tw, th);
        grandpa = scale("images/G9_grandpa.png", tw, th);

        // playMusic("dungeon9.wav");
        mapLayout = new int[]{
            1,0,0,0,1,1,1,0,0,0,0,
            0,0,0,0,1,1,1,0,0,0,0,
            1,0,0,0,1,1,1,1,1,0,0,
            1,0,0,0,0,0,0,0,0,0,0,
            1,0,0,0,0,0,0,0,0,0,0,
            1,0,1,1,1,1,1,1,1,1,1,
            2,0,0,0,0,0,0,0,0,0,1,
            2,0,0,1,1,1,1,1,0,0,1,
            1,1,1,1,0,0,1,1,1,0,1,
            1,1,1,0,0,0,0,0,0,0,1,
            1,1,1,1,1,1,1,1,1,1,1,
        };
        
        gridSlots = new JLabel[mapW * mapH];
        for (int i = 0; i < gridSlots.length; i++) {
            gridSlots[i] = new JLabel();
            gridSlots[i].setHorizontalAlignment(JLabel.CENTER);
            gridSlots[i].setVerticalAlignment(JLabel.CENTER);
        }
        loadSaveData();
    }

    
    private void renderEntities() {
        for (int i = 0; i < gridSlots.length; i++) {
            if (i == characterPosition) {
                gridSlots[i].setIcon(getAnimation());
            } else if (i == grandmaPos) {
                gridSlots[i].setIcon(grandma);
            } else if (i == grandpaPos) {
                gridSlots[i].setIcon(grandpa);
            } else {
                boolean trashPresent = false;
                for (int t : mapTrash) {
                    if (i == t && t != -1) {
                        trashPresent = true;
                        break;
                    }
                }
                gridSlots[i].setIcon(trashPresent ? scale("images/G9_trash.png", 30, 30) : null);
            }
        }
    }
    
    private void interact() {
    int targetPos = -1;
    switch (direction) {
        case "up":    targetPos = characterPosition - mapW; break;
        case "down":  targetPos = characterPosition + mapW; break;
        case "left":  if (characterPosition % mapW != 0) targetPos = characterPosition - 1; break;
        case "right": if ((characterPosition + 1) % mapW != 0) targetPos = characterPosition + 1; break;
    }
    
    

    // 1. Trash Pickup
    for (int i = 0; i < mapTrash.length; i++) {
        if (characterPosition == mapTrash[i] && mapTrash[i] != -1) {
            if (!talkedGrandma) {
                JOptionPane.showMessageDialog(frame, "Invalid. Interact with Grandma first.", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            if (itemsHeld == 0) {
                inv[0] = mapTrash[i];
                itemsHeld = 1;
                mapTrash[i] = -1; 
                dialog.show(layeredPane, new String[]{"[You've picked up trash!]"}, null, null, mapW, mapH);
                renderEntities();
                return; 
            }
        }
    }

    // 2. Grandma
    if (targetPos == grandmaPos) {
        talkedGrandma = true; 
        saveProgress();
        String[] lines = {
            "Listen carefully kid.",
            "Red bin = NonBio | Green bin = Bio | Blue bin = Recyclables",
            "If you mess up you'll hear from me. Also..",
            "If you find someone around my age here.. let me know. That guy's a pain to deal with...",
        };
        SwingUtilities.invokeLater(() -> dialog.show(layeredPane, lines, null, null, mapW, mapH));
        return;
    }

    // 3. Grandpa
    if (targetPos == grandpaPos) {
        dialog.show(layeredPane, new String[]{
            "Eugh. Kids these days...",
            "Have you seen my wife?",
            "I think I'm lost."
        }, null, null, mapW, mapH);
        return;
    }

    // 4. Bins
    if (characterPosition == binPos1 || characterPosition == binPos2 || targetPos == binPos1 || targetPos == binPos2) {
        trashEvent.handle(this, inv, itemsHeld, layeredPane, dialog, mapW, mapH, () -> {
            itemsHeld = 0;
            renderEntities();
        });
    }
}

    public boolean allTrashRemoved() {
        for (int t : mapTrash) {
            if (t != -1) return false;
        }
        return true;
    }
    
    private void setBlackOverlay(boolean visible) {
    if (blackOverlay != null) {
        blackOverlay.setVisible(visible);
        layeredPane.revalidate();
        layeredPane.repaint();
    }
}
    public void playCutscene(boolean objComplete) {
        if (!objComplete || battleTriggered) return;
        battleTriggered = true;
        setBlackOverlay(true);
        cutscene.setVisible(true); // show intro cutscene FIRST

        new javax.swing.Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                cutscene.setVisible(false);
                setBlackOverlay(false);
                // THEN start the battle after cutscene ends
                SwingUtilities.invokeLater(() -> {
                    G9_Room1_PD4.stopMusic();
                    battle.start(frame, "images/G9_minibossBG.png", "Bin Izharfed");
                });

                new javax.swing.Timer(500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        if (!Battle.paused) {
                            ((Timer) ev.getSource()).stop();
                            
                            if (battle.hp > 0) {
                                miniDefeat = true;
                                SaveSystem.markDefeated("Bin Izharfed");
                                saveProgress();
                                ImageIcon victoryImg = new ImageIcon("images/G9_cutscn2.png");
                                cutscene.setIcon(new ImageIcon(victoryImg.getImage()
                                    .getScaledInstance(cutW, cutH, Image.SCALE_DEFAULT)));
                                setBlackOverlay(true); 
                                cutscene.setVisible(true);
                                new javax.swing.Timer(5000, end -> {
                                    cutscene.setVisible(false);
                                    setBlackOverlay(false); 
                                    ((Timer) end.getSource()).stop();
                                }).start();
                            } else {
                                battleTriggered = false;
                                battle = new Battle();                                  
                                direction = "right";
                                renderEntities();
                                G9_Room1_PD4.playMusic("music/Bin Izharfed.wav");
                            }
                        }
                    }
                }).start();
            }
        }).start();
    }
    
    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G9_Room2_PD6");

        
        if (save.timeSeconds <= 0) {
            SaveSystem.SaveData pd4Save = SaveSystem.loadGame("G9_Room1_PD4");
            SaveSystem.startTimer(pd4Save.timeSeconds);
        } else {
            SaveSystem.startTimer(save.timeSeconds);
        }

        // load flags
        trashCleared = 0;
        if (save.flags != null) {
            for (String flag : save.flags) {
                if (flag.startsWith("cleared_")) {
                    int idx = Integer.parseInt(flag.replace("cleared_", ""));
                    if (idx >= 0 && idx < cleared.length) cleared[idx] = true;
                    trashCleared++;
                }
            }
        }
        objComplete = save.hasFlag("objective_complete");
        finalDialog = save.hasFlag("final_dialog_triggered");
        talkedGrandma = save.hasFlag("talked_to_grandma");
        miniDefeat = save.hasFlag("battle_won");
        rewardGiven = save.hasFlag("reward_given");
        
    }
    
    public void saveProgress() {
        SaveSystem.SaveData.Builder builder = new SaveSystem.SaveData.Builder("G9_Room2_PD6")
                .time(SaveSystem.getTotalSeconds())
                .flag(objComplete ? "objective_complete" : null)
                .flag(finalDialog ? "final_dialog_triggered" : null)
                .flag(talkedGrandma ? "talked_to_grandma" : null)
                .flag(miniDefeat ? "battle_won" : null)
                .flag(rewardGiven ? "reward_given" : null)
                .battles(SaveSystem.getDefeatedBosses());
        for (int i = 0; i < cleared.length; i++) {
            if (cleared[i]) builder.flag("cleared_" + i);
        }
        SaveSystem.saveGame(builder);
    }
   
    
    private ImageIcon scale(String path, int w, int h) {
        ImageIcon icon = new ImageIcon(path);
        return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
    }

    boolean isBlocked(int pos) {
        if (pos < 0 || pos >= mapLayout.length) return true;
        if (pos == grandmaPos || pos == grandpaPos) return true; 
        return mapLayout[pos] == 1;
    }

    ImageIcon getAnimation() {
        if (direction.equals("down"))  return animFrame == 0 ? pFrontW : animFrame == 1 ? pFrontW1 : pFrontW2;
        if (direction.equals("up"))    return animFrame == 0 ? pBack    : animFrame == 1 ? pBack1    : pBack2;
        if (direction.equals("left"))  return animFrame == 0 ? pLeft    : animFrame == 1 ? pLeft1    : pLeft2;
        if (direction.equals("right")) return animFrame == 0 ? pRight   : animFrame == 1 ? pRight1   : pRight2;
        return pStand;
    }
    
    public void setFrame() {
        cutscene.setVisible(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        layeredPane.setPreferredSize(new Dimension(frW, frH));
        layeredPane.setLayout(new GraphPaperLayout(new Dimension(mapW, mapH)));

        background.setBounds(0, 0, frW, frH);
        layeredPane.add(background, new Rectangle(0, 0, mapW, mapH));
        layeredPane.setLayer(background, JLayeredPane.DEFAULT_LAYER);

        charPanel = new JPanel();
        charPanel.setOpaque(false);
        charPanel.setLayout(new GraphPaperLayout(new Dimension(mapW, mapH)));
        charPanel.setBounds(0, 0, frW, frH);

        for (int i = 0; i < gridSlots.length; i++) {
            charPanel.add(gridSlots[i], new Rectangle(i % mapW, i / mapW, 1, 1));
        }

        // --- Black overlay: covers the whole map, sits above charPanel but below cutscene ---
        blackOverlay = new JPanel();
        blackOverlay.setBackground(Color.BLACK);
        blackOverlay.setOpaque(true);
        blackOverlay.setBounds(0, 0, frW, frH);
        blackOverlay.setVisible(false);
        layeredPane.add(blackOverlay, new Rectangle(0, 0, mapW, mapH));
        layeredPane.setLayer(blackOverlay, 1); // Layer 1: above map/chars

        layeredPane.add(cutscene, new Rectangle(1, 1, 9, 9));
        layeredPane.setLayer(cutscene, 2); // Layer 2: above the black overlay
        layeredPane.add(charPanel, new Rectangle(0, 0, mapW, mapH));
        layeredPane.setLayer(charPanel, Integer.valueOf(1));
      
        frame.add(layeredPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(this);
        dialog.addKey(frame);
        renderEntities();
        frame.setVisible(true);
        frame.requestFocusInWindow();
    }
    
    public static void main(String[] args){
        G9_Room2_PD6 g=new G9_Room2_PD6();
        g.setFrame();
        
    }
    
    private void checkKey(int key) {
    boolean isWASD  = (key == KeyEvent.VK_W || key == KeyEvent.VK_A ||
                       key == KeyEvent.VK_S || key == KeyEvent.VK_D);
    boolean isSpace = (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_Z);
    boolean isArrow = (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT ||
                       key == KeyEvent.VK_UP    || key == KeyEvent.VK_DOWN);

    if (!isSpace && !isWASD) {
        wrongKeyCount++;
        if (isFirstTimeM && isArrow) {
            wrongKeyCount = 0; isFirstTimeM = false;
            JOptionPane.showMessageDialog(frame, "Use WASD for movement.", "Input Error", JOptionPane.WARNING_MESSAGE);
            frame.requestFocusInWindow();
        } else if (isFirstTimeI && !isArrow && !isWASD && !isSpace) {
            wrongKeyCount = 0; isFirstTimeI = false;
            JOptionPane.showMessageDialog(frame, "Use spacebar to interact.", "Input Error", JOptionPane.WARNING_MESSAGE);
            frame.requestFocusInWindow();
        } else if (wrongKeyCount == 5) {
            wrongKeyCount = 0;
            JOptionPane.showMessageDialog(frame, "Don't forget to use WASD for movement and spacebar to interact.", "Input Error", JOptionPane.WARNING_MESSAGE);
            frame.requestFocusInWindow();
        }
    } else {
        wrongKeyCount = 0;
    }
}
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (dialog.isVisible()) return;
        int nextPos = characterPosition;
        int key = e.getKeyCode();

        checkKey(key);

        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_Z) {
            interact();
            return;
        }

        if (key == KeyEvent.VK_D) {
            direction = "right";
            if ((characterPosition + 1) % mapW == 0) {
                if (objComplete && !battleTriggered) {
                    playCutscene(true);
                }
                return;
            } else nextPos++;
        } else if (key == KeyEvent.VK_A) { direction = "left"; if (characterPosition % mapW != 0) nextPos--; }
        else if (key == KeyEvent.VK_S) { direction = "down"; if (characterPosition + mapW < 121) nextPos += mapW; }
        else if (key == KeyEvent.VK_W) { direction = "up"; if (characterPosition - mapW >= 0) nextPos -= mapW; }

        if (!isBlocked(nextPos)) {
            characterPosition = nextPos;
            animFrame = (animFrame + 1) % 3;
            renderEntities();
        }
    }
    
    @Override public void keyReleased(KeyEvent e) { animFrame = 0; renderEntities(); }
    @Override public void keyTyped(KeyEvent e) {}
}

// all assets, ideas, and concepts are human, but we've asked claude ai for help

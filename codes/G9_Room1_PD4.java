package codes;
// grp: ancla, badayos, sepe
    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.*;
    import java.util.ArrayList;
    import java.util.List;
    import java.io.File;
    import javax.sound.sampled.AudioInputStream;
    import javax.sound.sampled.AudioSystem;
    import javax.sound.sampled.Clip;

    // --- CUSTOM EXCEPTIONS ---
    class WASDException extends Exception {
        public WASDException(String message) { super(message); }
    }
    class InvalidKeyException extends Exception {
        public InvalidKeyException(String message) { super(message); }
    }
    class TooManyErrorsException extends Exception {
        public TooManyErrorsException(String message) { super(message); }
    }

    public class G9_Room1_PD4 implements KeyListener {
        JFrame frame;
        JLayeredPane layeredPane;
        JLabel background;
        JPanel charPanel;
        JLabel[] gridSlots;

        int mapW = 11, mapH = 11;
        int frW = 660, frH = 660;

        int characterPosition = 93;
        int npcPos = 47;
        int animFrame = 0;
        String direction = "down";

        // --- STATE TRACKING ---
        int wrongKeyCount = 0;
        boolean isFirstTimeM = true;
        boolean isFirstTimeI = true;
        boolean isFirstCheck = true; 
        boolean hasFinalGift = false;
        boolean talked = false;
        

        // --- STORY & QUIZ SYSTEM ---
        int[] toyLoc = new int[4];
        int invCont = 0; 
        int currentToyTarget = 0; 

        int wrongCount = 0; 
        boolean[][] wrongFlags = new boolean[4][3]; 
        boolean[] askedFlags = new boolean[4]; 

        JLabel[] toys;
        ImageIcon tYoyo, tCar, tBear, tKite, npcKid;
        ImageIcon pStand, pFrontW, pFrontW1, pFrontW2, pBack, pBack1, pBack2, pLeft, pLeft1, pLeft2, pRight, pRight1, pRight2;

        int[] mapLayout = new int[]{
            1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,2,2,2,2,2,2,2,2,
            1,1,1,2,0,0,0,0,0,0,0,
            1,1,1,0,0,0,0,0,0,0,0,
            3,3,3,0,0,0,0,0,0,0,0,
            3,3,3,0,0,0,0,0,0,0,0,
            3,3,3,0,0,0,0,0,0,0,0,
            1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1
        };

        Dialog dialog = new Dialog();

        public G9_Room1_PD4() {
            frame = new JFrame("Luminara Al-Qamar");

            layeredPane = new JLayeredPane();

            int tw = frW / mapW;
            int th = frH / mapH;
            int cw = (int)(tw * 0.7);
            int ch = (int)(th * 0.7);

            ImageIcon mapImg = new ImageIcon("images/G9_map1f.png");
            background = new JLabel(new ImageIcon(mapImg.getImage().getScaledInstance(frW, frH, Image.SCALE_DEFAULT)));

            // Assets
            pStand = scale("images/G9_pFront.png", cw, ch);
            pFrontW = scale("images/G9_pFront.png", cw, ch);
            pFrontW1 = scale("images/G9_pFrontLW.png", cw, ch);
            pFrontW2 = scale("images/G9_pFrontRW.png", cw, ch);
            pBack = scale("images/G9_pBack.png", cw, ch);
            pBack1 = scale("images/G9_pBack1.png", cw, ch);
            pBack2 = scale("images/G9_pBack2.png", cw, ch);
            pLeft = scale("images/G9_pLeft1.png", cw, ch);
            pLeft1 = scale("images/G9_pLeft.png", cw, ch);
            pLeft2 = scale("images/G9_pLeft2.png", cw, ch);
            pRight = scale("images/G9_pRight1.png", cw, ch);
            pRight1 = scale("images/G9_pRight.png", cw, ch);
            pRight2 = scale("images/G9_pRight2.png", cw, ch);

            npcKid = scale("images/G9_caseohT.png", cw, ch);
            tYoyo = scale("images/G9_yoyo.png", cw, ch);
            tCar = scale("images/G9_locket.png", cw, ch);
            tKite = scale("images/G9_torn kite.png", cw, ch);
            tBear = scale("images/G9_teddy.png", cw, ch);

            playMusic("music/Bin Izharfed.wav");

            gridSlots = new JLabel[mapW * mapH];
            toys = new JLabel[mapW * mapH];

            for (int i = 0; i < gridSlots.length; i++) {
                gridSlots[i] = new JLabel();
                gridSlots[i].setHorizontalAlignment(JLabel.CENTER);
                gridSlots[i].setVerticalAlignment(JLabel.CENTER);
                toys[i] = new JLabel();
            }

         List<Integer> validTiles = new ArrayList<>();

for (int i = 0; i < mapLayout.length; i++) {
    if (mapLayout[i] == 0) {

        boolean nearNPC =
            i == npcPos || 
            i == npcPos - 1 || 
            i == npcPos + 1 || 
            i == npcPos - mapW || 
            i == npcPos + mapW;

        if (!nearNPC && i != characterPosition) {
            validTiles.add(i);
        }
    }
}
            for (int i = 0; i < 4; i++) {
                if (!validTiles.isEmpty()) {
                    int index = (int)(Math.random() * validTiles.size());
                    toyLoc[i] = validTiles.get(index);
                    validTiles.remove(index);
                }
            }
            loadSaveData();
        }

        public static Clip bgmClip = null;

        public static void playMusic(String location) {
            try {
                File musicPath = new File(location);
                if (musicPath.exists()) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(audioInput);
                    bgmClip.start();
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    System.out.println("Can't find audio file: " + location);
                }
            } catch(Exception e) {
                System.out.println("Error playing music: " + e);
            }
        }

        public static void stopMusic() {
            if (bgmClip != null && bgmClip.isOpen()) {
                if (bgmClip.isRunning()) bgmClip.stop();
                bgmClip.close();
                bgmClip = null;
            }
        }

        private ImageIcon scale(String path, int w, int h) {
            ImageIcon icon = new ImageIcon(path);
            return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
        }

        private void render() {
            boolean onStairs = mapLayout[characterPosition] == 3;
            for (int i = 0; i < gridSlots.length; i++) {
                if (i == characterPosition) {
                    ImageIcon anim = getAnimation();
                    if (onStairs) {
                        gridSlots[i].setVerticalAlignment(JLabel.TOP);
                        gridSlots[i].setIcon(padDown(anim, 20));
                    } else {
                        gridSlots[i].setVerticalAlignment(JLabel.CENTER);
                        gridSlots[i].setIcon(anim);
                    }
                }
                else if (i == npcPos) gridSlots[i].setIcon(npcKid);
                else if (toys[i].getIcon() != null) gridSlots[i].setIcon(toys[i].getIcon());
                else gridSlots[i].setIcon(null);
            }
        }

        private ImageIcon getAnimation() {
            if (direction.equals("down"))  return animFrame == 0 ? pFrontW : animFrame == 1 ? pFrontW1 : pFrontW2;
            if (direction.equals("up"))    return animFrame == 0 ? pBack    : animFrame == 1 ? pBack1    : pBack2;
            if (direction.equals("left"))  return animFrame == 0 ? pLeft    : animFrame == 1 ? pLeft1    : pLeft2;
            if (direction.equals("right")) return animFrame == 0 ? pRight   : animFrame == 1 ? pRight1   : pRight2;
            return pStand;
        }

        private void checkKey(int keyCode) throws WASDException, InvalidKeyException, TooManyErrorsException {
            boolean isWASD = (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_A || 
                              keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_D);
            boolean isSpace = (keyCode == KeyEvent.VK_SPACE);
            boolean isArrow = (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_LEFT ||
                               keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN);

            if (!isSpace && !isWASD) {
                wrongKeyCount++;
                if (isFirstTimeM && isArrow) {
                    wrongKeyCount = 0; isFirstTimeM = false;
                    throw new TooManyErrorsException("Use WASD for movement.");
                } else if (isFirstTimeI && !isArrow && !isWASD && !isSpace) {
                    wrongKeyCount = 0; isFirstTimeI = false;
                    throw new TooManyErrorsException("Use Spacebar to interact.");
                }
                if (wrongKeyCount == 5) {
                    wrongKeyCount = 0;
                    throw new TooManyErrorsException("Don't forget to use WASD for movement and Spacebar to interact.");
                }
            } else {
                wrongKeyCount = 0;
            }
        }

@Override
public void keyPressed(KeyEvent e) {
    if (dialog.isVisible()) return;
    int keyCode = e.getKeyCode();
    
            try {
                checkKey(keyCode);
                if (keyCode == KeyEvent.VK_D) { move(1, 0, "right"); }
                else if (keyCode == KeyEvent.VK_A) { move(-1, 0, "left"); }
                else if (keyCode == KeyEvent.VK_S) { move(0, 1, "down"); }
                else if (keyCode == KeyEvent.VK_W) { move(0, -1, "up"); }
                else if (keyCode == KeyEvent.VK_SPACE) { handleInteraction(); }
            } catch (WASDException | InvalidKeyException | TooManyErrorsException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
                frame.requestFocusInWindow();
            }
}

private void move(int dx, int dy, String dir) {
            int next = characterPosition;
            boolean atEdge = false;
            boolean onStairs = false;

            if (mapLayout[characterPosition] == 3) onStairs = true;
            if (atEdge && hasFinalGift) {
            startNextMap();
            return;
        } else if (atEdge && hasFinalGift==false) {
            dialog.show(layeredPane, new String[]{
                "Hey, you there! Can you help me please?"
            }, null, null, mapW, mapH);
        }

        if (dx == 1) {
            if ((characterPosition + 1) % mapW == 0) {
                direction = dir;
                if (hasFinalGift) {
                    startNextMap();
                } else {
                    dialog.show(layeredPane, new String[]{
                        "Hey, you there! Can you help me please?"
                    }, null, null, mapW, mapH);
                }
                return;
            } else next++;
        } else if (dx == -1) {
            if (characterPosition % mapW == 0) {}
            else next--;
        } else if (dy == 1) {
            if (characterPosition + mapW < mapLayout.length) next += mapW;
        } else if (dy == -1) {
            if (characterPosition - mapW >= 0) next -= mapW;
        }

        direction = dir;

            if (atEdge && hasFinalGift) {
                startNextMap();
                return;
            } else if (atEdge && hasFinalGift==false) {
                dialog.show(layeredPane, new String[]{
                    "Hey, you there! Can you help me please?"
                }, null, null, mapW, mapH);
            }

            if (next >= 0 && next < mapLayout.length && mapLayout[next] != 1 && next != npcPos) {
                characterPosition = next;
                animFrame = (animFrame + 1) % 3;
            }
            render();
        }
        
        private ImageIcon padDown(ImageIcon icon, int pixels) {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            java.awt.image.BufferedImage buf = new java.awt.image.BufferedImage(w, h + pixels, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = buf.createGraphics();
            g.drawImage(icon.getImage(), 0, pixels, null);
            g.dispose();
            return new ImageIcon(buf);
        }
        private void handleInteraction() {
            if (isNearNPC()) {
                if (!talked) showIntro();
                else if (currentToyTarget == 4 && !hasFinalGift) giveFinalGift();
                else if (invCont > currentToyTarget) processReturn();
                else if (hasFinalGift) dialog.show(layeredPane, new String[]{"Oh look! The path has opened, continue on to the right."}, null, null, mapW, mapH);
                else dialog.show(layeredPane, new String[]{"While the fog's too thick, please find the next toy."}, null, null, mapW, mapH);
                return;
            }
            if (talked && invCont == currentToyTarget) checkTileForToy();
        }

        private void giveFinalGift() {
            hasFinalGift = true;
            saveProgress();
            dialog.show(layeredPane, new String[]{
                "You have brought the light back.",
                "Take this fragment of memory. It will guide you through the next gate.",
                "Now, walk towards the edge. Don't look back."
            }, null, null, mapW, mapH);
        }

        private boolean isNearNPC() {
            return (Math.abs(characterPosition - npcPos) == 1 || Math.abs(characterPosition - npcPos) == mapW);
        }

        private void checkTileForToy() {
            boolean found = false;
            if (characterPosition == toyLoc[0] && currentToyTarget == 0) {toys[characterPosition].setIcon(tBear); found = true; saveProgress();}
            else if (characterPosition == toyLoc[1] && currentToyTarget == 1) { toys[characterPosition].setIcon(tKite); found = true; saveProgress();}
            else if (characterPosition == toyLoc[2] && currentToyTarget == 2) { toys[characterPosition].setIcon(tYoyo); found = true; saveProgress();}
            else if (characterPosition == toyLoc[3] && currentToyTarget == 3) { toys[characterPosition].setIcon(tCar); found = true;saveProgress(); }

            if (found) {
                invCont++;
                isFirstCheck = false; 
                JOptionPane.showMessageDialog(frame, "You retrieved a memory.");
            } else {
                if (isFirstCheck) {
                    JOptionPane.showMessageDialog(frame, "Not here. Keep looking.");
                    isFirstCheck = false; 
                } else if (mapLayout[characterPosition] == 2) {
                    JOptionPane.showMessageDialog(frame, "The mist is still cold. Please, find the next toy.");
                }
            }
            render();
        }

        private void showIntro() {
            talked = true;
            saveProgress();
            dialog.show(layeredPane, new String[]{
                "They told me as long as I held these, they'd never truly be gone.",
                "But the sky fell... and my hands slipped.",
                "I'm starting to forget the sound of their voices.",
                "Please... can you find Callisto first? It got covered by the clouds on the floor."
            }, null, null, mapW, mapH);
        }

        private void processReturn() {
            ArrayList<String> currentOpts = new ArrayList<>();
            ArrayList<Runnable> currentNext = new ArrayList<>();
            String[] lines;

            if (currentToyTarget == 0) {
                lines = (!askedFlags[0]) 
                    ? new String[]{"My bear... my parents told me stories about the Greek myth when they tucked me to bed.", "Which war was it... the one where the old gods fell?"} 
                    : new String[]{"Which war destroyed the old gods?"};
                addOption(currentOpts, currentNext, 0, 0, "Odyssey", () -> handleWrong(0));
                addOption(currentOpts, currentNext, 0, 1, "Titanomachy", () -> handleCorrect("Yes. Find my kite next."));
                addOption(currentOpts, currentNext, 0, 2, "Iliad", () -> handleWrong(0));
            } 
            else if (currentToyTarget == 1) {
                lines = (!askedFlags[1]) 
                    ? new String[]{"My kite! Father's hands were so warm when he guided the string.", "I forgot what he used to call that 'nothingness' before the world began.."} 
                    : new String[]{"What was the 'nothingness' called?"};
                addOption(currentOpts, currentNext, 1, 0, "Chaos", () -> handleCorrect("Chaos. Yes. Find my yo-yo."));
                addOption(currentOpts, currentNext, 1, 1, "Elysium", () -> handleWrong(1));
                addOption(currentOpts, currentNext, 1, 2, "Olympus", () -> handleWrong(1));
            }
            else if (currentToyTarget == 2) {
                lines = (!askedFlags[2]) 
                    ? new String[]{"Down and up... like my heart when I hear the wind.", "Who are the three sisters who cut the thread of life?"} 
                    : new String[]{"Who cuts the thread?"};
                addOption(currentOpts, currentNext, 2, 0, "The Muses", () -> handleWrong(2));
                addOption(currentOpts, currentNext, 2, 1, "The Furies", () -> handleWrong(2));
                addOption(currentOpts, currentNext, 2, 2, "The Moirai", () -> handleCorrect("The Moirai. Yes. Only the locket is left."));
            }
            else if (currentToyTarget == 3) {
                lines = (!askedFlags[3]) 
                    ? new String[]{"My locket. It holds the picture of them.", "Mother said the hunter who chased Castillo was his son. His name?"} 
                    : new String[]{"What was the name of the hunter son?"};
                addOption(currentOpts, currentNext, 3, 0, "Arcas", () -> handleCorrect("Arcas. Now I remember everything."));
                addOption(currentOpts, currentNext, 3, 1, "Actaeon", () -> handleWrong(3));
                addOption(currentOpts, currentNext, 3, 2, "Adonis", () -> handleWrong(3));
            }
            else { lines = new String[]{"The mist is clearing. I can see them again. Thank you."}; }

            dialog.show(layeredPane, lines, currentOpts.toArray(new String[0]), currentNext.toArray(new Runnable[0]), mapW, mapH);
        }

        private void handleWrong(int stage) {
            wrongCount++; askedFlags[stage] = true;
            dialog.show(layeredPane, new String[]{"I don't think that's right..."}, null, null, mapW, mapH);
        }

private void handleCorrect(String msg) {
    currentToyTarget++;
    wrongCount = 0;

    // If last answer was just completed
    if (currentToyTarget == 4) {
        hasFinalGift = true;
        saveProgress();

        dialog.show(layeredPane, new String[]{
            msg,
            "You have brought the light back.", 
            "Take this fragment of memory. It will guide you through the next gate.",  
            "Now, walk towards the edge. Don't look back."
        }, null, null, mapW, mapH);
    } else {
        dialog.show(layeredPane, new String[]{msg}, null, null, mapW, mapH);
    }
}

        private void startNextMap() {
            SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("G9_Room2_PD6")
                    .time(SaveSystem.getTotalSeconds())
            );
            frame.dispose();
            new G9_Room2_PD6().setFrame();
        }

       
        private void loadSaveData() {
            SaveSystem.SaveData save = SaveSystem.loadGame("G9_Room1_PD4");

            SaveSystem.startTimer(save.timeSeconds);

            // talked, # toy, obj finsihed
            talked = save.hasFlag("talked_to_npc");
            for (int i = 1; i <= 5; i++) {
                if (save.hasFlag("toy_progress_" + i)) {
                    currentToyTarget = i;
                }
            }
            hasFinalGift = save.hasFlag("finished_objective");
        }
        
        private void saveProgress() {
            SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("G9_Room1_PD4")
                    .time(SaveSystem.getTotalSeconds()) 
                    .flag(talked ? "talked_to_npc" : null)
                    .flag(hasFinalGift ? "finished_objective" : null)
                    .flag("toy_progress_" + currentToyTarget)
            );
        }
        private void addOption(List<String> opts, List<Runnable> nexts, int stage, int optIdx, String text, Runnable action) {
            if (!wrongFlags[stage][optIdx]) {
                opts.add(text);
                nexts.add(() -> {
                    int oldTarget = currentToyTarget;
                    action.run();
                    if (currentToyTarget == oldTarget) {
                        if (wrongCount == 1) wrongFlags[stage][optIdx] = true;
                        processReturn(); 
                    }
                });
            }
        }

        public void setFrame() {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            layeredPane.setPreferredSize(new Dimension(frW, frH));
            layeredPane.setLayout(new GraphPaperLayout(new Dimension(mapW, mapH)));

            charPanel = new JPanel();
            charPanel.setOpaque(false);
            charPanel.setLayout(new GraphPaperLayout(new Dimension(mapW, mapH)));

            for (int i = 0; i < gridSlots.length; i++) {
                charPanel.add(gridSlots[i], new Rectangle(i % mapW, i / mapW, 1, 1));
            }

            layeredPane.setLayout(new GraphPaperLayout(new Dimension(mapW, mapH)));

            layeredPane.add(background, new Rectangle(0, 0, mapW, mapH));
            layeredPane.setLayer(background, 0);

            layeredPane.add(charPanel, new Rectangle(0, 0, mapW, mapH));
            layeredPane.setLayer(charPanel, 1);



            frame.add(layeredPane);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addKeyListener(this);
            dialog.addKey(frame);
            render();
            frame.setVisible(true);
        }

        public static void main(String[] args){
            G9_Room1_PD4 g = new G9_Room1_PD4();
            g.setFrame();
        }
        
        @Override public void keyReleased(KeyEvent e) { animFrame = 0; render(); }
        @Override public void keyTyped(KeyEvent e) {}
    }

/*all the assets, concepts are human but we had help in the code from claude AI*/

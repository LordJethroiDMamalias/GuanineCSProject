//Guanine Group 6: Estrellan, Gomez, Pañares
package codes;
import javax.sound.sampled.*;
import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class G6_Room2_PD6 extends JFrame implements KeyListener {

    int mapw = 11;
    int maph = 11;
    int frameWidth = 660;
    int frameHeight = 660;
    Clip bgMusic;
    JLabel[] chr = new JLabel[mapw * maph];
    int chrpos = 34;

    Dialog dialog = new Dialog();

    ImageIcon icon, icon2, icon3, icon4, icon5,
              icon6, icon7, icon8, icon9, icon10;

    int chrmodeR = 0, chrmodeL = 0, chrmodeU = 0, chrmodeD = 0;

    Set<Integer> questionTiles = new HashSet<>();
    Set<Integer> answeredTiles = new HashSet<>();
    final int TOTAL_QUESTIONS = 10;
    final int EXIT_TILE = 9 * 11 + 1; // row9 col1

    // Map layout
    int[] mapLayout = {
        0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,
        0,1,1,1,1,1,1,1,1,1,0,
        0,1,1,1,0,0,0,1,1,1,0,
        0,1,1,1,0,0,0,1,1,1,0,
        0,1,1,1,0,0,0,1,1,1,0,
        0,1,1,1,1,1,1,1,1,1,0,
        0,1,1,1,1,1,1,1,1,1,0,
        0,1,1,1,1,1,1,1,1,1,0,
        0,0,0,0,0,0,0,0,0,0,0
    };

    SaveSystem.SaveData saveData;

    // ========== CONSTRUCTORS ==========

    /**
     * Called when transitioning from Room 1.
     * Accepts the already-playing Clip so music continues without interruption.
     */
    public G6_Room2_PD6(Clip existingClip) {
        this.bgMusic = existingClip; // reuse Room 1's clip — no gap in music
    }

    /**
     * No-arg constructor for standalone launch (e.g. main method).
     * bgMusic stays null; setFrame() will start fresh music.
     */
    public G6_Room2_PD6() {
        // bgMusic is null — playMusic() will be called in setFrame()
    }

    // ========== MUSIC ==========

    void playMusic(String path) {
        try {
            File musicFile = new File(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            bgMusic = AudioSystem.getClip();
            bgMusic.open(audioStream);
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            bgMusic.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopMusic() {
        if (bgMusic != null && bgMusic.isRunning()) {
            bgMusic.stop();
            bgMusic.close();
        }
    }

    // ========== FRAME ==========

    public void setFrame() {
        // Only start music if no existing clip was passed in from Room 1
        if (bgMusic == null) {
            playMusic("music/Main.wav");
        }
        // If bgMusic was passed in from Room1, it is already playing — do nothing.

        // ---------- LOAD SAVE ----------
        saveData = SaveSystem.loadGame("G6_Room2_PD6");
        SaveSystem.startTimer(saveData.timeSeconds);

        // ---------- SPRITES ----------
        icon  = new ImageIcon("images/G6_char1.png");
        icon2 = new ImageIcon("images/G6_char2.png");
        icon3 = new ImageIcon("images/G6_char3.png");
        icon4 = new ImageIcon("images/G6_char4.png");
        icon5 = new ImageIcon("images/G6_char5.png");
        icon6 = new ImageIcon("images/G6_char6.png");
        icon7 = new ImageIcon("images/G6_char7.png");
        icon8 = new ImageIcon("images/G6_char8.png");
        icon9 = new ImageIcon("images/G6_char9.png");
        icon10= new ImageIcon("images/G6_char10.png");

        int tileW = frameWidth / mapw;
        int tileH = frameHeight / maph;

        icon  = scale(icon, tileW, tileH);
        icon2 = scale(icon2, tileW, tileH);
        icon3 = scale(icon3, tileW, tileH);
        icon4 = scale(icon4, tileW, tileH);
        icon5 = scale(icon5, tileW, tileH);
        icon6 = scale(icon6, tileW, tileH);
        icon7 = scale(icon7, tileW, tileH);
        icon8 = scale(icon8, tileW, tileH);
        icon9 = scale(icon9, tileW, tileH);
        icon10= scale(icon10, tileW, tileH);

        for (int i = 0; i < chr.length; i++) {
            chr[i] = new JLabel();
            chr[i].setOpaque(false);
            chr[i].setHorizontalAlignment(SwingConstants.CENTER);
            chr[i].setVerticalAlignment(SwingConstants.CENTER);
        }

        chr[chrpos].setIcon(icon);

        // ---------- LAYERS ----------
        JLayeredPane layers = getLayeredPane();
        layers.removeAll();
        layers.setLayout(new GraphPaperLayout(new Dimension(mapw, maph)));

        // Background
        ImageIcon bg = new ImageIcon("images/G6_LabMap.png");
        JLabel background = new JLabel(
            new ImageIcon(bg.getImage().getScaledInstance(frameWidth, frameHeight, Image.SCALE_SMOOTH))
        );
        layers.add(background, new Rectangle(0, 0, mapw, maph));
        layers.setLayer(background, Integer.valueOf(0));

        // Character
        int x = 0, y = 0;
        for (int i = 0; i < chr.length; i++) {
            layers.add(chr[i], new Rectangle(x, y, 1, 1));
            layers.setLayer(chr[i], Integer.valueOf(100));
            x++;
            if (x % mapw == 0) { x = 0; y++; }
        }

        setSize(frameWidth, frameHeight);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopMusic();
                System.exit(0);
            }
        });
        setVisible(true);
        addKeyListener(this);
        setFocusable(true);

        // ---------- RANDOM QUESTION TILES ----------
        Random rand = new Random();
        while (questionTiles.size() < TOTAL_QUESTIONS) {
            int cand = rand.nextInt(mapLayout.length);
            if (mapLayout[cand] == 1 && cand != chrpos && cand != EXIT_TILE)
                questionTiles.add(cand);
        }

        dialog.addKey(this);
        dialog.show(getLayeredPane(),
            new String[]{
                "This is the Chemistry Laboratory, be careful with every step.",
                "You may come across sneaky questions you HAVE to answer.",
                "Questions may repeat to test your memory and wit."
            },
            null, null, mapw, maph,
            () -> requestFocusInWindow()
        );

        requestFocusInWindow();
    }

    ImageIcon scale(ImageIcon img, int w, int h) {
        return new ImageIcon(img.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    // ========== QUESTIONS ==========

    void triggerQuestion() {
        String[][] qBank = {
            {"What is H2O?", "Water","Oxygen","Hydrogen","Salt"},
            {"pH of neutral?", "7","1","14","0"},
            {"NaCl is?", "Salt","Acid","Base","Gas"},
            {"HCl is?", "Acid","Base","Salt","Neutral"},
            {"CO2 is?", "Gas","Liquid","Solid","Metal"},
            {"Atomic number of H?", "1","2","8","16"},
            {"Na is?", "Metal","Gas","Acid","Nonmetal"},
            {"O2 is?", "Gas","Liquid","Metal","Salt"},
            {"H2SO4 is?", "Acid","Base","Salt","Gas"},
            {"CH4 is?", "Methane","Ethane","Propane","Butane"},
            {"K is?", "Potassium","Calcium","Sodium","Iron"},
            {"Fe is?", "Iron","Gold","Silver","Copper"},
            {"Au is?", "Gold","Iron","Silver","Lead"},
            {"Ag is?", "Silver","Gold","Copper","Zinc"},
            {"Zn is?", "Zinc","Iron","Gold","Silver"},
            {"Cl is?", "Chlorine","Fluorine","Oxygen","Hydrogen"},
            {"F is?", "Fluorine","Chlorine","Oxygen","Hydrogen"},
            {"H2 is?", "Gas","Liquid","Solid","Metal"},
            {"O3 is?", "Ozone","Oxygen","Carbon","Nitrogen"},
            {"N2 is?", "Nitrogen","Oxygen","Hydrogen","Carbon"},
            {"C is?", "Carbon","Calcium","Copper","Chlorine"},
            {"Ca is?", "Calcium","Carbon","Copper","Chlorine"},
            {"Cu is?", "Copper","Carbon","Calcium","Chlorine"},
            {"Mg is?", "Magnesium","Manganese","Mercury","Gold"},
            {"Hg is?", "Mercury","Magnesium","Gold","Silver"},
            {"Pb is?", "Lead","Iron","Silver","Copper"},
            {"Sn is?", "Tin","Iron","Lead","Copper"},
            {"Al is?", "Aluminum","Iron","Copper","Gold"},
            {"Si is?", "Silicon","Sulfur","Silver","Sodium"},
            {"P is?", "Phosphorus","Potassium","Platinum","Lead"},
            {"What is H2O?", "Water", "Oxygen", "Hydrogen", "Salt"},
            {"pH of neutral substance?", "7", "1", "14", "0"},
            {"NaCl is commonly known as?", "Salt","Acid","Base","Gas"},
            {"HCl is classified as?", "Acid","Base","Salt","Neutral"},
            {"CO2 is in what state at room temp?", "Gas","Liquid","Solid","Metal"},
            {"Atomic number of Hydrogen?", "1","2","8","16"},
            {"Na represents which element?", "Sodium","Neon","Nitrogen","Nickel"},
            {"O2 is essential for?", "Respiration","Combustion","Rusting","All of these"},
            {"H2SO4 is known as?", "Sulfuric acid","Hydrochloric acid","Nitric acid","Carbonic acid"},
            {"CH4 is?", "Methane","Ethane","Propane","Butane"},
            {"K symbolizes?", "Potassium","Calcium","Sodium","Iron"},
            {"Fe stands for?", "Iron","Gold","Silver","Copper"},
            {"Au stands for?", "Gold","Iron","Silver","Lead"},
            {"Ag is the symbol for?", "Silver","Gold","Copper","Zinc"},
            {"Zn represents?", "Zinc","Iron","Gold","Silver"},
            {"Cl is the symbol of?", "Chlorine","Fluorine","Oxygen","Hydrogen"},
            {"F stands for?", "Fluorine","Chlorine","Oxygen","Hydrogen"},
            {"Li stands for?", "Lithium","Lead","Lanthanum","Lutetium"},
            {"He is?", "Helium","Hydrogen","Hafnium","Holmium"},
            {"Fe's atomic number?", "26","24","25","27"},
            {"Which element has symbol Ca?", "Calcium","Carbon","Cobalt","Copper"},
            {"Symbol of Potassium?", "K","P","Pt","Po"},
            {"Symbol for Sodium?", "Na","S","N","Si"}
        };

        int qIndex = new Random().nextInt(qBank.length);
        askUntilCorrect(qBank[qIndex]);
    }

    void askUntilCorrect(String[] qData) {
        String question = qData[0];
        String correct = qData[1];
        ArrayList<String> choices = new ArrayList<>();
        for (int i = 1; i <= 4; i++) choices.add(qData[i]);
        Collections.shuffle(choices);

        Runnable[] events = new Runnable[4];

        for (int i = 0; i < 4; i++) {
            String ans = choices.get(i);
            if (ans.equals(correct)) {
                events[i] = () -> {
                    answeredTiles.add(chrpos);
                    dialog.show(getLayeredPane(),
                        new String[]{
                            "Correct!",
                            "Progress: " + answeredTiles.size() + "/" + TOTAL_QUESTIONS
                        },
                        null, null, mapw, maph,
                        () -> {
                            if (answeredTiles.size() >= TOTAL_QUESTIONS) {
                                dialog.show(getLayeredPane(),
                                    new String[]{
                                        "More journeys await you, you may now proceed [exit is at lower left corner]"
                                    },
                                    null, null, mapw, maph,
                                    () -> requestFocusInWindow());
                            } else requestFocusInWindow();
                        }
                    );
                };
            } else {
                events[i] = () ->
                    dialog.show(getLayeredPane(),
                        new String[]{"Wrong! Try again."},
                        null, null, mapw, maph,
                        () -> askUntilCorrect(qData)
                    );
            }
        }

        dialog.show(getLayeredPane(),
            new String[]{question},
            choices.toArray(new String[0]),
            events,
            mapw, maph,
            () -> requestFocusInWindow()
        );
    }

    // ========== MOVEMENT (WASD) ==========

    @Override
    public void keyPressed(KeyEvent e) {

        if (dialog.isVisible()) return;

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_D) {
            if (chrpos % mapw != mapw - 1 && mapLayout[chrpos + 1] != 0) {
                chr[chrpos].setIcon(null);
                chrpos++;
                if (chrmodeR == 0) { chr[chrpos].setIcon(icon2); chrmodeR = 1; }
                else               { chr[chrpos].setIcon(icon3); chrmodeR = 0; }
                resetModesExcept("R");
            }
        } else if (key == KeyEvent.VK_A) {
            if (chrpos % mapw != 0 && mapLayout[chrpos - 1] != 0) {
                chr[chrpos].setIcon(null);
                chrpos--;
                if (chrmodeL == 0) { chr[chrpos].setIcon(icon4); chrmodeL = 1; }
                else               { chr[chrpos].setIcon(icon5); chrmodeL = 0; }
                resetModesExcept("L");
            }
        } else if (key == KeyEvent.VK_S) {
            if (chrpos + mapw < mapLayout.length && mapLayout[chrpos + mapw] != 0) {
                chr[chrpos].setIcon(null);
                chrpos += mapw;
                if      (chrmodeD == 0) { chr[chrpos].setIcon(icon);  chrmodeD = 1; }
                else if (chrmodeD == 1) { chr[chrpos].setIcon(icon6); chrmodeD = 2; }
                else                    { chr[chrpos].setIcon(icon7); chrmodeD = 0; }
                resetModesExcept("D");
            }
        } else if (key == KeyEvent.VK_W) {
            if (chrpos - mapw >= 0 && mapLayout[chrpos - mapw] != 0) {
                chr[chrpos].setIcon(null);
                chrpos -= mapw;
                if      (chrmodeU == 0) { chr[chrpos].setIcon(icon8);  chrmodeU = 1; }
                else if (chrmodeU == 1) { chr[chrpos].setIcon(icon9);  chrmodeU = 2; }
                else                    { chr[chrpos].setIcon(icon10); chrmodeU = 0; }
                resetModesExcept("U");
            }
        }

        // ---------- QUESTION TRIGGER ----------
        if (questionTiles.contains(chrpos)
                && !answeredTiles.contains(chrpos)
                && answeredTiles.size() < TOTAL_QUESTIONS) {
            triggerQuestion();
        }

        // ---------- EXIT ----------
        if (answeredTiles.size() >= TOTAL_QUESTIONS && chrpos == EXIT_TILE) {
            SaveSystem.pauseTimer();
            SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("G6_Room2_PD6")
                    .flags(saveData.flags)
                    .battles(SaveSystem.getDefeatedBosses())
            );

            dialog.show(getLayeredPane(),
                new String[]{
                    "You have exited the Chemistry Lab!",
                    "Progress saved. Total time: " +
                    new SaveSystem.SaveData.Builder("G6_Room2_PD6")
                        .time(SaveSystem.getTotalSeconds())
                        .build().formattedTime()
                },
                null, null, mapw, maph,
                () -> {
                    stopMusic();
                    dispose();
                }
            );
        }

        repaint();
    }

    void resetModesExcept(String dir) {
        if (!dir.equals("R")) chrmodeR = 0;
        if (!dir.equals("L")) chrmodeL = 0;
        if (!dir.equals("U")) chrmodeU = 0;
        if (!dir.equals("D")) chrmodeD = 0;
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        G6_Room2_PD6 room = new G6_Room2_PD6();
        room.setFrame();
    }
}
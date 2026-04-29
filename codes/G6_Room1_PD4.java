//Guanine Group 6: Estrellan, Gomez, Pañares
//with savesystem
//boss activates when you step on the 108th tile
package codes;


import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Arrays;


public class G6_Room1_PD4 implements KeyListener {


    JFrame frame;
    ImageIcon wall, path, ball, exit;
    JLabel[] tiles;
    JLabel[] chr;


    ImageIcon icon, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9, icon10;
    ImageIcon dan, tine;
    ImageIcon icon11, icon12; // right_4, left_4

    ImageIcon playerBattle;

    int[] mapLayout;
    int chrpos;


    int mapw = 11, maph = 11;
    int frameWidth = 660, frameHeight = 660;


    int chrmodeR, chrmodeL, chrmodeD, chrmodeU;


    int totalBalls = 0;
    int collectedBalls = 0;
    boolean bossDefeated = false;


    Dialog dialog;
    boolean introShown = false;
    boolean tile94DialogShown = false;

    boolean bossPollerActive = false;

    // MUSIC
    Clip bgmClip = null;

    // SAVE SYSTEM
    SaveSystem.SaveData saveData;


    // QUIZ DATA
    String[] questions = new String[30];
    String[][] choices = new String[30][3];
    int[] correctAnswer = new int[30];
    ArrayList<Integer> unusedQuestions = new ArrayList<>();
    Random rand = new Random();


    public G6_Room1_PD4() {
        frame = new JFrame();
        dialog = new Dialog();
        loadImages();
        initMap();
        initQuiz();


        loadSaveData();
        saveProgress();
        // bossDefeated intentionally NOT restored from save —
        // the player must fight Main every session to reach Room 2.
    }

    
    // ========== IMAGE LOADER ==========
    void loadImages() {
    int tw = frameWidth / mapw;
    int th = frameHeight / maph;
    int cw = (int)(tw * 0.7);
    int ch = (int)(th * 0.7);


    icon   = scaledWH("images/down_1.png",  cw, ch);
    icon2  = scaledWH("images/right_1.png",  cw, ch);
    icon3  = scaledWH("images/right_2.png",  cw, ch);
    icon4  = scaledWH("images/left_1.png",  cw, ch);
    icon5  = scaledWH("images/left_2.png",  cw, ch);
    icon6  = scaledWH("images/down_4.png",  cw, ch);
    icon7  = scaledWH("images/down_2.png",  cw, ch);
    icon8  = scaledWH("images/up_1.png",  cw, ch);
    icon9  = scaledWH("images/up_2.png",  cw, ch);
    icon10 = scaledWH("images/up_4.png", cw, ch);
    icon11 = scaledWH("images/right_4.png", cw, ch);
    icon12 = scaledWH("images/left_4.png", cw, ch);
    
    wall = scaled("images/G6_wall.png",     tw);
    path = scaled("images/G6_bluepath.png", tw);
    ball = scaled("images/G6_ball.png",     tw);
    exit = scaled("images/G6_door.png",     tw);
    dan  = scaled("images/G6_dan.png",      tw);
    tine = scaled("images/G6_tine.png",     tw);

    playerBattle = new ImageIcon("images/G6_char2.png");
}
    private ImageIcon scaledWH(String p, int w, int h) {
        return new ImageIcon(new ImageIcon(p).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
    private ImageIcon scaled(String p, int s) {
        return new ImageIcon(new ImageIcon(p).getImage()
                .getScaledInstance(s, s, Image.SCALE_DEFAULT));
    }


    // ========== MAP ==========
    void initMap() {
        mapLayout = new int[]{
            0,0,0,0,0,0,0,0,0,0,0,   // row 0  (indices 0-10)
            4,1,1,1,1,1,0,1,1,1,0,   // row 1  (indices 11-21)
            0,2,0,1,0,1,0,2,0,1,0,   // row 2  (indices 22-32)
            0,1,0,1,0,1,1,1,1,1,0,   // row 3  (indices 33-43)
            0,0,0,1,0,0,0,0,1,1,0,   // row 4  (indices 44-54)
            0,1,0,1,1,1,1,0,2,0,0,   // row 5  (indices 55-65)
            0,1,1,1,0,1,1,1,1,1,0,   // row 6  (indices 66-76)
            0,2,0,1,0,0,1,0,0,0,0,   // row 7  (indices 77-87)
            0,1,0,1,2,0,1,1,1,1,0,   // row 8  (indices 88-98)
            0,1,1,1,0,0,1,0,1,1,3,   // row 9  (indices 99-109)
            0,0,0,0,0,0,0,0,0,0,0    // row 10 (indices 110-120)
        };

        totalBalls = 0;
        for (int v : mapLayout) if (v == 2) totalBalls++;

        tiles = new JLabel[mapw * maph];
        chr   = new JLabel[mapw * maph];

        for (int i = 0; i < tiles.length; i++) {
            if      (mapLayout[i] == 0) tiles[i] = new JLabel(wall);
            else if (mapLayout[i] == 1) tiles[i] = new JLabel(path);
            else if (mapLayout[i] == 2) tiles[i] = new JLabel(ball);
            else if (mapLayout[i] == 3) tiles[i] = new JLabel(exit);
            else                         tiles[i] = new JLabel(path);
        }

        tiles[107] = new JLabel(dan);
        tiles[105] = new JLabel(tine);

        for (int i = 0; i < chr.length; i++) {
            chr[i] = new JLabel();
            if (mapLayout[i] == 4) {
                chr[i].setIcon(icon);
                chrpos = i;
            }
        }
    }


    // ========== FRAME ==========
    public void setFrame() {
        JLayeredPane layers = frame.getLayeredPane();
        layers.setLayout(new GraphPaperLayout(new Dimension(mapw, maph)));

        int x = 0, y = 0;
        for (int i = 0; i < tiles.length; i++) {
            layers.add(chr[i],   new Rectangle(x, y, 1, 1));
            layers.add(tiles[i], new Rectangle(x, y, 1, 1));
            x++;
            if (x % mapw == 0) { x = 0; y++; }
        }

        frame.setSize(frameWidth, frameHeight);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopMusic();
                System.exit(0);
            }
        });
        frame.setVisible(true);
        frame.addKeyListener(this);
        dialog.addKey(frame);
        
        saveProgress();

        startMusic("music/Main.wav");

        if (!introShown) {
            introShown = true;
            dialog.show(layers,
                new String[]{
                    "You just stepped into the Blue Brick Road...",
                    "Collect all the balls for a shot to exit this room!",
                    "The air feels tense, every step echoing louder than it should, as though the place itself is listening.",
                    "Somewhere within the winding paths, a presence lingers—quiet, watchful, and patient.",
                    "Main is here.",
                    "As you move forward, one truth becomes clear: You are not navigating the maze—the maze is leading you to her."
                },
                null, null, mapw, maph);
        }
    }
    
    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G6_Room1_PD4");

        if (save.timeSeconds <= 0) {
            SaveSystem.SaveData pd4Save = SaveSystem.loadGame("G4_Room2_PD6");
            SaveSystem.startTimer(pd4Save.timeSeconds);
        } else {
            SaveSystem.startTimer(save.timeSeconds);
        }

        if (save.isDefeated("Main")) bossDefeated = true;

        System.out.println("[Room1/PD4] Loaded — "
                + "BattleDone:" + bossDefeated
                + "  Time:" + save.formattedTime());
    }

    // =========================================================================
    // Save integration — save
    // =========================================================================
    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G6_Room1_PD4")
                .battles(SaveSystem.getDefeatedBosses())
        );
    }


    // ========== MOVEMENT ==========
    @Override
    public void keyPressed(KeyEvent e) {
        if (dialog.isVisible()) return;
        if (Battle.paused) return;

        if (e.getKeyCode() == KeyEvent.VK_D) {
            if (chrpos % mapw != mapw - 1
                    && mapLayout[chrpos + 1] != 0
                    && chrpos + 1 != 105
                    && chrpos + 1 != 107) {
                chr[chrpos].setIcon(null);
                chrpos++;
                if      (chrmodeR == 0) { chr[chrpos].setIcon(icon2);  chrmodeR = 1; }
                else if (chrmodeR == 1) { chr[chrpos].setIcon(icon11); chrmodeR = 2; }
                else                    { chr[chrpos].setIcon(icon3);  chrmodeR = 0; }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            if (chrpos % mapw != 0
                    && mapLayout[chrpos - 1] != 0
                    && chrpos - 1 != 105
                    && chrpos - 1 != 107) {
                chr[chrpos].setIcon(null);
                chrpos--;
                if      (chrmodeL == 0) { chr[chrpos].setIcon(icon4);  chrmodeL = 1; }
                else if (chrmodeL == 1) { chr[chrpos].setIcon(icon12); chrmodeL = 2; }
                else                    { chr[chrpos].setIcon(icon5);  chrmodeL = 0; }
            }

        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            if (chrpos + mapw < mapLayout.length
                    && mapLayout[chrpos + mapw] != 0
                    && chrpos + mapw != 105
                    && chrpos + mapw != 107) {
                chr[chrpos].setIcon(null);
                chrpos += mapw;
                if      (chrmodeD == 0) { chr[chrpos].setIcon(icon);  chrmodeD = 1; }
                else if (chrmodeD == 1) { chr[chrpos].setIcon(icon6); chrmodeD = 2; }
                else                    { chr[chrpos].setIcon(icon7); chrmodeD = 0; }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            if (chrpos - mapw >= 0
                    && mapLayout[chrpos - mapw] != 0
                    && chrpos - mapw != 105
                    && chrpos - mapw != 107) {
                chr[chrpos].setIcon(null);
                chrpos -= mapw;
                if      (chrmodeU == 0) { chr[chrpos].setIcon(icon8);  chrmodeU = 1; }
                else if (chrmodeU == 1) { chr[chrpos].setIcon(icon9);  chrmodeU = 2; }
                else                    { chr[chrpos].setIcon(icon10); chrmodeU = 0; }
            }
        }

        handleTileEvents();
    }


    // ========== TILE EVENTS ==========
    void handleTileEvents() {
        if (mapLayout[chrpos] == 2) {
            askQuestion();
            return;
        }

        if (chrpos == 94 && !tile94DialogShown) {
            tile94DialogShown = true;
            dialog.show(frame.getLayeredPane(),
                new String[]{
                    "Constantine: Hello, 30th student of Guanine.",
                    "Danica: You must be careful as you step forth in this journey, you must face a terror to escape this room.",
                    "Constantine: She comes by the name of Main."
                },
                null, null, mapw, maph);
            return;
        }
        
        if (chrpos == 97) {
        if (collectedBalls < totalBalls) {
            dialog.show(frame.getLayeredPane(),
                new String[]{"Collect all the balls before proceeding!"},
                null, null, mapw, maph);
            return;
        }
        dialog.show(frame.getLayeredPane(),
            new String[]{
                "Danica: The next step you take will determine your fate.",
                "You will be facing Main and I must warn you, it'll be a tough nut to crack.",
                "Constantine: Goodluck I guess..."
            },
            null, null, mapw, maph);
        return;
    }

        if (chrpos == 108 && !bossDefeated) {
            if (collectedBalls < totalBalls) {
                dialog.show(frame.getLayeredPane(),
                    new String[]{"Collect all the balls before facing Main!"},
                    null, null, mapw, maph);
                return;
            }
            startBossFight();
            return;
        }

        if (mapLayout[chrpos] == 3) {
            if (collectedBalls < totalBalls) {
                dialog.show(frame.getLayeredPane(),
                    new String[]{"You need to collect all the balls before leaving!"},
                    null, null, mapw, maph);
                return;
            }
            if (!bossDefeated) {
                dialog.show(frame.getLayeredPane(),
                    new String[]{"You must defeat Main before you can escape!"},
                    null, null, mapw, maph);
                return;
            }

            // All conditions met — save and transition to Room 2
            // NOTE: Do NOT call stopMusic() here — we pass bgmClip to Room2 so music continues seamlessly
            saveProgress();
            frame.dispose();
            G6_Room2_PD6 room2 = new G6_Room2_PD6(bgmClip); // pass the live clip
            room2.setFrame();
        }
    }


    // ========== BOSS FIGHT ==========
    void startBossFight() {
        if (bossPollerActive) return;

        bossPollerActive = true;
        Battle.paused = true;

        JLayeredPane layers = frame.getLayeredPane();
        LayoutManager mapLayout2 = layers.getLayout();
        layers.setLayout(null);

        stopMusic(); // silence during battle — no music at all

        Battle battle = new Battle();
        battle.start(frame, "images/G6_wall.png", "Main");
        waitForBattleEnd(() -> {
            if (battle.didPlayerWin()) {
                if (!SaveSystem.isDefeated("Main")) {
                    SaveSystem.markDefeated("Main");
                    bossDefeated = true;
                }
                dialog.show(frame.getLayeredPane(),
                    new String[]{
                        "You have defeated Main, she will now send you to the Chemistry Laboratory.",
                        "You better escape or face your doom."
                    },
                    null, null, mapw, maph);
                frame.requestFocusInWindow();
            } else {
                bossPollerActive = false;
            }
            saveProgress();
        });
    }
    
    private void waitForBattleEnd(Runnable onEnd) {
        javax.swing.Timer poller = new javax.swing.Timer(200, null);
        poller.addActionListener(ev -> {
            if (!Battle.paused) {
                poller.stop();
                onEnd.run();
                startMusic("music/Main.wav");
            }
        });
        poller.start();
    }


    // ========== MUSIC ==========
    void startMusic(String filepath) {
        try {
            if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filepath));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception ex) {
            System.out.println("BGM error: " + ex.getMessage());
        }
    }

    void stopMusic() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }


    // ========== QUIZ ==========
    void askQuestion() {
        if (unusedQuestions.isEmpty()) return;

        int qIndex = unusedQuestions.remove(0);

        String correctText = choices[qIndex][correctAnswer[qIndex]];
        ArrayList<String> shuffled = new ArrayList<>(Arrays.asList(choices[qIndex]));
        Collections.shuffle(shuffled);
        String[] shuffledChoices = shuffled.toArray(new String[0]);
        int newCorrectIndex = shuffled.indexOf(correctText);

        Runnable[] events = new Runnable[3];
        for (int i = 0; i < 3; i++) {
            final int choice  = i;
            final int qIdx    = qIndex;
            final int correct = newCorrectIndex;
            events[i] = () -> {
                if (choice == correct) {
                    correctBall();
                } else {
                    unusedQuestions.add(qIdx);
                    wrongBall();
                }
            };
        }

        dialog.show(frame.getLayeredPane(),
            new String[]{questions[qIndex]}, shuffledChoices, events, mapw, maph);
    }


    void correctBall() {
        collectedBalls++;
        mapLayout[chrpos] = 1;
        tiles[chrpos].setIcon(path);
        dialog.show(frame.getLayeredPane(),
            new String[]{"Correct! (" + collectedBalls + "/" + totalBalls + " balls collected)"},
            null, null, mapw, maph);
    }


    void wrongBall() {
        dialog.show(frame.getLayeredPane(),
            new String[]{"Wrong answer. Try again next time you step here."},
            null, null, mapw, maph);
    }


    // ========== QUIZ DATA ==========
    void initQuiz() {
        String[] q = {
            "Which activity improves cardiovascular endurance?",
            "Which is NOT a PE activity?",
            "What should you do after exercise?",
            "Which improves flexibility?",
            "Which improves muscular strength?",
            "How many players per team are on a basketball court?",
            "What is the standard height of a basketball ring?",
            "Which skill is used to move while bouncing the ball?",
            "How many points is a free throw worth?",
            "Which violation is walking without dribbling?",
            "What object is hit in badminton?",
            "Which equipment is used to hit the shuttlecock?",
            "How many players in singles badminton?",
            "What divides the badminton court?",
            "Which serve is legal in badminton?",
            "How many players per team on a volleyball court?",
            "What action starts a volleyball rally?",
            "Which skill is used to pass the ball upward?",
            "How many touches are allowed per team in volleyball?",
            "Which hit sends the ball strongly over the net?",
            "How many players per team in football (soccer)?",
            "Which body part cannot touch the ball in football?",
            "What do you call scoring with the head?",
            "Which player can use hands in soccer?",
            "What card means a player is sent off?",
            "What object is thrown in ultimate disc?",
            "How do players move with the disc?",
            "How is a point scored in ultimate disc?",
            "Ultimate disc is a ___ contact sport?",
            "In ultimate disc, you cannot run while holding the disc. (True or False?)"
        };

        String[][] c = {
            {"Sprint","Long run","Shot put"},
            {"Dance","Basketball","Chess"},
            {"Cool down","Stop","Sleep"},
            {"Stretching","Sprinting","Eating"},
            {"Weight lifting","Jogging","Yoga"},
            {"5","6","11"},
            {"3.05m","2m","4m"},
            {"Dribbling","Passing","Shooting"},
            {"1","2","3"},
            {"Traveling","Double dribble","Foul"},
            {"Ball","Shuttlecock","Disc"},
            {"Racket","Bat","Stick"},
            {"1","2","6"},
            {"Net","Line","Border"},
            {"Underhand","Kick","Throw"},
            {"6","5","11"},
            {"Serve","Spike","Block"},
            {"Set","Kick","Dribble"},
            {"3","2","5"},
            {"Spike","Set","Dig"},
            {"11","5","6"},
            {"Hand","Foot","Head"},
            {"Header","Volley","Pass"},
            {"Goalkeeper","Defender","Striker"},
            {"Red","Yellow","Blue"},
            {"Frisbee","Ball","Shuttlecock"},
            {"Passing","Running","Kicking"},
            {"End zone","Net","Line"},
            {"Non-contact","Full","Combat"},
            {"True","False","Sometimes"}
        };

        int[] ans = {1,2,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0};

        for (int i = 0; i < q.length; i++) {
            questions[i]     = q[i];
            choices[i]       = c[i];
            correctAnswer[i] = ans[i];
            unusedQuestions.add(i);
        }
        Collections.shuffle(unusedQuestions);
    }


    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}


    public static void main(String[] args) {
        G6_Room1_PD4 sg = new G6_Room1_PD4();
        sg.setFrame();
    }
}
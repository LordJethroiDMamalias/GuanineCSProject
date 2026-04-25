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


public class G6_Room1_PD4 implements KeyListener {


    JFrame frame;
    ImageIcon wall, path, ball, exit;
    JLabel[] tiles;
    JLabel[] chr;


    ImageIcon icon, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9, icon10;
    ImageIcon dan, tine;

    // playerBattle is now stored here so we can pass it to Battle
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
    boolean tile93DialogShown = false;

    // track whether the boss battle poller is already running
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


        saveData = SaveSystem.loadGame("G6_Room1_PD4");
        SaveSystem.startTimer(saveData.timeSeconds);

        for (String b : saveData.battles) SaveSystem.markDefeated(b);
        // bossDefeated intentionally NOT restored from save —
        // the player must fight Main every session to reach Room 2.
    }


    // ========== IMAGE LOADER ==========
    void loadImages() {
        int tileSize = frameWidth / mapw;
        icon   = scaled("images/G6_char1.png",  tileSize);
        icon2  = scaled("images/G6_char2.png",  tileSize);
        icon3  = scaled("images/G6_char3.png",  tileSize);
        icon4  = scaled("images/G6_char4.png",  tileSize);
        icon5  = scaled("images/G6_char5.png",  tileSize);
        icon6  = scaled("images/G6_char6.png",  tileSize);
        icon7  = scaled("images/G6_char7.png",  tileSize);
        icon8  = scaled("images/G6_char8.png",  tileSize);
        icon9  = scaled("images/G6_char9.png",  tileSize);
        icon10 = scaled("images/G6_char10.png", tileSize);
        wall   = scaled("images/G6_wall.png",       tileSize);
        path   = scaled("images/G6_bluepath.png",   tileSize);
        ball   = scaled("images/G6_ball.png",        tileSize);
        exit   = scaled("images/G6_door.png",        tileSize);
        dan    = scaled("images/G6_dan.png",          tileSize);
        tine   = scaled("images/G6_tine.png",         tileSize);

        // Player battle sprite — used by the Battle overlay
        playerBattle = new ImageIcon("images/G6_char2.png");
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
        // tile 108 = row 9, col 9 → value 1 (path) ← boss trigger
        // tile 109 = row 9, col 10 → value 3 (exit)
        // tile 107 = row 9, col 8 → value 1 (path) — NPC dan placed here as JLabel
        // tile 105 = row 9, col 6 → value 1 (path) — NPC tine placed here


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


        // NPCs — placed as tile overlays; movement already blocks tiles 105 and 107
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
        frame.setVisible(true);
        frame.addKeyListener(this);
        dialog.addKey(frame);

        startMusic("music/BBR.wav");


        if (!introShown) {
            introShown = true;
            dialog.show(layers,
                new String[]{
                    "You just stepped into the Blue Brick Road...",
                    "Collect all the balls."
                },
                null, null, mapw, maph);
        }
    }


    // ========== MOVEMENT ==========
    @Override
    public void keyPressed(KeyEvent e) {
        // Block input while dialog is open OR while a boss battle is running
        if (dialog.isVisible()) return;
        if (Battle.paused) return;


        if (e.getKeyCode() == KeyEvent.VK_D) {
            if (chrpos % mapw != mapw - 1
                    && mapLayout[chrpos + 1] != 0
                    && chrpos + 1 != 105
                    && chrpos + 1 != 107) {
                chr[chrpos].setIcon(null);
                chr[chrpos + 1].setIcon(chrmodeR == 0 ? icon2 : icon3);
                chrmodeR = 1 - chrmodeR;
                chrpos++;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            if (chrpos % mapw != 0
                    && mapLayout[chrpos - 1] != 0
                    && chrpos - 1 != 105
                    && chrpos - 1 != 107) {
                chr[chrpos].setIcon(null);
                chr[chrpos - 1].setIcon(chrmodeL == 0 ? icon4 : icon5);
                chrmodeL = 1 - chrmodeL;
                chrpos--;
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
        // Ball tile — ask a question
        if (mapLayout[chrpos] == 2) {
            askQuestion();
            return;
        }


        // Tile 94 — NPC dialog (one-time)
        // (tiles 105=tine, 107=dan are blocked from entry so we use the
        //  adjacent walkable tile 94 as the trigger for their combined dialog)
        if (chrpos == 94 && !tile93DialogShown) {
            tile93DialogShown = true;
            dialog.show(frame.getLayeredPane(),
                new String[]{
                    "Constantine: Hello, 30th student of Guanine.",
                    "Danica: You must be careful as you step forth in this journey, you must face a terror to escape this room.",
                    "Constantine: She comes by the name of Main."
                },
                null, null, mapw, maph);
            return;
        }


        // Tile 108 — boss fight trigger
        // Row 9, col 9 → index = 9*11 + 9 = 108
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


        // Exit tile (value 3) — requires all balls + boss defeated
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
            SaveSystem.saveGame(
                new SaveSystem.SaveData.Builder("G6_Room1_PD4")
                    .battles(SaveSystem.getDefeatedBosses())
            );
            stopMusic();
            frame.dispose();
            G6_Room2_PD6 room2 = new G6_Room2_PD6();
            room2.setFrame();
        }
    }


    // ========== BOSS FIGHT ==========
    void startBossFight() {
        // Guard: never start a second poller if one is already running
        if (bossPollerActive) return;

        bossPollerActive = true;
        Battle.paused = true;

        JLayeredPane layers = frame.getLayeredPane();

        // ── KEY FIX ──────────────────────────────────────────────────────────
        // The frame's JLayeredPane has GraphPaperLayout installed (from setFrame).
        // Battle.buildOverlay() adds components with an Integer layer constant,
        // but GraphPaperLayout requires a Rectangle constraint and throws
        // IllegalArgumentException.  We swap to null layout (absolute positioning)
        // before starting the battle, then restore GraphPaperLayout afterwards
        // so the map tiles repaint correctly when the player returns.
        // ─────────────────────────────────────────────────────────────────────
        LayoutManager mapLayout2 = layers.getLayout();   // save GraphPaperLayout
        layers.setLayout(null);                           // Battle needs null layout

        Battle battle = new Battle();
        battle.start(frame, "", "Main");

        // Poll every 500 ms until Battle.end() clears the paused flag
        javax.swing.Timer poller = new javax.swing.Timer(500, null);
        poller.addActionListener(ev -> {
            if (!Battle.paused) {
                poller.stop();
                bossPollerActive = false;

                // Restore the map layout so tiles are positioned correctly again
                layers.setLayout(mapLayout2);
                layers.revalidate();
                layers.repaint();

                // Mark defeated only in memory — NOT saved to disk so that
                // on the next session the player must fight Main again.
                bossDefeated = true;

                // Save time progress only (no battles list — boss must be re-fought)
                SaveSystem.saveGame(
                    new SaveSystem.SaveData.Builder("G6_Room1_PD4")
                );

                // Return focus to the map frame so keys work again
                frame.requestFocusInWindow();
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


        // Remove the question index from the pool before showing it
        int qIndex = unusedQuestions.remove(0);

        Runnable[] events = new Runnable[3];
        for (int i = 0; i < 3; i++) {
            final int choice  = i;
            final int qIdx    = qIndex;
            events[i] = () -> {
                if (choice == correctAnswer[qIdx]) {
                    correctBall();
                } else {
                    // Re-insert the question at the end so it can be asked again
                    unusedQuestions.add(qIdx);
                    wrongBall();
                }
            };
        }


        dialog.show(frame.getLayeredPane(),
            new String[]{questions[qIndex]}, choices[qIndex], events, mapw, maph);
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
            {"Moving ball","Passing","Shooting"},
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
package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * MEMBERS: FEDORA MAYNOPAS, GN JUMALON, EDWARD MALVAS
 */
public class G3_Room2_PD6 extends JPanel implements ActionListener, KeyListener, MouseListener {

    int playerX = 289, playerY = 275;
    final int TILE_SIZE = 40;

    ImageIcon[] pUp = new ImageIcon[4], pDown = new ImageIcon[4],
                pLeft = new ImageIcon[4], pRight = new ImageIcon[4];
    int walkFrame = 0;
    int direction = 1;

    Image bg, npcSprite, niteIcon, characterImg, chemicalImg;
    Timer timer = new Timer(16, this);
    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    Rectangle npcBox         = new Rectangle(165, 220, 26, 70);
    Rectangle npcInteractBox = new Rectangle(140, 200, 100, 150);

    Battle battle = new Battle();
    JFrame frame;
    private static Clip bgmClip = null;
    
    // ── Prevents multiple battle pollers from stacking up ─────────────────────
    private javax.swing.Timer battlePoller = null;

    // ── Guards transitionToRoom1() against double-invocation ──────────────────
    private boolean transitionStarted = false;

    boolean nearNPC = false, nearItem = false, showDialogue = false, questStarted = false;
    boolean allNitesSolved = false, finalBossTriggered = false, showNiteSplash = false;
    boolean hasNite = false, inEnding = false, gameFinished = false;
    long splashStartTime = 0;

    // ── Tracks whether we are waiting for the battle screen to close ──────────
    boolean battleInProgress = false;

    String currentObjective = "Talk to the Dazed Spirit";
    String dialogueText = "", dialogueSpeaker = "Celene";
    int dialogueStep = 0, endStep = 0;

    private String[] endDialogue = {
        "*The corrupted glow fades from Celene's eyes as she collapses*",
        "My... my head... it feels like it's finally quiet...",
        "The Nite... it wasn't supposed to be that heavy. That dark.",
        "I remember now. I was waiting for someone... a friend named Night.",
        "She loved these puzzles. Chemistry, Math... it was our language.",
        "Thank you for fixing the Nite, even if it tried to swallow me whole.",
        "I'm not a ghost anymore... I think I'm just finally free."
    };

    // ── Shown when the player loses the boss battle ───────────────────────────
    private String[] lossDialogue = {
        "The darkness swallows you whole...",
        "But something pulls you back. You can still hear the Nite humming.",
        "Get up. She still needs you."
    };
    boolean showLossDialogue = false;
    int lossStep = 0;

    class QuestItem {
        Rectangle bounds;
        String equation, answer;
        boolean solved = false;
        QuestItem(int x, int y, String eq, String ans) {
            this.bounds = new Rectangle(x, y, 60, 60);
            this.equation = eq;
            this.answer = ans;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem = null;
    String currentInput = "";

    // =========================================================================
    // Constructor
    // =========================================================================
    public G3_Room2_PD6() {
        this.frame = new JFrame("Chemistry Quest: Room 2");
        setPreferredSize(new Dimension(660, 660));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        try {
            for (int i = 0; i < 4; i++) {
                pUp[i]    = scalePlayer("images/up_"    + (i + 1) + ".png");
                pDown[i]  = scalePlayer("images/down_"  + (i + 1) + ".png");
                pLeft[i]  = scalePlayer("images/left_"  + (i + 1) + ".png");
                pRight[i] = scalePlayer("images/right_" + (i + 1) + ".png");
            }
            bg          = new ImageIcon("images/G3_background.png").getImage();
            npcSprite   = new ImageIcon("images/Celene.png").getImage();
            niteIcon    = new ImageIcon("images/G3_nite.png").getImage();
            characterImg = new ImageIcon("images/G3_FEDORA.png").getImage();
            chemicalImg  = new ImageIcon("images/G3_CHEMICAL.png").getImage();
        } catch (Exception e) {}

        solidBoxes.add(new Rectangle(256, 0, 17, 264));
        solidBoxes.add(new Rectangle(256, 308, 17, 242));
        solidBoxes.add(npcBox);

        items.add(new QuestItem(41,  330, "H2 + O2 -> H2O (ex: 1,1,1)",  "2,1,2"));
        items.add(new QuestItem(289, 462, "N2 + H2 -> NH3 (ex: 1,1,1)",  "1,3,2"));
        items.add(new QuestItem(495, 220, "CH4 + O2 -> CO2 + H2O",       "1,2,1,2"));

        loadSaveData();
        saveProgress();
        timer.start();
    }

    // =========================================================================
    // Save helpers
    // =========================================================================
    private void loadSaveData() {
        SaveSystem.SaveData save = SaveSystem.loadGame("G3_Room2_PD6");
        SaveSystem.startTimer(save.timeSeconds);
        gameFinished = save.hasFlag("room2_complete");
        if (gameFinished) {
            currentObjective = "Quest Complete!";
            hasNite = true;
            allNitesSolved = true;
            for (QuestItem item : items) item.solved = true;
        }
    }

    private void saveProgress() {
        SaveSystem.saveGame(
            new SaveSystem.SaveData.Builder("G3_Room2_PD6")
                .time(SaveSystem.getTotalSeconds())
                .flag(gameFinished ? "room2_complete" : null)
                .battles(SaveSystem.getDefeatedBosses())
        );
    }

    // =========================================================================
    // setFrame / music helpers
    // =========================================================================
    public void setFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        this.requestFocusInWindow();
    }

    public static void stopMusic() {
        if (bgmClip != null && bgmClip.isOpen()) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    private ImageIcon scalePlayer(String path) {
        ImageIcon icon = new ImageIcon(path);
        return new ImageIcon(icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
    }

    // =========================================================================
    // Movement
    // =========================================================================
    private void attemptMove(int dx, int dy) {
        if (activeQuestItem != null || showDialogue || showNiteSplash
                || showLossDialogue || battleInProgress) return;

        int nextX = playerX + dx, nextY = playerY + dy;
        if (nextX < 0 || nextX > 628 || nextY < 0 || nextY > 596) return;
        Rectangle nextHitbox = new Rectangle(nextX + 9, nextY + 53, 13, 10);
        for (Rectangle box : solidBoxes) if (nextHitbox.intersects(box)) return;

        playerX = nextX;
        playerY = nextY;
        walkFrame = (walkFrame + 1) % 4;
    }

    // =========================================================================
    // Timer tick
    // =========================================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (showNiteSplash && System.currentTimeMillis() - splashStartTime > 2000) {
            showNiteSplash = false;
            hasNite = true;
            inEnding = true;
            endStep = 0;
        }
        Rectangle playerHitbox = new Rectangle(playerX + 9, playerY + 53, 13, 10);
        nearNPC  = playerHitbox.intersects(npcInteractBox);
        nearItem = false;
        if (questStarted && !allNitesSolved) {
            Rectangle range = new Rectangle(playerX - 40, playerY - 40, 112, 144);
            for (QuestItem item : items)
                if (!item.solved && range.intersects(item.bounds)) { nearItem = true; break; }
        }
        repaint();
    }

    // =========================================================================
    // Rendering
    // =========================================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (bg != null) g.drawImage(bg, 0, 0, 660, 660, null);
        if (npcSprite != null) g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        ImageIcon sprite = (direction == 0) ? pUp[walkFrame]
                         : (direction == 2) ? pLeft[walkFrame]
                         : (direction == 3) ? pRight[walkFrame]
                         : pDown[walkFrame];
        if (sprite != null) g.drawImage(sprite.getImage(), playerX, playerY, null);

        if (questStarted) {
            for (QuestItem item : items) {
                if (!item.solved) {
                    float pulse = (float)(Math.sin(System.currentTimeMillis() / 400.0) * 0.15 + 0.85);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                    g.drawImage(npcSprite, item.bounds.x, item.bounds.y,
                                item.bounds.width, item.bounds.height, null);
                }
            }
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        if (showNiteSplash) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, 660, 660);
            if (niteIcon != null) g.drawImage(niteIcon, 180, 80, 300, 300, null);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.BOLD, 36));
            g.drawString("Restored the Nite!",
                330 - g.getFontMetrics().stringWidth("Restored the Nite!") / 2, 440);
        }

        drawUI(g);

        // Priority order: ending > loss > normal dialogue > quiz
        if (inEnding && !gameFinished)  drawEnding(g);
        else if (showLossDialogue)       drawLossDialogue(g);
        else if (showDialogue)           drawDialogueBox(g, dialogueSpeaker, dialogueText);

        if (activeQuestItem != null) drawQuizBox(g);
    }

    private void drawUI(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(20, 20, 280, 50, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("OBJECTIVE: " + (gameFinished ? "Quest Complete!" : currentObjective), 35, 50);

        if (hasNite && niteIcon != null)
            g.drawImage(niteIcon, 590, 20, 50, 50, null);

        if ((nearNPC || nearItem) && !showDialogue && activeQuestItem == null
                && !inEnding && !showLossDialogue) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(230, 385, 200, 35, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(230, 385, 200, 35, 10, 10);
            g.setColor(Color.YELLOW);
            g.drawString("[SPACE] to interact", 270, 408);
        }
    }

    private void drawDialogueBox(Graphics g, String speaker, String text) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(0, 0, 0, 240));
        g.fillRoundRect(20, 520, 620, 110, 20, 20);
        g.setColor(Color.WHITE);
        g.drawRoundRect(20, 520, 620, 110, 20, 20);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(speaker, 45, 555);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.ITALIC, 22));
        g.drawString(text, 45, 595);
    }

    private void drawQuizBox(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 660, 660);
        g.setColor(Color.CYAN);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("BALANCING TRIAL: " + activeQuestItem.equation, 50, 250);
        g.setColor(Color.GREEN);
        g.drawString("> " + currentInput + "_", 50, 400);
    }

    /** End-of-game dialogue — shown only after winning the boss battle. */
    private void drawEnding(Graphics g) {
        if (endStep < endDialogue.length) {
            // Show Fedora's character image alongside dialogue
            if (characterImg != null) g.drawImage(characterImg, 390, 50, 250, 400, null);
            drawDialogueBox(g, "Celene", endDialogue[endStep]);
        } else {
            // Final chemical splash before transition
            if (chemicalImg != null) g.drawImage(chemicalImg, 230, 150, 200, 200, null);
        }
    }

    /** Defeat screen — shown when the player loses the boss battle. */
    private void drawLossDialogue(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, 660, 660);

        // Red "DEFEATED" banner
        g.setColor(new Color(180, 0, 0));
        g.setFont(new Font("Serif", Font.BOLD, 48));
        String defeated = "DEFEATED";
        g.drawString(defeated, 330 - g.getFontMetrics().stringWidth(defeated) / 2, 200);

        // Flavour text
        if (lossStep < lossDialogue.length) {
            drawDialogueBox(g, "???", lossDialogue[lossStep]);
        } else {
            // Prompt to retry
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Serif", Font.BOLD, 28));
            String prompt = "Press SPACE to challenge Celene again...";
            g.drawString(prompt, 330 - g.getFontMetrics().stringWidth(prompt) / 2, 380);
        }
    }

    // =========================================================================
    // Keyboard input
    // =========================================================================
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // ── HIGHEST PRIORITY: End dialogue (only after winning) ───────────────
        if (inEnding && !gameFinished) {
            if (key == KeyEvent.VK_SPACE) {
                endStep++;
                if (endStep >= endDialogue.length) {
                    inEnding     = false;    // ← stop re-entry before async dispose
                    gameFinished = true;
                    currentObjective = "Quest Complete!";
                    saveProgress();
                    transitionToRoom1();     // idempotency guard inside handles spam
                }
            }
            repaint();
            return;
        }

        // ── Loss dialogue / retry prompt ──────────────────────────────────────
        if (showLossDialogue) {
            if (key == KeyEvent.VK_SPACE) {
                if (lossStep < lossDialogue.length) {
                    lossStep++;
                } else {
                    // Player confirmed retry — start battle again
                    showLossDialogue = false;
                    lossStep = 0;
                    launchBoss();
                }
            }
            repaint();
            return;
        }

        // ── Quiz input ────────────────────────────────────────────────────────
        if (activeQuestItem != null) { handleQuizInput(e); return; }

        // ── Normal interaction / movement ─────────────────────────────────────
        if (key == KeyEvent.VK_SPACE) {
            if (showDialogue) {
                advanceDialogue();
            } else if (nearNPC) {
                if (!questStarted) {
                    dialogueSpeaker = "Celene";
                    dialogueText    = "Where... where is it? The light... it broke...";
                    showDialogue    = true;
                    questStarted    = true;
                    currentObjective = "Collect the Nite Fragments";
                } else if (allNitesSolved && !finalBossTriggered) {
                    dialogueStep = 20;
                    showDialogue  = true;
                    advanceDialogue();
                }
            } else if (nearItem) {
                Rectangle range = new Rectangle(playerX - 40, playerY - 40, 112, 144);
                for (QuestItem item : items)
                    if (!item.solved && range.intersects(item.bounds)) {
                        activeQuestItem = item;
                        currentInput = "";
                        break;
                    }
            }
        } else {
            if      (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    direction = 0;
            else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  direction = 1;
            else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  direction = 2;
            else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) direction = 3;

            attemptMove(
                (direction == 2 ? -TILE_SIZE : direction == 3 ? TILE_SIZE : 0),
                (direction == 0 ? -TILE_SIZE : direction == 1 ? TILE_SIZE : 0)
            );
        }
        repaint();
    }

    // =========================================================================
    // Quiz input
    // =========================================================================
    private void handleQuizInput(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (currentInput.trim().equalsIgnoreCase(activeQuestItem.answer)) {
                activeQuestItem.solved = true;
                activeQuestItem = null;
                checkAllPieces();
                saveProgress();
            } else {
                currentInput = "";
            }
        } else if (key == KeyEvent.VK_BACK_SPACE && currentInput.length() > 0) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else if (key == KeyEvent.VK_ESCAPE) {
            activeQuestItem = null;
        } else {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == ',' || c == '+') currentInput += c;
        }
        repaint();
    }

    // =========================================================================
    // Dialogue advancement
    // =========================================================================
    private void advanceDialogue() {
        if      (dialogueStep == 20) { dialogueText = "...It's... it's humming. The pieces are together..."; dialogueStep = 21; }
        else if (dialogueStep == 21) { dialogueSpeaker = "Me";     dialogueText = "Celene? Is the Nite supposed to be black?"; dialogueStep = 22; }
        else if (dialogueStep == 22) { dialogueSpeaker = "Celene"; dialogueText = "STAY AWAY! THE NITE... IT WANTS TO FEED!";  dialogueStep = 23; }
        else if (dialogueStep == 23) {
            showDialogue = false;
            finalBossTriggered = true;
            stopMusic();
            launchBoss();          // <-- centralised boss launcher (handles win/loss)
            return;
        } else if (dialogueStep == 30) {
            showDialogue = false;
            showNiteSplash = true;
            splashStartTime = System.currentTimeMillis();
        } else {
            showDialogue = false;
        }
        repaint();
    }

    // =========================================================================
    // Boss battle launcher — mirrors G2's waitForBattleEnd pattern
    // =========================================================================
    /**
     * Starts the Celene boss battle and attaches a 200 ms poller
     * (identical to G2's waitForBattleEnd) that fires when Battle.paused
     * flips back to false.
     *
     * Win  → SaveSystem.isDefeated("Celene") == true  → trigger end dialogue
     * Loss → SaveSystem.isDefeated("Celene") == false → show loss screen / retry
     */
    private void launchBoss() {
        // Prevent double-launch from SPACE spam or overlapping state.
        if (battleInProgress) return;

        battleInProgress  = true;
        showDialogue      = false;   // clear any open dialogue box
        showLossDialogue  = false;   // clear loss screen if retrying

        battle = new Battle();       // fresh instance = full player HP, clean state
        battle.start(frame, "images/G3_background.png", "Celene");

        waitForBattleEnd(() -> {
            if (battle.didPlayerWin()) {
                // Mark defeated unconditionally — the SaveSystem guard was removed
                // because it caused inEnding to be skipped on retry-wins.
                SaveSystem.markDefeated("Celene");
                inEnding = true;
                endStep  = 0;
            } else {
                showLossDialogue = true;
                lossStep         = 0;
            }
            saveProgress();
            repaint();
        });
    }
    
    private void waitForBattleEnd(Runnable onEnd) {
        // Kill any previous poller so two can never run simultaneously.
        if (battlePoller != null && battlePoller.isRunning()) {
            battlePoller.stop();
        }

        final boolean[] battleStarted = { false };

        battlePoller = new javax.swing.Timer(200, null);
        battlePoller.addActionListener(ev -> {
            // Phase 1 — wait until Battle.start() has actually set paused = true.
            // This absorbs the stale `false` left over from the previous battle.
            if (!battleStarted[0]) {
                if (Battle.paused) battleStarted[0] = true;
                return;
            }
            // Phase 2 — now wait for the battle to finish (paused → false).
            if (!Battle.paused) {
                battlePoller.stop();
                battlePoller = null;
                battleInProgress = false;
                SwingUtilities.invokeLater(() -> G3_Room2_PD6.this.requestFocusInWindow());
                onEnd.run();
            }
        });
        battlePoller.start();
    }

    // =========================================================================
    // Piece collection check
    // =========================================================================
    private void checkAllPieces() {
        if (finalBossTriggered) {
            dialogueSpeaker = "Celene";
            dialogueText    = "I see clearly now. Take the Nite.";
            showDialogue    = true;
            dialogueStep    = 30;
            return;
        }
        boolean allDone = true;
        for (QuestItem item : items) if (!item.solved) allDone = false;
        if (allDone && !allNitesSolved) {
            allNitesSolved   = true;
            currentObjective = "Return to Celene";
            dialogueSpeaker  = "Me";
            dialogueText     = "I have the fragments. She looks lost...";
            showDialogue     = true;
        }
    }

    // =========================================================================
    // Room transition — called after the end dialogue finishes
    // =========================================================================
    /**
     * Disposes this room and launches G4_Room1_PD4.
     * Mirrors G2's transitionToG4() pattern exactly.
     */
    private void transitionToRoom1() {
        if (transitionStarted) return;   // idempotency guard
        transitionStarted = true;

        timer.stop();    // stop the game loop — nothing should repaint after this
        stopMusic();     // release room BGM

        SwingUtilities.invokeLater(() -> {
            frame.dispose();
            try {
                G4_Room1_PD4 nextRoom = new G4_Room1_PD4();
                nextRoom.setFrame();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    null,
                    "Failed to load G4_Room1_PD4: " + ex.getMessage(),
                    "Transition Error",
                    JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });
    }

    // =========================================================================
    // Mouse input
    // =========================================================================
    @Override
    public void mousePressed(MouseEvent e) {
        Point click = e.getPoint();
        if (showDialogue)           { advanceDialogue(); return; }
        if (showLossDialogue)       { /* advance via SPACE only */ return; }
        if (questStarted && !allNitesSolved) {
            for (QuestItem item : items)
                if (!item.solved && item.bounds.contains(click)) {
                    activeQuestItem = item;
                    currentInput = "";
                    repaint();
                    break;
                }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    // =========================================================================
    // Entry point
    // =========================================================================
    public static void main(String[] args) {
        playFedoraMusic("music/Celene.wav");
        SwingUtilities.invokeLater(() -> {
            try { new G3_Room2_PD6().setFrame(); }
            catch (Exception e) { e.printStackTrace(); }
        });
    }

    private static void playFedoraMusic(String location) {
        try {
            File musicPath = new File(location);
            if (musicPath.exists()) {
                AudioInputStream ai = AudioSystem.getAudioInputStream(musicPath);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(ai);
                bgmClip.start();
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {}
    }
}
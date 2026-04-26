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
 * LORE: Celene is dazed/memory-wiped. MC collects broken Nite pieces.
 * The Nite becomes corrupted upon restoration, triggering a boss fight.
 */
public class G3_Room2_PD6 extends JPanel implements ActionListener, KeyListener, MouseListener {

    // ── Grid & Movement Constants ─────────────────────────────────────────
    // Window: 660x660. All world coords scaled from original 800x600 design.
    // scaleX = 660/800 = 0.825,  scaleY = 660/600 = 1.1
    int playerX = 289, playerY = 275;   // was (350, 250)
    final int TILE_SIZE = 40;

    // ── Animated player sprites ───────────────────────────────────────────
    ImageIcon[] pUp    = new ImageIcon[4];
    ImageIcon[] pDown  = new ImageIcon[4];
    ImageIcon[] pLeft  = new ImageIcon[4];
    ImageIcon[] pRight = new ImageIcon[4];
    int walkFrame = 0;
    int direction = 1;

    Image bg, npcSprite, niteIcon, shiningSprite, characterImg, chemicalImg;

    Timer timer = new Timer(16, this);
    ArrayList<Rectangle> solidBoxes = new ArrayList<>();

    // NPC scaled: 200*0.825=165, 200*1.1=220, 32*0.825=26, 64*1.1=70
    Rectangle npcBox         = new Rectangle(165, 220, 26, 70);
    // Interact zone: 180*0.825=149, 180*1.1=198, 100*0.825=83, 120*1.1=132
    Rectangle npcInteractBox = new Rectangle(149, 198, 83, 132);

    Battle battle = new Battle();
    JFrame frame;

    // ── Music Variable ────────────────────────────────────────────────────
    private static Clip bgmClip = null;

    boolean nearNPC            = false;
    boolean nearItem           = false;
    boolean showDialogue       = false;
    boolean questStarted       = false;
    boolean allNitesSolved     = false;
    boolean finalBossTriggered = false;
    boolean showNiteSplash     = false;
    boolean hasNite            = false;
    boolean inEnding           = false;
    boolean gameFinished       = false;
    long    splashStartTime    = 0;

    String currentObjective = "Talk to the Dazed Spirit";
    String dialogueText     = "";
    String dialogueSpeaker  = "Celene";
    int    dialogueStep     = 0;
    int    endStep          = 0;

    private String[] endDialogue = {
        "*The corrupted glow fades from Celene's eyes as she collapses*",
        "My... my head... it feels like it's finally quiet...",
        "The Nite... it wasn't supposed to be that heavy. That dark.",
        "I remember now. I was waiting for someone... a friend named Night.",
        "She loved these puzzles. Chemistry, Math... it was our language.",
        "Thank you for fixing the Nite, even if it tried to swallow me whole.",
        "I'm not a ghost anymore... I think I'm just finally free."
    };

    class QuestItem {
        Rectangle bounds;
        String equation;
        String answer;
        boolean solved = false;

        QuestItem(int x, int y, String eq, String ans) {
            // item size scaled: 40*0.825=33, 70*1.1=77
            this.bounds   = new Rectangle(x, y, 33, 77);
            this.equation = eq;
            this.answer   = ans;
        }
    }

    ArrayList<QuestItem> items = new ArrayList<>();
    QuestItem activeQuestItem  = null;
    String    currentInput     = "";

    private ImageIcon scalePlayer(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == MediaTracker.ERRORED)
                return new ImageIcon(new java.awt.image.BufferedImage(32, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB));
            return new ImageIcon(icon.getImage().getScaledInstance(32, 64, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return new ImageIcon();
        }
    }

    public G3_Room2_PD6(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(660, 660));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        playMusic("music/Fedora.wav");

        try {
            for (int i = 0; i < 4; i++) {
                pUp[i]    = scalePlayer("images/up_"    + (i + 1) + ".png");
                pDown[i]  = scalePlayer("images/down_"  + (i + 1) + ".png");
                pLeft[i]  = scalePlayer("images/left_"  + (i + 1) + ".png");
                pRight[i] = scalePlayer("images/right_" + (i + 1) + ".png");
            }
            bg            = new ImageIcon("images/G3_background.png").getImage();
            npcSprite     = new ImageIcon("images/Celene.png").getImage();
            niteIcon      = new ImageIcon("images/G3_nite.png").getImage();
            shiningSprite = new ImageIcon("images/G3_light.png").getImage();
            characterImg  = new ImageIcon("images/G3_FEDORA.png").getImage();
            chemicalImg   = new ImageIcon("images/G3_CHEMICAL.png").getImage();
        } catch (Exception e) {
            System.err.println("Resource error: " + e.getMessage());
        }

        // ── Solid boxes scaled from original 800x600 ──────────────────────
        solidBoxes.add(new Rectangle(  0, 176,  33, 11));  // was (0,   160,  40, 10)
        solidBoxes.add(new Rectangle( 66, 176,  50, 11));  // was (80,  160,  60, 10)
        solidBoxes.add(new Rectangle(107,  99,   8, 88));  // was (130,  90,  10, 80)
        solidBoxes.add(new Rectangle(107,  99, 165, 11));  // was (130,  90, 200, 10)
        solidBoxes.add(new Rectangle(256,   0,  17,264));  // was (310,   0,  20,240)
        solidBoxes.add(new Rectangle(256, 308,  17,242));  // was (310, 280,  20,220)
        solidBoxes.add(new Rectangle(  0, 545, 272, 11));  // was (0,   495, 330, 10)
        solidBoxes.add(new Rectangle(256, 440,  58, 11));  // was (310, 400,  70, 10)
        solidBoxes.add(new Rectangle(371, 440,  74, 11));  // was (450, 400,  90, 10)
        solidBoxes.add(new Rectangle(425,   0,  17,110));  // was (515,   0,  20,100)
        solidBoxes.add(new Rectangle(425, 171,  21,385));  // was (515, 155,  25,350)
        solidBoxes.add(new Rectangle(425, 539, 235, 17));  // was (515, 490, 330, 15) — right edge capped at 660
        solidBoxes.add(new Rectangle(425, 358, 165, 17));  // was (515, 325, 200, 15)
        solidBoxes.add(npcBox);

        // Quest item positions scaled: x*0.825, y*1.1
        // was (50,300), (350,420), (600,200)
        items.add(new QuestItem( 41, 330, "H2 + O2 -> H2O (ex: 1,1,1)",          "2,1,2"));
        items.add(new QuestItem(289, 462, "N2 + H2 -> NH3 (ex: 1,1,1)",           "1,3,2"));
        items.add(new QuestItem(495, 220, "CH4 + O2 -> CO2 + H2O (ex: 1,1,1,1)", "1,2,1,2"));

        timer.start();
    }

    // --- Music Logic ---
    private void playMusic(String location) {
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
        } catch (Exception e) {
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

    private ImageIcon currentSprite() {
        return switch (direction) {
            case 0 -> pUp[walkFrame];
            case 2 -> pLeft[walkFrame];
            case 3 -> pRight[walkFrame];
            default -> pDown[walkFrame];
        };
    }

    private void attemptMove(int dx, int dy) {
        if (activeQuestItem != null || showNiteSplash) return;

        int nextX = playerX + dx;
        int nextY = playerY + dy;
        if (nextX < 0 || nextX > 660 - 32 || nextY < 0 || nextY > 660 - 64) return;

        Rectangle nextHitbox = new Rectangle(nextX + 9, nextY + 53, 13, 10);
        boolean blocked = false;
        for (Rectangle box : solidBoxes) {
            if (nextHitbox.intersects(box)) { blocked = true; break; }
        }
        if (!blocked) {
            playerX = nextX;
            playerY = nextY;
            walkFrame = (walkFrame + 1) % 4;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (showNiteSplash) {
            if (System.currentTimeMillis() - splashStartTime > 2000) {
                showNiteSplash = false;
                hasNite = true;
                inEnding = true;
                endStep = 0;
                this.requestFocusInWindow();
            }
        }

        Rectangle playerHitbox = new Rectangle(playerX + 9, playerY + 53, 13, 10);
        nearNPC = playerHitbox.intersects(npcInteractBox);

        nearItem = false;
        if (questStarted && !allNitesSolved) {
            Rectangle interactZone = new Rectangle(playerX - 30, playerY - 30, 92, 124);
            for (QuestItem item : items) {
                if (!item.solved && interactZone.intersects(item.bounds)) {
                    nearItem = true;
                    break;
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        else { g.setColor(Color.BLACK); g.fillRect(0, 0, 660, 660); }

        if (npcSprite != null) g.drawImage(npcSprite, npcBox.x, npcBox.y, npcBox.width, npcBox.height, null);

        ImageIcon sprite = currentSprite();
        if (sprite != null && sprite.getIconWidth() > 0)
            g.drawImage(sprite.getImage(), playerX, playerY, playerX + 32, playerY + 64,
                        0, 0, sprite.getIconWidth(), sprite.getIconHeight(), null);

        if (questStarted) {
            for (QuestItem item : items) {
                if (!item.solved && npcSprite != null) {
                    float pulse = (float)(Math.sin(System.currentTimeMillis() / 400.0) * 0.15 + 0.85);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
                    g.drawImage(npcSprite, item.bounds.x, item.bounds.y, item.bounds.width, item.bounds.height, null);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }
        }

        if (showNiteSplash) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, 660, 660);
            if (niteIcon != null) g.drawImage(niteIcon, 180, 80, 300, 300, null);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.BOLD, 36));
            String msg = "Restored the Nite!";
            g.drawString(msg, 330 - g.getFontMetrics().stringWidth(msg) / 2, 440);
        }

        drawUI(g);

        if (inEnding && !gameFinished) {
            drawEnding(g);
        } else if (showDialogue) {
            drawDialogueBox(g, dialogueSpeaker, dialogueText);
        }

        if (activeQuestItem != null) drawQuizBox(g);
    }

    private void drawUI(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(20, 20, 280, 50, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(20, 20, 280, 50, 15, 15);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("OBJECTIVE:", 35, 40);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(questStarted ? (allNitesSolved ? Color.GREEN : Color.YELLOW) : Color.WHITE);
        g.drawString(gameFinished ? "Quest Complete!" : currentObjective, 35, 58);

        if (hasNite && niteIcon != null) {
            g.drawImage(niteIcon, 590, 20, 50, 50, null);
        }

        if ((nearNPC || nearItem) && !showDialogue && activeQuestItem == null && !inEnding) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(230, 390, 200, 30, 10, 10);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("[SPACE] to interact", 265, 410);
        }
    }

    private void drawDialogueBox(Graphics g, String speaker, String text) {
        int bw = 620, bh = 110, bx = 20, by = 520;
        g.setColor(new Color(0, 0, 0, 240));
        g.fillRoundRect(bx, by, bw, bh, 20, 20);
        g.setColor(Color.WHITE);
        g.drawRoundRect(bx, by, bw, bh, 20, 20);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(speaker, bx + 25, by + 35);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.ITALIC, 22));
        g.drawString(text, bx + 25, by + 75);
    }

    private void drawQuizBox(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("BALANCING TRIAL:", 100, 100);
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        g.setColor(Color.CYAN);
        String eq = activeQuestItem.equation;
        g.drawString(eq, 330 - g.getFontMetrics().stringWidth(eq) / 2, 220);
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        g.setColor(Color.GREEN);
        String inp = "> " + currentInput + "_";
        g.drawString(inp, 330 - g.getFontMetrics().stringWidth(inp) / 2, 380);
    }

    private void drawEnding(Graphics g) {
        if (endStep < endDialogue.length) {
            if (characterImg != null) g.drawImage(characterImg, 390, 50, 250, 400, null);
            drawDialogueBox(g, "Celene", endDialogue[endStep]);
        } else if (endStep == endDialogue.length) {
            if (chemicalImg != null) g.drawImage(chemicalImg, 230, 150, 200, 200, null);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (inEnding) {
            if (key == KeyEvent.VK_SPACE) {
                endStep++;
                if (endStep > endDialogue.length + 1) {
                    inEnding = false;
                    gameFinished = true;
                    currentObjective = "Quest Complete!";
                }
                repaint();
            }
        }

        if (activeQuestItem != null) { handleQuizInput(e); return; }

        if (key == KeyEvent.VK_SPACE) {
            if (showDialogue) {
                advanceDialogue();
            } else if (nearNPC) {
                if (!questStarted) {
                    dialogueSpeaker  = "Celene";
                    dialogueText     = "Where... where is it? The light... it broke into pieces...";
                    showDialogue     = true;
                    questStarted     = true;
                    currentObjective = "Collect the Nite Fragments";
                } else if (allNitesSolved && !finalBossTriggered) {
                    dialogueStep = 20;
                    showDialogue = true;
                    advanceDialogue();
                }
            } else if (nearItem) {
                Rectangle interactZone = new Rectangle(playerX - 30, playerY - 30, 92, 124);
                for (QuestItem item : items) {
                    if (!item.solved && interactZone.intersects(item.bounds)) {
                        activeQuestItem = item;
                        currentInput = "";
                        break;
                    }
                }
            }
        }
        else if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)   { direction = 0; attemptMove(0, -TILE_SIZE); }
        else if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  { direction = 1; attemptMove(0,  TILE_SIZE); }
        else if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  { direction = 2; attemptMove(-TILE_SIZE, 0); }
        else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) { direction = 3; attemptMove( TILE_SIZE, 0); }

        repaint();
    }

    private void handleQuizInput(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (currentInput.trim().equalsIgnoreCase(activeQuestItem.answer)) {
                activeQuestItem.solved = true;
                activeQuestItem = null;
                checkAllPieces();
            } else {
                currentInput = "";
            }
        } else if (key == KeyEvent.VK_BACK_SPACE && currentInput.length() > 0) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == ',' || c == '+') currentInput += c;
        }
        repaint();
    }

    private void advanceDialogue() {
        if      (dialogueStep == 20) { dialogueSpeaker = "Celene"; dialogueText = "...It's... it's humming. The pieces are together...";        dialogueStep = 21; }
        else if (dialogueStep == 21) { dialogueSpeaker = "Me";     dialogueText = "Celene? You're shaking. Is the Nite supposed to be black?"; dialogueStep = 22; }
        else if (dialogueStep == 22) { dialogueSpeaker = "Celene"; dialogueText = "Too loud... the chemicals... the logic... it's all WRONG!"; dialogueStep = 23; }
        else if (dialogueStep == 23) { dialogueSpeaker = "Celene"; dialogueText = "STAY AWAY! THE NITE... IT WANTS TO FEED!";                  dialogueStep = 24; }
        else if (dialogueStep == 24) { dialogueSpeaker = "System"; dialogueText = "The Nite's corruption has taken control of Celene!";        dialogueStep = 25; }
        else if (dialogueStep == 25) {
            showDialogue       = false;
            finalBossTriggered = true;
            stopMusic();  // stop overworld BGM before battle
            battle.start(frame, "images/G3_background.png", "Celene");
            inEnding = true;
            endStep  = 0;
            this.requestFocusInWindow();
        }
        else if (dialogueStep == 30) {
            showDialogue    = false;
            showNiteSplash  = true;
            splashStartTime = System.currentTimeMillis();
        }
        else { showDialogue = false; }
        repaint();
    }

    private void checkAllPieces() {
        if (finalBossTriggered) {
            dialogueSpeaker = "Celene";
            dialogueText    = "I... I can see clearly now. Take the Nite... it's safe now.";
            showDialogue    = true;
            dialogueStep    = 30;
            return;
        }

        boolean allDone = true;
        for (QuestItem item : items) if (!item.solved) { allDone = false; break; }

        if (allDone && !allNitesSolved) {
            allNitesSolved   = true;
            currentObjective = "Return to Celene";
            dialogueSpeaker  = "Me";
            dialogueText     = "I've gathered all the fragments. She looks so lost...";
            showDialogue     = true;
            dialogueStep     = 0;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chemistry Quest: Room 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new G3_Room2_PD6(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
